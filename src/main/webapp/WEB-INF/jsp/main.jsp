<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>どこつぶ</title>
</head>
<body>
	<h1>どこつぶメイン</h1>
	<p>
	
		<c:out value="${loginUser.id}" />
		<c:out value="${loginUser.name}" />
		さん、ログイン中です 更新




	</p>

	<a href="Logout">ログアウト</a>
	<br>

	<%--postは投稿で使うもの。検索はデータとってくるものなのでgetにしている --%>
	<form action="SearchMutter" method="get">
		<input type="text" name="keyword" placeholder="検索キーワード"> <input
			type="submit" value="検索">
	</form>


	<p>
		<a href="Main">更新</a>
	</p>
	<form action="Main" method="post">
		<input type="text" name="text" placeholder="つぶやきを入力してください"> <input
			type="submit" value="つぶやく">
	</form>




	<c:if test="${not empty errorMsg}">
		<p>
			<c:out value="${errorMsg}" />
		</p>
	</c:if>


	<c:forEach var="mutter" items="${mutterList}">
		<p>
			<c:out value="${mutter.userName}" />
			：
			<c:out value="${mutter.text}" />

			<%--削除ボタン追加 --%>

			<c:if test="${mutter.userId == loginUser.id}">
				<form action="DeleteMutter" method="post" style="display:inline;">
					<input type="hidden" name="mutterId" value="${mutter.id}">
					<input type="submit" value="削除">
				</form>
			</c:if>




		</p>
	</c:forEach>


	<jsp:include page="footer.jsp" />

</body>
</html>