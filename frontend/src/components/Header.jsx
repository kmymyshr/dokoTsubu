export default function Header({ user, contextPath }) {
  return (
    <header className="page-header">
      <div>
        <h1>どこつぶ</h1>
        <p>{user ? `${user.name} さん、ログイン中です` : "ログイン情報を読み込み中です"}</p>
      </div>
      <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
        {user && <a href={`${contextPath}/Profile?userId=${user.id}`}>マイページ</a>}
        <a href={`${contextPath}/Logout`}>ログアウト</a>
      </div>
    </header>
  );
}
