<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>プロフィール</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/react/assets/main.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">
    <main style="max-width: 720px; margin: 0 auto; padding: 24px;">
        <p><a href="${pageContext.request.contextPath}/Main">Mainへ戻る</a></p>

        <section class="profile-card">
            <h1>${profileUser.name}</h1>
            <p>ユーザーID: ${profileUser.id}</p>

            <div style="display: flex; gap: 16px; flex-wrap: wrap; margin: 16px 0;">
                <p id="followersCount">フォロワー: ${followers}</p>
                <p>
                    フォロー中:
                    <a href="${pageContext.request.contextPath}/FollowingList?userId=${profileUser.id}">
                        ${followingCount}
                    </a>
                </p>
            </div>

            <c:if test="${not ownProfile}">
                <div id="profileFollow"
                     data-user-id="${profileUser.id}"
                     data-following="${following}">
                    <button id="followButton" type="button">
                        <c:choose>
                            <c:when test="${following}">フォロー済</c:when>
                            <c:otherwise>フォロー</c:otherwise>
                        </c:choose>
                    </button>
                    <p id="followMessage" role="status"></p>
                </div>
                <script src="${pageContext.request.contextPath}/js/profile.js"></script>
            </c:if>
        </section>
    </main>
</body>
</html>
