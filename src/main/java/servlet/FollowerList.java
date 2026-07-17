package servlet;

import java.io.IOException;
import java.util.stream.Collectors;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.FollowUserLogic;
import model.User;

/**
 * JSP版フォロワー一覧画面を表示するServlet。
 *
 * <p>Phase5では対象ユーザーの取得をUserService経由に寄せた。フォロー関係の取得は、
 * 既存JSPとの互換を保つため、移行用のFollowUserLogicを経由してSocialServiceへ委譲している。
 * Phase8ではReact化に備えて同じ情報を返す `/api/follows?type=followers` も追加し、
 * このServletはJSP互換画面の入口として残す。</p>
 */
@WebServlet("/FollowerList")
public class FollowerList extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /** フォロワー一覧と、ログインユーザーから見たフォロー状態をJSPへ渡す。 */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        Integer userId = parsePositiveInteger(request.getParameter("userId"));
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "userIdが不正です");
            return;
        }

        User targetUser = ApplicationServiceBridge.users().findById(userId);
        if (targetUser == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "指定されたユーザーは存在しません");
            return;
        }

        // JSP側が一覧表示とボタン状態を組み立てられるよう、対象ユーザー・件数・フォロー済みIDをまとめて渡す。
        FollowUserLogic logic = new FollowUserLogic();
        request.setAttribute("targetUser", targetUser);
        request.setAttribute("followerUsers", logic.findFollowerUsers(userId));
        request.setAttribute("followerCount", logic.countFollowers(userId));
        request.setAttribute("currentUserId", loginUser.getId());
        request.setAttribute("followedUserIds",
                logic.findFollowingUsers(loginUser.getId()).stream()
                        .map(User::getId)
                        .collect(Collectors.toSet()));

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/followerList.jsp");
        dispatcher.forward(request, response);
    }

    /** URLパラメータのIDを正の整数として安全に読む。 */
    private Integer parsePositiveInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int number = Integer.parseInt(value);
            return number > 0 ? number : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
