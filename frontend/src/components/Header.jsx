export default function Header({ user, contextPath }) {
  return (
    <header className="page-header">
      <div>
        <h1>どこつぶ</h1>
        <p>{user ? `${user.id} ${user.name} さん、ログイン中です` : "ログイン情報を読み込み中です"}</p>
      </div>
      <a href={`${contextPath}/Logout`}>ログアウト</a>
    </header>
  );
}
