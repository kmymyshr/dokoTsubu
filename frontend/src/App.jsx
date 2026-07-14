import { useCallback, useEffect, useRef, useState } from "react";
import { createMutter, deleteMutter, fetchMutterPage, fetchSession, updateMutter } from "./api.js";
import EditDialog from "./components/EditDialog.jsx";
import Header from "./components/Header.jsx";
import MutterList from "./components/MutterList.jsx";
import PostForm from "./components/PostForm.jsx";
import SearchForm from "./components/SearchForm.jsx";

const PAGE_LIMIT = 20;

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
    } catch (error) {
      if (!silent) setMessage({ text: error.message, error: true });
      console.error(error);
    } finally {
      requestInFlight.current = false;
      if (silent) {
        setRefreshing(false);
      } else {
        setLoading(false);
      }
    }
  }, [keyword, page.nextCursor]);

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

  async function handlePost(text) {
    setLoading(true);
    try {
      await createMutter(text);
      setKeyword("");
      setOlderPagesLoaded(false);
      setMessage({ text: "つぶやきを投稿しました。", error: false });
      await loadPage({ search: "", silent: true });
      return true;
    } catch (error) {
      setMessage({ text: error.message, error: true });
      return false;
    } finally {
      setLoading(false);
    }
  }

  async function handleSearch(nextKeyword) {
    setKeyword(nextKeyword);
    setOlderPagesLoaded(false);
    await loadPage({ search: nextKeyword });
  }

  async function handleDelete(mutter) {
    if (!window.confirm("このつぶやきを削除しますか？")) return;
    try {
      await deleteMutter(mutter.id);
      setMessage({ text: "つぶやきを削除しました。", error: false });
      setOlderPagesLoaded(false);
      await loadPage({ silent: true });
    } catch (error) {
      setMessage({ text: error.message, error: true });
    }
  }

  async function handleUpdate(id, text, version) {
    setLoading(true);
    try {
      await updateMutter(id, text, version);
      setEditing(null);
      setMessage({ text: "つぶやきを更新しました。", error: false });
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
      <footer><p>どこつぶ</p></footer>
    </>
  );
}
