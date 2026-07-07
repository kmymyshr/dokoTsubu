<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>フォロー中一覧</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/react/assets/main.css">
</head>
<body>
    <main style="max-width: 720px; margin: 0 auto; padding: 24px;">
        <p><a href="${pageContext.request.contextPath}/Profile?userId=${targetUser.id}">プロフィールへ戻る</a></p>

        <section class="profile-card">
            <h1>${targetUser.name} さんのフォロー中</h1>
            <p>合計: ${followingCount} 人</p>

            <c:choose>
                <c:when test="${empty followingUsers}">
                    <p>まだフォローしているユーザーはいません。</p>
                </c:when>
                <c:otherwise>
                    <ul>
                        <c:forEach var="followingUser" items="${followingUsers}">
                            <li>
                                <a href="${pageContext.request.contextPath}/Profile?userId=${followingUser.id}">
                                    ${followingUser.name}
                                </a>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</body>
</html>
