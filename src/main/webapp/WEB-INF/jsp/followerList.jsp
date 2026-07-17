<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>フォロワー一覧</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/react/assets/main.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">
    <main style="max-width: 720px; margin: 0 auto; padding: 24px;">
        <p><a href="${pageContext.request.contextPath}/Profile?userId=${targetUser.id}">プロフィールへ戻る</a></p>

        <section class="profile-card">
            <h1>${targetUser.name} さんのフォロワー</h1>
            <p>合計: <span id="listCount">${followerCount}</span> 人</p>

            <c:choose>
                <c:when test="${empty followerUsers}">
                    <p id="emptyState">まだフォロワーはいません。</p>
                </c:when>
                <c:otherwise>
                    <p id="emptyState" style="display: none;">まだフォロワーはいません。</p>
                    <ul id="followList">
                        <c:forEach var="followerUser" items="${followerUsers}">
                            <li data-user-id="${followerUser.id}" style="display: flex; gap: 12px; align-items: center; margin: 8px 0;">
                                <a href="${pageContext.request.contextPath}/Profile?userId=${followerUser.id}">
                                    ${followerUser.name}
                                </a>
                                <c:if test="${currentUserId ne followerUser.id}">
                                    <button
                                        type="button"
                                        class="follow-toggle"
                                        data-user-id="${followerUser.id}"
                                        data-following="${followedUserIds.contains(followerUser.id)}"
                                        data-remove-on-unfollow="false">
                                        <c:choose>
                                            <c:when test="${followedUserIds.contains(followerUser.id)}">フォロー済</c:when>
                                            <c:otherwise>フォロー</c:otherwise>
                                        </c:choose>
                                    </button>
                                </c:if>
                            </li>
                        </c:forEach>
                    </ul>
                    <p id="followActionMessage" role="status"></p>
                    <script src="${pageContext.request.contextPath}/js/followList.js"></script>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</body>
</html>
