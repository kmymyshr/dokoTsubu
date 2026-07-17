import { describe, expect, it } from "vitest";
import { renderToStaticMarkup } from "react-dom/server";
import Header from "../Header.jsx";

describe("Header", () => {
  it("ログアウトをCSRFトークン付きPOSTフォームとして表示する", () => {
    const html = renderToStaticMarkup(
      <Header
        user={{ id: 7, name: "alice", csrfToken: "csrf-value" }}
        contextPath="/dokoTsubu"
      />
    );

    expect(html).toContain("alice さん、ログイン中です");
    expect(html).toContain("マイページ");
    expect(html).toContain("ログアウト");
    expect(html).toContain('action="/dokoTsubu/Logout"');
    expect(html).toContain('method="post"');
    expect(html).toContain('name="_csrf"');
    expect(html).toContain('value="csrf-value"');
  });
});
