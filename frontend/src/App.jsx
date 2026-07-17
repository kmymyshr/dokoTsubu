/**
 * メイン画面のReactルートコンポーネント。
 *
 * Phase6では、旧JSPで描画していた投稿一覧画面の主な操作をこのコンポーネント配下へ集約する。
 * ここではログインユーザー、投稿一覧、検索条件、ページング、編集ダイアログの状態を管理し、
 * 個別の表示は小さなコンポーネントへ委譲する。
 */
import { useCallback, useEffect, useRef, useState } from "react";
import { createMutter, deleteMutter, fetchMutterPage, fetchSession, updateMutter } from "./api.js";
import EditDialog from "./components/EditDialog.jsx";
import Header from "./components/Header.jsx";
import MutterList from "./components/MutterList.jsx";
import PostForm from "./components/PostForm.jsx";
import SearchForm from "./components/SearchForm.jsx";

const PAGE_LIMIT = 20;

/** APIレスポンスに含まれる投稿者ごとのフォロー状態を、画面更新しやすいMap形式にする。 */
function buildFollowStateMap(mutters) {
  return Object.fromEntries(
    mutters.map(mutter => [mutter.userId, Boolean(mutter.followedByMe)])
  );
}

export default function App() {
  const contextPath = document.body.dataset.contextPath || "";
  const [user, setUser] = useState(null);
  const [mutters, setMutters] = useState([]);
  const [followStateByUserId, setFollowStateByUserId] = useState({});
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState({ nextCursor: null, hasNext: false });
  const [olderPagesLoaded, setOlderPagesLoaded] = useState(false);
  const [editing, setEditing] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [message, setMessage] = useState(null);
  const requestInFlight = useRef(false);

  /**
   * 投稿一覧を読み込む共通処理。
   *
   * append=trueなら「さらに読み込む」、falseなら検索/更新後の先頭再取得として扱う。
   * 自動更新では画面全体のローディング表示を出しすぎないよう silent=true を使う。
   */
  const loadPage = useCallback(async ({ append = false, search = keyword, silent = false } = {}) => {
    if (requestInFlight.current) return false;
    requestInFlight.current = true;
    if (silent) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }
    try {
      const result = await fetchMutterPage({
        keyword: search,
        cursor: append ? page.nextCursor : null,
        limit: PAGE_LIMIT
      });
      setMutters(current => append ? [...current, ...result.mutters] : result.mutters);
      setFollowStateByUserId(current => {
        if (!append) {
          return buildFollowStateMap(result.mutters);
        }

        const next = { ...current };
        for (const mutter of result.mutters) {
          next[mutter.userId] = Boolean(mutter.followedByMe);
        }
        return next;
      });
      setPage({ nextCursor: result.nextCursor, hasNext: result.hasNext });
      if (append) setOlderPagesLoaded(true);
      if (!silent) setMessage(null);
      return true;
    } catch (error) {
      if (!silent) setMessage({ text: error.message, error: true });
      console.error(error);
      return false;
    } finally {
      requestInFlight.current = false;
      if (silent) {
        setRefreshing(false);
      } else {
        setLoading(false);
      }
    }
  }, [keyword, page.nextCursor]);

  /** 初期表示時にセッション情報と投稿一覧を取得する。 */
  useEffect(() => {
    let active = true;
    async function initialize() {
      requestInFlight.current = true;
      try {
        const sessionUser = await fetchSession();
        if (!active) return;
        setUser(sessionUser);
        const result = await fetchMutterPage({ limit: PAGE_LIMIT });
        if (!active) return;
        setMutters(result.mutters);
        setFollowStateByUserId(buildFollowStateMap(result.mutters));
        setPage({ nextCursor: result.nextCursor, hasNext: result.hasNext });
      } catch (error) {
        if (active) setMessage({ text: error.message, error: true });
      } finally {
        requestInFlight.current = false;
        if (active) setLoading(false);
      }
    }
    initialize();
    return () => { active = false; };
  }, []);

  /**
   * 旧画面の自動更新に相当する処理。
   *
   * 検索中・過去ページ読み込み後・編集中は、表示中の文脈を壊さないため自動更新しない。
   */
  useEffect(() => {
    const timer = window.setInterval(() => {
      if (document.visibilityState === "visible"
          && !requestInFlight.current
          && !keyword
          && !olderPagesLoaded
          && !editing) {
        loadPage({ silent: true });
      }
    }, 5000);
    return () => window.clearInterval(timer);
  }, [keyword, olderPagesLoaded, editing, loadPage]);

  /** 投稿作成後は検索条件を解除し、最新一覧を再取得する。 */
  async function handlePost(text) {
    setLoading(true);
    try {
      await createMutter(text);
      setKeyword("");
      setOlderPagesLoaded(false);
      setMessage({ text: "投稿しました。", error: false });
      await loadPage({ search: "", silent: true });
      return true;
    } catch (error) {
      setMessage({ text: error.message, error: true });
      return false;
    } finally {
      setLoading(false);
    }
  }

  /** 検索条件を更新し、先頭ページから読み直す。 */
  async function handleSearch(nextKeyword) {
    setKeyword(nextKeyword);
    setOlderPagesLoaded(false);
    await loadPage({ search: nextKeyword });
  }

  /** 削除前に確認を挟み、成功後は一覧を再取得する。 */
  async function handleDelete(mutter) {
    if (!window.confirm("この投稿を削除しますか？")) return;
    try {
      await deleteMutter(mutter.id);
      setMessage({ text: "投稿を削除しました。", error: false });
      setOlderPagesLoaded(false);
      await loadPage({ silent: true });
    } catch (error) {
      setMessage({ text: error.message, error: true });
    }
  }

  /** 編集ダイアログからの保存。楽観ロック用versionもAPIへ渡す。 */
  async function handleUpdate(id, text, version) {
    setLoading(true);
    try {
      await updateMutter(id, text, version);
      setEditing(null);
      setMessage({ text: "投稿を更新しました。", error: false });
      setOlderPagesLoaded(false);
      await loadPage({ silent: true });
    } catch (error) {
      setMessage({ text: error.message, error: true });
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <Header user={user} contextPath={contextPath} />
      <main>
        {message && <p className={`message${message.error ? " error" : ""}`} role="status">{message.text}</p>}
        {refreshing && <p className="message" role="status">最新の投稿を確認しています...</p>}
        <PostForm onSubmit={handlePost} disabled={loading || !user} />
        <SearchForm onSearch={handleSearch} disabled={loading || !user} />
        <MutterList mutters={mutters} user={user} contextPath={contextPath}
                    followStateByUserId={followStateByUserId}
                    onFollowChange={(userId, following) => {
                      setFollowStateByUserId(current => ({ ...current, [userId]: following }));
                    }}
                    loading={loading} hasNext={page.hasNext}
                    onRefresh={() => { setOlderPagesLoaded(false); loadPage(); }}
                    onLoadMore={() => loadPage({ append: true })}
                    onEdit={setEditing} onDelete={handleDelete} />
      </main>
      <EditDialog mutter={editing} saving={loading}
                  onCancel={() => setEditing(null)} onSave={handleUpdate} />
      <footer><p>dokoTsubu</p></footer>
    </>
  );
}
