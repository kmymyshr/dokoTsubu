import { describe, expect, it } from "vitest";
import { renderToStaticMarkup } from "react-dom/server";
import MutterList from "../MutterList.jsx";

describe("MutterList", () => {
  it("本人の投稿にだけ編集・削除ボタンを表示する", () => {
    const html = renderToStaticMarkup(
      <MutterList
        mutters={[
          { id: 1, userId: 7, userName: "alice", text: "自分の投稿", version: 0,
            createdAt: "2026-07-07T09:00:00" },
          { id: 2, userId: 8, userName: "bob", text: "他人の投稿", version: 0,
            createdAt: "2026-07-07T09:01:00" }
        ]}
        user={{ id: 7, name: "alice" }}
        loading={false}
        hasNext={false}
        onRefresh={() => {}}
        onLoadMore={() => {}}
        onEdit={() => {}}
        onDelete={() => {}}
      />
    );

    expect(html).toContain("自分の投稿");
    expect(html).toContain("他人の投稿");
    expect((html.match(/>編集<\/button>/g) ?? [])).toHaveLength(1);
    expect((html.match(/>削除<\/button>/g) ?? [])).toHaveLength(1);
  });
});
