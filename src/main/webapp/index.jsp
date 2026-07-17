<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
  ログイン画面のReactホスト。

  Phase12で、JSPフォーム描画をReactへ移行した。認証処理そのものはSpring Securityの
  `/Login` フォーム認証を維持し、このJSPはcontextPath、CSRFトークン、ログイン結果メッセージを
  Reactへ渡す入口に縮小する。
--%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>どこつぶ</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/react/assets/main.css">
</head>
<body data-context-path="${pageContext.request.contextPath}"
      data-react-page="login"
      data-csrf-token="${_csrf.token}"
      data-csrf-header="${_csrf.headerName}"
      data-csrf-param="${_csrf.parameterName}"
      data-login-error="${param.error eq '1'}"
      data-logged-out="${param.logout eq '1'}">
    <div id="root"><p>ログイン画面を読み込み中です...</p></div>
    <noscript>この画面を利用するにはJavaScriptを有効にしてください。</noscript>
    <script type="module" src="${pageContext.request.contextPath}/react/assets/main.js"></script>
</body>
</html>
