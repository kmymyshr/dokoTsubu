/**
 * 投稿作成フォーム。
 *
 * Phase6で旧JSPフォームからReactフォームへ移した部分。送信時は画面遷移せず、
 * Appから渡されたonSubmit経由でREST APIへ投稿する。
 */
import { useState } from "react";

export default function PostForm({ onSubmit, disabled }) {
  const [text, setText] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    const normalized = text.trim();
    if (!normalized) return;
    if (await onSubmit(normalized)) setText("");
  }

  return (
    <section aria-labelledby="postHeading">
      <h2 id="postHeading">つぶやく</h2>
      <form onSubmit={handleSubmit}>
        <label htmlFor="postText">本文</label>
        <div className="form-row">
          <input id="postText" maxLength="255" required autoComplete="off"
                 placeholder="つぶやきを入力してください"
                 value={text} onChange={event => setText(event.target.value)} />
          <button type="submit" disabled={disabled}>投稿</button>
        </div>
      </form>
    </section>
  );
}
