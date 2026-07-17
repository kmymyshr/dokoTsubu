/**
 * ユーザー登録画面のReactコンポーネント。
 *
 * Phase10で旧JSPフォームを置き換え、入力・送信・完了表示をReactへ移した。
 * 登録処理そのものは `/api/register` へ委譲し、Reactは画面状態とユーザーへの案内を担当する。
 */
import { useState } from "react";
import { registerUser } from "../api.js";

export default function RegisterPage({ contextPath }) {
  const [name, setName] = useState("");
  const [pass, setPass] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState(null);
  const [registered, setRegistered] = useState(null);

  /** 入力値をJSON APIへ送り、成功時は完了表示へ切り替える。 */
  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setMessage(null);

    try {
      const result = await registerUser({ name, pass });
      setRegistered(result);
      setName("");
      setPass("");
    } catch (error) {
      setMessage({ text: error.message || "登録に失敗しました。", error: true });
    } finally {
      setSubmitting(false);
    }
  }

  const loginUrl = registered?.loginUrl || `${contextPath}/`;

  return (
    <main>
      <section className="profile-card" aria-labelledby="registerHeading">
        <h1 id="registerHeading">ユーザー登録</h1>

        {registered ? (
          <div>
            <p className="message" role="status">
              {registered.message}（{registered.name}）
            </p>
            <p><a href={loginUrl}>ログイン画面へ戻る</a></p>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="form-field">
              <label htmlFor="registerName">ユーザー名</label>
              <input
                id="registerName"
                type="text"
                value={name}
                onChange={event => setName(event.target.value)}
                autoComplete="username"
                required
              />
            </div>
            <div className="form-field">
              <label htmlFor="registerPass">パスワード</label>
              <input
                id="registerPass"
                type="password"
                value={pass}
                onChange={event => setPass(event.target.value)}
                autoComplete="new-password"
                required
              />
            </div>
            <button type="submit" disabled={submitting}>
              {submitting ? "登録中..." : "登録"}
            </button>
          </form>
        )}

        {message && <p className={`message${message.error ? " error" : ""}`} role="alert">{message.text}</p>}
        {!registered && <p><a href={`${contextPath}/`}>ログイン画面へ戻る</a></p>}
      </section>
    </main>
  );
}
