<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%--
  Phase6で、メイン画面はJSPで一覧HTMLを生成せず、Reactアプリを読み込むホストにする。
  contextPathはReact側のAPI呼び出し・画面遷移で必要なため、bodyのdata属性として渡す。
--%>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>dokoTsubu</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/react/assets/main.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">
    <div id="root"><p>画面を読み込み中です...</p></div>
    <noscript>この画面を利用するにはJavaScriptを有効にしてください。</noscript>
    <script type="module" src="${pageContext.request.contextPath}/react/assets/main.js"></script>
</body>
</html>
