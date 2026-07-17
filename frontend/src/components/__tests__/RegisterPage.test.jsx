import { describe, expect, it } from "vitest";
import { renderToStaticMarkup } from "react-dom/server";
import RegisterPage from "../RegisterPage.jsx";

describe("RegisterPage", () => {
  it("登録フォームとログイン画面への導線を表示する", () => {
    const html = renderToStaticMarkup(<RegisterPage contextPath="/dokoTsubu" />);

    expect(html).toContain("ユーザー登録");
    expect(html).toContain("ユーザー名");
    expect(html).toContain("パスワード");
    expect(html).toContain(">登録</button>");
    expect(html).toContain("/dokoTsubu/");
  });
});
