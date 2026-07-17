<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%--
  プロフィール画面のReactホスト。

  Phase11で、JSPによるプロフィール描画と自己紹介フォームをReact + `/api/profile` に移行した。
  JSPはcontextPath、対象ユーザーID、CSRFトークンをReactへ渡し、表示・更新・フォロー操作はReactが担当する。
--%>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>プロフィール</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/react/assets/main.css">
</head>
<body data-context-path="${pageContext.request.contextPath}"
      data-react-page="profile"
      data-target-user-id="${targetUserId}"
      data-csrf-token="${_csrf.token}"
      data-csrf-header="${_csrf.headerName}">
    <div id="root"><p>プロフィールを読み込み中です...</p></div>
    <noscript>この画面を利用するにはJavaScriptを有効にしてください。</noscript>
    <script type="module" src="${pageContext.request.contextPath}/react/assets/main.js"></script>
</body>
</html>
