import { useState } from "react";

export default function SearchForm({ onSearch, disabled }) {
  const [keyword, setKeyword] = useState("");

  function submit(event) {
    event.preventDefault();
    onSearch(keyword.trim());
  }

  function clear() {
    setKeyword("");
    onSearch("");
  }

  return (
    <section aria-labelledby="searchHeading">
      <h2 id="searchHeading">検索</h2>
      <form className="form-row" onSubmit={submit}>
        <label className="visually-hidden" htmlFor="keyword">検索キーワード</label>
        <input id="keyword" maxLength="100" placeholder="検索キーワード"
               value={keyword} onChange={event => setKeyword(event.target.value)} />
        <button type="submit" disabled={disabled}>検索</button>
        <button type="button" disabled={disabled} onClick={clear}>解除</button>
      </form>
    </section>
  );
}
