import { useState } from "react";
import { likeMutter, followUser } from "../api";

function formatDate(value) {
  const valueWithTimezone = value && !/[zZ]|[+-]\d{2}:\d{2}$/.test(value)
    ? `${value}Z`
    : value;
  const date = new Date(valueWithTimezone);
  return Number.isNaN(date.getTime())
    ? ""
    : date.toLocaleString("ja-JP", { timeZone: "Asia/Tokyo" });
}

function MutterCard({
  mutter,
  user,
  contextPath,
  editable,
  followed,
  onFollowChange,
  onEdit,
  onDelete
}) {
  const [likeCount, setLikeCount] = useState(mutter.likeCount ?? 0);
  const [liked, setLiked] = useState(mutter.likedByMe ?? false);

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
      const nextFollowing = Boolean(res.following);
      onFollowChange?.(mutter.userId, nextFollowing);
      alert((nextFollowing ? "フォローしました" : "フォローを解除しました") + "（フォロワー: " + res.followers + "）");
    } catch (e) {
      alert(e.message || "フォローに失敗しました");
    }
  }

  async function handleFollowClick() {
    if (followed && !window.confirm("フォローを解除しますか？")) {
      return;
    }
    await handleFollow();
  }

  return (
    <article className="mutter-card">
      <div className="mutter-meta">
        <div style={{ display: "flex", gap: "8px", alignItems: "center", flexWrap: "wrap" }}>
          <strong>
            <a href={`${contextPath}/Profile?userId=${mutter.userId}`}>{mutter.userName}</a>
          </strong>
          {user?.id !== mutter.userId && (
            <button
              type="button"
              onClick={handleFollowClick}
              title={followed ? "クリックするとフォローを解除します" : "クリックするとフォローします"}
              style={followed ? { backgroundColor: "#dbeafe", color: "#1d4ed8", borderColor: "#60a5fa" } : undefined}
            >
              {followed ? "フォロー済" : "フォロー"}
            </button>
          )}
        </div>
        <time dateTime={mutter.createdAt}>{formatDate(mutter.createdAt)}</time>
      </div>
      <p className="mutter-text">{mutter.text}</p>
      <div className="mutter-actions">
        <button
          type="button"
          onClick={handleLike}
          disabled={user?.id === mutter.userId}
          title={user?.id === mutter.userId ? "自分の投稿にはいいねできません" : "いいね"}
        >
          {liked ? "♥" : "♡"} {likeCount}
        </button>
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

export default function MutterList({
  mutters,
  user,
  contextPath,
  followStateByUserId,
  onFollowChange,
  loading,
  hasNext,
  onRefresh,
  onLoadMore,
  onEdit,
  onDelete
}) {
  return (
    <section aria-labelledby="listHeading">
      <div className="section-heading">
        <h2 id="listHeading">つぶやき一覧</h2>
        <button type="button" disabled={loading} onClick={onRefresh}>更新</button>
      </div>
      <div aria-live="polite" aria-busy={loading}>
        {mutters.map(mutter => (
          <MutterCard
            key={mutter.id}
            mutter={mutter}
            user={user}
            contextPath={contextPath}
            editable={user?.id === mutter.userId}
            followed={followStateByUserId[mutter.userId] ?? Boolean(mutter.followedByMe)}
            onFollowChange={onFollowChange}
            onEdit={onEdit}
            onDelete={onDelete}
          />
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
