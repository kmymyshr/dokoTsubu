<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>つぶやき編集</title>
</head>
<body>
	<h1>つぶやき編集</h1>
	<form action="${pageContext.request.contextPath}/UpdateMutter" method="post">
		<input type="hidden" name="mutterId" value="<c:out value='${mutter.id}' />">
		<input type="hidden" name="version" value="<c:out value='${mutter.version}' />">
		<input type="hidden" name="${_csrf.parameterName}" value="<c:out value='${_csrf.token}' />">
		<input type="text" name="text" value="<c:out value='${mutter.text}' />" required>
		<input type="submit" value="更新">
	</form>
	<p><a href="${pageContext.request.contextPath}/Main">メイン画面に戻る</a></p>
</body>
</html>
