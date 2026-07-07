function formatDate(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "" : date.toLocaleString("ja-JP");
}

function MutterCard({ mutter, editable, onEdit, onDelete }) {
  return (
    <article className="mutter-card">
      <div className="mutter-meta">
        <strong>{mutter.userName}</strong>
        <time dateTime={mutter.createdAt}>{formatDate(mutter.createdAt)}</time>
      </div>
      <p className="mutter-text">{mutter.text}</p>
      {editable && (
        <div className="mutter-actions">
          <button type="button" onClick={() => onEdit(mutter)}>編集</button>
          <button type="button" onClick={() => onDelete(mutter)}>削除</button>
        </div>
      )}
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
          <MutterCard key={mutter.id} mutter={mutter}
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
