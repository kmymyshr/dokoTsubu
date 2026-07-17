<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
  フォロワー一覧のReactホスト。

  Phase9で一覧HTMLの生成をJSPからReactへ移した。
  JSPはcontextPath、画面種別、対象ユーザーIDだけをbodyのdata属性で渡し、
  実際の一覧取得・表示・フォロー切り替えはReactコンポーネントが担当する。
--%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>フォロワー一覧</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/react/assets/main.css">
</head>
<body data-context-path="${pageContext.request.contextPath}"
      data-react-page="follow-list"
      data-follow-list-type="followers"
      data-target-user-id="${targetUser.id}">
    <div id="root"><p>フォロワー一覧を読み込み中です...</p></div>
    <noscript>この画面を利用するにはJavaScriptを有効にしてください。</noscript>
    <script type="module" src="${pageContext.request.contextPath}/react/assets/main.js"></script>
</body>
</html>
