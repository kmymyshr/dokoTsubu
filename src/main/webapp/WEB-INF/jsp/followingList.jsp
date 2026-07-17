<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  フォロー中一覧画面。

  Phase8ではJSP表示を残しながら、React化後に使う一覧取得APIを追加した。
  ログインユーザー自身のフォロー中一覧では、解除後に行を消す既存UXを維持する。
--%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>フォロー中一覧</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/react/assets/main.css">
</head>
<body data-context-path="${pageContext.request.contextPath}"
      data-csrf-token="${_csrf.token}"
      data-csrf-header="${_csrf.headerName}"
      data-follow-list-type="following"
      data-target-user-id="${targetUser.id}">
    <main style="max-width: 720px; margin: 0 auto; padding: 24px;">
        <p><a href="${pageContext.request.contextPath}/Profile?userId=${targetUser.id}">プロフィールへ戻る</a></p>

        <section class="profile-card">
            <%-- 一覧の所有者と件数。React化時はFollowListResponse.targetUser/countから描画する。 --%>
            <h1><c:out value="${targetUser.name}" /> さんのフォロー中</h1>
            <p>合計: <span id="listCount">${followingCount}</span> 人</p>

            <c:choose>
                <c:when test="${empty followingUsers}">
                    <p id="emptyState">まだフォローしているユーザーはいません。</p>
                </c:when>
                <c:otherwise>
                    <p id="emptyState" style="display: none;">まだフォローしているユーザーはいません。</p>
                    <ul id="followList">
                        <c:forEach var="followingUser" items="${followingUsers}">
                            <%-- 各行はReactコンポーネント化しやすいよう、操作対象IDをdata属性に持たせる。 --%>
                            <li data-user-id="${followingUser.id}" style="display: flex; gap: 12px; align-items: center; margin: 8px 0;">
                                <a href="${pageContext.request.contextPath}/Profile?userId=${followingUser.id}">
                                    <c:out value="${followingUser.name}" />
                                </a>
                                <c:if test="${currentUserId ne followingUser.id}">
                                    <%-- 自分のフォロー中一覧では、解除後にそのユーザーを一覧から外す。 --%>
                                    <button
                                        type="button"
                                        class="follow-toggle"
                                        data-user-id="${followingUser.id}"
                                        data-following="${followedUserIds.contains(followingUser.id)}"
                                        data-remove-on-unfollow="${listOwnerIsLoginUser}">
                                        <c:choose>
                                            <c:when test="${followedUserIds.contains(followingUser.id)}">フォロー済</c:when>
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
