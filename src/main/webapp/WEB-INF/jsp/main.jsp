<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>どこつぶ</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">
<header class="page-header">
    <div>
        <h1>どこつぶ</h1>
        <p id="loginStatus">ログイン情報を読み込み中です</p>
    </div>
    <a href="${pageContext.request.contextPath}/Logout">ログアウト</a>
</header>

<main>
    <p id="message" class="message" role="status" aria-live="polite" hidden></p>

    <section aria-labelledby="postHeading">
        <h2 id="postHeading">つぶやく</h2>
        <form id="postForm">
            <label for="postText">本文</label>
            <div class="form-row">
                <input id="postText" name="text" maxlength="255" required
                       placeholder="つぶやきを入力してください" autocomplete="off">
                <button type="submit">投稿</button>
            </div>
        </form>
    </section>

    <section aria-labelledby="searchHeading">
        <h2 id="searchHeading">検索</h2>
        <form id="searchForm" class="form-row">
            <label class="visually-hidden" for="keyword">検索キーワード</label>
            <input id="keyword" name="keyword" placeholder="検索キーワード">
            <button type="submit">検索</button>
            <button id="clearSearchButton" type="button">解除</button>
        </form>
    </section>

    <section aria-labelledby="listHeading">
        <div class="section-heading">
            <h2 id="listHeading">つぶやき一覧</h2>
            <button id="refreshButton" type="button">更新</button>
        </div>
        <div id="mutterList" aria-live="polite" aria-busy="true"></div>
        <p id="emptyMessage" hidden>つぶやきがありません。</p>
        <button id="loadMoreButton" type="button" hidden>さらに読み込む</button>
    </section>
</main>

<dialog id="editDialog">
    <form id="editForm">
        <h2>つぶやきを編集</h2>
        <input id="editId" type="hidden">
        <input id="editVersion" type="hidden">
        <label for="editText">本文</label>
        <input id="editText" maxlength="255" required>
        <div class="dialog-actions">
            <button id="cancelEditButton" type="button">キャンセル</button>
            <button type="submit">更新</button>
        </div>
    </form>
</dialog>

<footer><p>どこつぶ</p></footer>
<script type="module" src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
