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
<body data-context-path="${pageContext.request.contextPath}"
      data-csrf-token="${_csrf.token}"
      data-csrf-header="${_csrf.headerName}">
    <main style="max-width: 720px; margin: 0 auto; padding: 24px;">
        <p><a href="${pageContext.request.contextPath}/Main">Mainへ戻る</a></p>

        <section class="profile-card">
            <h1>${profileUser.name}</h1>
            <p>ユーザーID: ${profileUser.id}</p>

            <div style="display: flex; gap: 16px; flex-wrap: wrap; margin: 16px 0;">
                <p id="followersCount">
                    フォロワー:
                    <a href="${pageContext.request.contextPath}/FollowerList?userId=${profileUser.id}">
                        ${followers}
                    </a>
                </p>
                <p>
                    フォロー中:
                    <a href="${pageContext.request.contextPath}/FollowingList?userId=${profileUser.id}">
                        ${followingCount}
                    </a>
                </p>
            </div>

            <section style="margin: 24px 0;">
                <h2>ひとこと自己紹介</h2>
                <c:choose>
                    <c:when test="${not empty submittedBio}">
                        <p><c:out value="${submittedBio}" /></p>
                    </c:when>
                    <c:when test="${not empty profileUser.bio}">
                        <p><c:out value="${profileUser.bio}" /></p>
                    </c:when>
                    <c:otherwise>
                        <p>まだ自己紹介はありません。</p>
                    </c:otherwise>
                </c:choose>
            </section>

            <c:if test="${param.updated eq '1'}">
                <p role="status">自己紹介を更新しました。</p>
            </c:if>
            <c:if test="${not empty errorMsg}">
                <p role="alert"><c:out value="${errorMsg}" /></p>
            </c:if>

            <c:if test="${ownProfile}">
                <section style="margin: 24px 0;">
                    <h2>自己紹介を編集</h2>
                    <form action="${pageContext.request.contextPath}/Profile" method="post">
                        <input type="hidden" name="userId" value="${profileUser.id}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <textarea name="bio" rows="4" maxlength="160" style="width: 100%;"><c:out value="${not empty submittedBio ? submittedBio : profileUser.bio}" /></textarea>
                        <div style="margin-top: 12px;">
                            <button type="submit">保存</button>
                        </div>
                    </form>
                </section>
            </c:if>

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
