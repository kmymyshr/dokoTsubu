<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
  ユーザー登録画面のReactホスト。

  Phase10で、JSPフォームによる登録からReact + `/api/register` に移行した。
  JSPはcontextPathとCSRFトークンをReactへ渡し、実際の入力・送信・完了表示はReactが担当する。
--%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>ユーザー登録</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/react/assets/main.css">
</head>
<body data-context-path="${pageContext.request.contextPath}"
      data-react-page="register"
      data-csrf-token="${_csrf.token}"
      data-csrf-header="${_csrf.headerName}">
    <div id="root"><p>登録画面を読み込み中です...</p></div>
    <noscript>この画面を利用するにはJavaScriptを有効にしてください。</noscript>
    <script type="module" src="${pageContext.request.contextPath}/react/assets/main.js"></script>
</body>
</html>
