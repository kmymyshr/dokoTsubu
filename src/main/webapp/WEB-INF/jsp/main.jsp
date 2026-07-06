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
		<small>（<c:out value="${mutter.createdAt}" />）</small>

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

<c:if test="${not searchMode}">
    <button id="loadMoreButton" type="button" style="display:none;">さらに読み込む</button>
</c:if>

<jsp:include page="footer.jsp"/>

<script>
const pageLimit = 20;
let nextCursor = null;
let hasNext = false;
let olderPagesLoaded = false;

async function fetchMutterPage(cursor) {
    const url = new URL("${pageContext.request.contextPath}/MutterList", window.location.origin);
    url.searchParams.set("limit", pageLimit);
    if (cursor !== null) {
        url.searchParams.set("cursor", cursor);
    }

    const response = await fetch(url);
    if (!response.ok) {
        throw new Error("通信に失敗しました: " + response.status);
    }
    return response.json();
}

function createMutterElement(mutter, loginUserId) {
    const p = document.createElement("p");
    const createdAt = new Date(mutter.createdAt).toLocaleString("ja-JP");
    p.appendChild(document.createTextNode(
        mutter.userName + "：" + mutter.text + " （" + createdAt + "） "
    ));

    if (mutter.userId === loginUserId) {
        const updateForm = document.createElement("form");
        updateForm.action = "${pageContext.request.contextPath}/UpdateMutter";
        updateForm.method = "get";
        updateForm.style.display = "inline";

        const updateHidden = document.createElement("input");
        updateHidden.type = "hidden";
        updateHidden.name = "mutterId";
        updateHidden.value = mutter.id;

        const updateButton = document.createElement("input");
        updateButton.type = "submit";
        updateButton.value = "編集";

        updateForm.appendChild(updateHidden);
        updateForm.appendChild(updateButton);

        const deleteForm = document.createElement("form");
        deleteForm.action = "${pageContext.request.contextPath}/DeleteMutter";
        deleteForm.method = "post";
        deleteForm.style.display = "inline";

        const deleteHidden = document.createElement("input");
        deleteHidden.type = "hidden";
        deleteHidden.name = "mutterId";
        deleteHidden.value = mutter.id;

        const deleteButton = document.createElement("input");
        deleteButton.type = "submit";
        deleteButton.value = "削除";

        deleteForm.appendChild(deleteHidden);
        deleteForm.appendChild(deleteButton);
        p.appendChild(updateForm);
        p.appendChild(deleteForm);
    }

    return p;
}

function renderMutterPage(mutters, append) {
    const area = document.getElementById("mutterList");
    const loginUserId = <c:out value="${loginUser.id}" />;
    if (!append) {
        area.textContent = "";
    }
    mutters.forEach(mutter => area.appendChild(createMutterElement(mutter, loginUserId)));
}

function updateLoadMoreButton() {
    const button = document.getElementById("loadMoreButton");
    if (button) {
        button.style.display = hasNext ? "inline-block" : "none";
        button.disabled = false;
    }
}

async function loadLatestMutterList() {
    try {
        const page = await fetchMutterPage(null);
        renderMutterPage(page.mutters, false);
        nextCursor = page.nextCursor;
        hasNext = page.hasNext;
        updateLoadMoreButton();
    } catch (e) {
        console.error(e);
    }
}

<c:if test="${not searchMode}">
document.getElementById("loadMoreButton").addEventListener("click", async event => {
    if (!hasNext || nextCursor === null) {
        return;
    }

    event.currentTarget.disabled = true;
    try {
        const page = await fetchMutterPage(nextCursor);
        renderMutterPage(page.mutters, true);
        nextCursor = page.nextCursor;
        hasNext = page.hasNext;
        olderPagesLoaded = true;
        updateLoadMoreButton();
    } catch (e) {
        console.error(e);
        event.currentTarget.disabled = false;
    }
});

loadLatestMutterList();
setInterval(() => {
    if (!olderPagesLoaded) {
        loadLatestMutterList();
    }
}, 5000);
</c:if>
</script>

</body>
</html>
