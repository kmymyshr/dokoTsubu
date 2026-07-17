/**
 * 投稿編集ダイアログ。
 *
 * 旧JSPの編集画面へ遷移せず、React画面上で編集できるようにするためのコンポーネント。
 * 保存時には投稿ID・本文・versionをAppへ渡し、REST APIで楽観ロック付き更新を行う。
 */
import { useEffect, useState } from "react";

export default function EditDialog({ mutter, saving, onCancel, onSave }) {
  const [text, setText] = useState("");
  useEffect(() => setText(mutter?.text ?? ""), [mutter]);
  if (!mutter) return null;

  function submit(event) {
    event.preventDefault();
    const normalized = text.trim();
    if (normalized) onSave(mutter.id, normalized, mutter.version);
  }

  return (
    <div className="dialog-backdrop" role="presentation">
      <section className="edit-dialog" role="dialog" aria-modal="true" aria-labelledby="editHeading">
        <form onSubmit={submit}>
          <h2 id="editHeading">つぶやきを編集</h2>
          <label htmlFor="editText">本文</label>
          <input id="editText" autoFocus maxLength="255" required
                 value={text} onChange={event => setText(event.target.value)} />
          <div className="dialog-actions">
            <button type="button" disabled={saving} onClick={onCancel}>キャンセル</button>
            <button type="submit" disabled={saving}>更新</button>
          </div>
        </form>
      </section>
    </div>
  );
}
