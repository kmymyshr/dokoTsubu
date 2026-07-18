/**
 * ログイン画面を表示するReactコンポーネント。
 *
 * Phase12で旧index.jspのフォーム描画を置き換え、Phase22でJSPファイル自体も廃止した。認証処理はSpring Securityの
 * `/Login` フォーム認証をそのまま使うため、このコンポーネントは入力フォーム、
 * CSRF hidden項目、失敗/ログアウト後メッセージの表示だけを担当する。
 */
export default function LoginPage({
  contextPath,
  csrfToken,
  csrfParam,
  loginError,
  loggedOut
}) {
  const loginAction = `${contextPath}/Login`;

  return (
    <main>
      <section className="profile-card" aria-labelledby="loginHeading">
        <h1 id="loginHeading">つぶやきアプリへようこそ!</h1>

        {loginError && (
          <p className="message error" role="alert">
            パスワードが間違っているか、ユーザーが未登録です。
          </p>
        )}
        {loggedOut && (
          <p className="message" role="status">
            ログアウトしました。
          </p>
        )}

        <form action={loginAction} method="post">
          <input type="hidden" name={csrfParam} value={csrfToken} />
          <div className="form-field">
            <label htmlFor="loginName">ユーザー名</label>
            <input
              id="loginName"
              type="text"
              name="name"
              autoComplete="username"
              required
            />
          </div>
          <div className="form-field">
            <label htmlFor="loginPass">パスワード</label>
            <input
              id="loginPass"
              type="password"
              name="pass"
              autoComplete="current-password"
              required
            />
          </div>
          <button type="submit">ログイン</button>
        </form>

        <hr />
        <p>ユーザー登録は<a href={`${contextPath}/Register`}>こちら</a></p>
      </section>
    </main>
  );
}
