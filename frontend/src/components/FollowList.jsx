/**
 * フォロー中/フォロワー一覧を表示するReactコンポーネント。
 *
 * Phase9では、Phase8で用意した `/api/follows` を使ってJSPの一覧描画をReactへ移す。
 * JSPは画面のホストに縮小し、このコンポーネントが一覧取得、空表示、フォロー切り替え後の
 * 画面更新を担当する。
 */
import { useEffect, useState } from "react";
import { fetchFollowList, followUser } from "../api.js";
import Header from "./Header.jsx";

const TITLE_BY_TYPE = {
  followers: "フォロワー",
  following: "フォロー中"
};

const EMPTY_MESSAGE_BY_TYPE = {
  followers: "まだフォロワーはいません。",
  following: "まだフォローしているユーザーはいません。"
};

/** APIのtype値を、画面で扱う2種類へ正規化する。 */
function normalizeType(type) {
  return type === "followers" || type === "following" ? type : "followers";
}

/**
 * API取得済みの状態をHTMLへ変換する表示専用部分。
 *
 * データ取得と表示を分けておくと、後続でページ遷移やルーティングを見直すときも
 * 一覧の見た目だけをテストしやすい。
 */
export function FollowListView({
  contextPath,
  sessionUser,
  userId,
  type,
  targetUser,
  users,
  count,
  loading,
  message,
  onRefresh,
  onFollowClick
}) {
  const normalizedType = normalizeType(type);
  const title = TITLE_BY_TYPE[normalizedType];
  const emptyMessage = EMPTY_MESSAGE_BY_TYPE[normalizedType];

  return (
    <>
      <Header user={sessionUser} contextPath={contextPath} />
      <main>
        <p>
          <a href={`${contextPath}/Profile?userId=${userId}`}>プロフィールへ戻る</a>
        </p>

        {message && <p className={`message${message.error ? " error" : ""}`} role="status">{message.text}</p>}

        <section className="profile-card" aria-labelledby="followListHeading">
          <div className="section-heading">
            <h1 id="followListHeading">
              {targetUser ? `${targetUser.name} さんの${title}` : title}
            </h1>
            <button type="button" onClick={onRefresh} disabled={loading}>更新</button>
          </div>

          <p>合計: <span>{count}</span> 人</p>
          {loading && <p>一覧を読み込み中です...</p>}
          {!loading && users.length === 0 && <p>{emptyMessage}</p>}

          <ul className="follow-list" aria-live="polite" aria-busy={loading}>
            {users.map(rowUser => (
              <li key={rowUser.id} className="follow-list-row">
                <a href={`${contextPath}/Profile?userId=${rowUser.id}`}>{rowUser.name}</a>
                {!rowUser.me && (
                  <button
                    type="button"
                    disabled={!sessionUser}
                    onClick={() => onFollowClick(rowUser)}
                    title={!sessionUser
                      ? "ログイン情報を確認中です"
                      : rowUser.followedByMe ? "クリックするとフォローを解除します" : "クリックするとフォローします"}
                  >
                    {rowUser.followedByMe ? "フォロー済" : "フォロー"}
                  </button>
                )}
              </li>
            ))}
          </ul>
        </section>
      </main>
      <footer><p>つぶやきアプリ</p></footer>
    </>
  );
}

export default function FollowList({ contextPath, sessionUser, userId, type }) {
  const normalizedType = normalizeType(type);
  const [targetUser, setTargetUser] = useState(null);
  const [users, setUsers] = useState([]);
  const [count, setCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  /**
   * 初期表示時に一覧を取得する。
   *
   * JSP時代はServletが属性として一覧を渡していたが、React化後はAPIレスポンスを唯一の描画元にする。
   */
  useEffect(() => {
    let active = true;

    async function load() {
      setLoading(true);
      try {
        const result = await fetchFollowList({ userId, type: normalizedType });
        if (!active) return;
        setTargetUser(result.targetUser);
        setUsers(result.users);
        setCount(Number(result.count));
        setMessage(null);
      } catch (error) {
        if (active) setMessage({ text: error.message, error: true });
      } finally {
        if (active) setLoading(false);
      }
    }

    load();
    return () => {
      active = false;
    };
  }, [userId, normalizedType]);

  /**
   * 行ごとのフォロー状態を切り替える。
   *
   * 自分のフォロー中一覧でフォロー解除した場合は、旧JSP画面と同じくその行を一覧から外す。
   */
  async function handleFollowClick(rowUser) {
    if (rowUser.followedByMe && !window.confirm("フォローを解除しますか？")) {
      return;
    }

    try {
      const result = await followUser(rowUser.id);
      const nextFollowing = Boolean(result.following);

      if (normalizedType === "following" && targetUser?.id === sessionUser?.id && !nextFollowing) {
        setUsers(current => current.filter(user => user.id !== rowUser.id));
        setCount(current => Math.max(0, current - 1));
      } else {
        setUsers(current => current.map(user =>
          user.id === rowUser.id ? { ...user, followedByMe: nextFollowing } : user
        ));
      }

      setMessage({
        text: nextFollowing ? "フォローしました。" : "フォローを解除しました。",
        error: false
      });
    } catch (error) {
      setMessage({ text: error.message || "フォロー処理に失敗しました。", error: true });
    }
  }

  return (
    <FollowListView
      contextPath={contextPath}
      sessionUser={sessionUser}
      userId={userId}
      type={normalizedType}
      targetUser={targetUser}
      users={users}
      count={count}
      loading={loading}
      message={message}
      onRefresh={() => window.location.reload()}
      onFollowClick={handleFollowClick}
    />
  );
}
