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
    さん、ログイン中です
</p>

<a href="Logout">ログアウト</a>

<br><br>

<form action="SearchMutter" method="get">
    <input type="text"
           name="keyword"
           placeholder="検索キーワード">

    <input type="submit"
           value="検索">
</form>

<p>
    <a href="Main">更新</a>
</p>

<form action="Main" method="post">
    <input type="text"
           name="text"
           placeholder="つぶやきを入力してください">

    <input type="submit"
           value="つぶやく">
</form>

<c:if test="${not empty errorMsg}">
    <p>
        <c:out value="${errorMsg}" />
    </p>
</c:if>

<hr>

<div id="mutterList">

<c:forEach var="mutter" items="${mutterList}">

    <p>

        <c:out value="${mutter.userName}" />
        ：
        <c:out value="${mutter.text}" />

        <c:if test="${mutter.userId == loginUser.id}">

            <form action="UpdateMutter"
                  method="get"
                  style="display:inline;">

                <input type="hidden"
                       name="mutterId"
                       value="${mutter.id}">

                <input type="submit"
                       value="編集">

            </form>

            <form action="DeleteMutter"
                  method="post"
                  style="display:inline;">

                <input type="hidden"
                       name="mutterId"
                       value="${mutter.id}">

                <input type="submit"
                       value="削除">

            </form>

        </c:if>

    </p>

</c:forEach>

</div>

<jsp:include page="footer.jsp"/>

<script>
async function loadMutterList() {
    const response = await fetch("MutterList");
    const list = await response.json();

    const area = document.getElementById("mutterList");
    area.textContent = "";

    list.forEach(mutter => {
        const p = document.createElement("p");

        const text = document.createTextNode(
            mutter.userName + "：" + mutter.text + " "
        );

        p.appendChild(text);

        area.appendChild(p);
    });
}

loadMutterList();
</script>

</body>
</html>