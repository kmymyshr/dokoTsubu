import { useState } from "react";
import { likeMutter, followUser } from "../api";

function formatDate(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "" : date.toLocaleString("ja-JP");
}

function MutterCard({ mutter, user, editable, onEdit, onDelete }) {
  const [likeCount, setLikeCount] = useState(mutter.likeCount ?? 0);
  const [liked, setLiked] = useState(mutter.likedByMe ?? false);
  const [followed, setFollowed] = useState(mutter.followedByMe ?? false);

  async function handleLike() {
    try {
      const res = await likeMutter(mutter.id);
      setLiked(Boolean(res.liked));
      setLikeCount(Number(res.count));
    } catch (e) {
      alert(e.message || "いいねに失敗しました");
    }
  }

  async function handleFollow() {
    if (!user) return;
    try {
      const res = await followUser(mutter.userId);
      // res.following と res.followers などが返る（既存サーブレットではfollowing/followers）
      setFollowed(Boolean(res.following));
      alert((res.following ? "フォローしました" : "フォローを解除しました") + "（フォロワー: " + res.followers + "）");
    } catch (e) {
      alert(e.message || "フォローに失敗しました");
    }
  }

  return (
    <article className="mutter-card">
      <div className="mutter-meta">
        <strong>{mutter.userName}</strong>
        <time dateTime={mutter.createdAt}>{formatDate(mutter.createdAt)}</time>
      </div>
      <p className="mutter-text">{mutter.text}</p>
      <div className="mutter-actions">
        <button type="button" onClick={handleLike}>{liked ? "♥" : "♡"} {likeCount}</button>
        {user?.id !== mutter.userId && (
          <button type="button" onClick={handleFollow}>{followed ? "フォロー中" : "フォロー"}</button>
        )}
        {editable && (
          <>
            <button type="button" onClick={() => onEdit(mutter)}>編集</button>
            <button type="button" onClick={() => onDelete(mutter)}>削除</button>
          </>
        )}
      </div>
    </article>
  );
}

export default function MutterList({ mutters, user, loading, hasNext, onRefresh, onLoadMore,
                                     onEdit, onDelete }) {
  return (
    <section aria-labelledby="listHeading">
      <div className="section-heading">
        <h2 id="listHeading">つぶやき一覧</h2>
        <button type="button" disabled={loading} onClick={onRefresh}>更新</button>
      </div>
      <div aria-live="polite" aria-busy={loading}>
        {mutters.map(mutter => (
          <MutterCard key={mutter.id} mutter={mutter} user={user}
                      editable={user?.id === mutter.userId}
                      onEdit={onEdit} onDelete={onDelete} />
        ))}
      </div>
      {!loading && mutters.length === 0 && <p>つぶやきがありません。</p>}
      {hasNext && (
        <button className="load-more" type="button" disabled={loading} onClick={onLoadMore}>
          さらに読み込む
        </button>
      )}
    </section>
  );
}
