import { describe, expect, it } from "vitest";
import { renderToStaticMarkup } from "react-dom/server";
import { ProfileView } from "../ProfilePage.jsx";

describe("ProfileView", () => {
  it("本人プロフィールでは自己紹介編集フォームを表示する", () => {
    const html = renderToStaticMarkup(
      <ProfileView
        contextPath="/dokoTsubu"
        sessionUser={{ id: 7, name: "alice" }}
        profile={{
          user: { id: 7, name: "alice", bio: "こんにちは" },
          ownProfile: true,
          following: false,
          followers: 2,
          followingCount: 3
        }}
        bio="こんにちは"
        loading={false}
        saving={false}
        message={null}
        onBioChange={() => {}}
        onSave={() => {}}
        onFollowClick={() => {}}
      />
    );

    expect(html).toContain("alice");
    expect(html).toContain("ユーザーID: 7");
    expect(html).toContain("/dokoTsubu/FollowerList?userId=7");
    expect(html).toContain("ひとこと自己紹介");
    expect(html).toContain("こんにちは");
    expect(html).toContain("自己紹介を編集");
    expect(html).toContain(">保存</button>");
  });

  it("他ユーザーのプロフィールではフォローボタンを表示する", () => {
    const html = renderToStaticMarkup(
      <ProfileView
        contextPath=""
        sessionUser={{ id: 7, name: "alice" }}
        profile={{
          user: { id: 8, name: "bob", bio: "" },
          ownProfile: false,
          following: true,
          followers: 1,
          followingCount: 0
        }}
        bio=""
        loading={false}
        saving={false}
        message={null}
        onBioChange={() => {}}
        onSave={() => {}}
        onFollowClick={() => {}}
      />
    );

    expect(html).toContain("bob");
    expect(html).toContain("まだ自己紹介はありません。");
    expect(html).toContain(">フォロー中</button>");
  });
});
