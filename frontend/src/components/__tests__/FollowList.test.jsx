import { describe, expect, it } from "vitest";
import { renderToStaticMarkup } from "react-dom/server";
import { FollowListView } from "../FollowList.jsx";

describe("FollowListView", () => {
  it("フォロー一覧APIのレスポンスを一覧として表示する", () => {
    const html = renderToStaticMarkup(
      <FollowListView
        contextPath="/dokoTsubu"
        sessionUser={{ id: 7, name: "alice", csrfToken: "csrf-value" }}
        userId={7}
        type="following"
        targetUser={{ id: 7, name: "alice" }}
        users={[
          { id: 8, name: "bob", followedByMe: true, me: false },
          { id: 9, name: "charlie", followedByMe: false, me: false },
          { id: 7, name: "alice", followedByMe: false, me: true }
        ]}
        count={3}
        loading={false}
        message={null}
        onRefresh={() => {}}
        onFollowClick={() => {}}
      />
    );

    expect(html).toContain("alice さんのフォロー中");
    expect(html).toContain("合計:");
    expect(html).toContain(">3</span>");
    expect(html).toContain("/dokoTsubu/Profile?userId=8");
    expect(html).toContain("bob");
    expect(html).toContain("charlie");
    expect((html.match(/>フォロー済<\/button>/g) ?? [])).toHaveLength(1);
    expect((html.match(/>フォロー<\/button>/g) ?? [])).toHaveLength(1);
  });

  it("フォロワーが0件の場合は空メッセージを表示する", () => {
    const html = renderToStaticMarkup(
      <FollowListView
        contextPath=""
        sessionUser={{ id: 7, name: "alice" }}
        userId={7}
        type="followers"
        targetUser={{ id: 7, name: "alice" }}
        users={[]}
        count={0}
        loading={false}
        message={null}
        onRefresh={() => {}}
        onFollowClick={() => {}}
      />
    );

    expect(html).toContain("alice さんのフォロワー");
    expect(html).toContain("まだフォロワーはいません。");
  });
});
