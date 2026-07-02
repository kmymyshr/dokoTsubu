//ユーザ登録画面を出力するビュー。リンクをふんで遷移する先。

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ユーザ登録</title>
</head>
<body>
<h1>ユーザ登録</h1>

<form action="Register" method="post">
    ユーザー名：<input type="text" name="name" value="${param.name}"><br>
    パスワード：<input type="password" name="pass"><br>
    <input type="submit" value="登録">
</form>

<c:if test="${not empty errorMsg}">
    <p style="color:red">${errorMsg}</p>
</c:if>

<p><a href="index.jsp">ログイン画面へ戻る</a></p>
