/**
 * Reactメイン画面のヘッダー。
 *
 * ログインユーザー名、プロフィールへの導線、Spring SecurityのPOSTログアウトフォームを表示する。
 * ログアウトはCSRFトークン付きPOSTが必要なため、リンクではなくフォームとして残している。
 */
export default function Header({ user, contextPath }) {
  return (
    <header className="page-header">
      <div>
        <h1>つぶやきアプリ</h1>
        <p>{user ? `${user.name} さん、ログイン中です` : "ログイン情報を読み込み中です"}</p>
      </div>
      <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
        {user && <a href={`${contextPath}/Profile?userId=${user.id}`}>マイページ</a>}
        {user && (
          <form action={`${contextPath}/Logout`} method="post">
            <input type="hidden" name="_csrf" value={user.csrfToken} />
            <button type="submit">ログアウト</button>
          </form>
        )}
      </div>
    </header>
  );
}
