import { describe, expect, it } from "vitest";
import { renderToStaticMarkup } from "react-dom/server";
import LoginPage from "../LoginPage.jsx";

describe("LoginPage", () => {
  it("Spring Securityのフォーム認証へ送信するログインフォームを表示する", () => {
    const html = renderToStaticMarkup(
      <LoginPage
        contextPath="/dokoTsubu"
        csrfToken="csrf-value"
        csrfParam="_csrf"
        loginError={false}
        loggedOut={false}
      />
    );

    expect(html).toContain("つぶやきアプリへようこそ!");
    expect(html).toContain('action="/dokoTsubu/Login"');
    expect(html).toContain('name="_csrf"');
    expect(html).toContain('value="csrf-value"');
    expect(html).toContain('name="name"');
    expect(html).toContain('name="pass"');
    expect(html).toContain("/dokoTsubu/Register");
  });

  it("ログイン失敗とログアウト後のメッセージを表示する", () => {
    const html = renderToStaticMarkup(
      <LoginPage
        contextPath=""
        csrfToken="csrf-value"
        csrfParam="_csrf"
        loginError={true}
        loggedOut={true}
      />
    );

    expect(html).toContain("パスワードが間違っているか、ユーザーが未登録です。");
    expect(html).toContain("ログアウトしました。");
  });
});
