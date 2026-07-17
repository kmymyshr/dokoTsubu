package servlet;

import java.io.IOException;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

/**
 * フォロワー一覧のReactホストを表示するServlet。
 *
 * <p>Phase9で一覧描画をReactへ移したため、このServletはログイン確認、対象ユーザー確認、
 * ReactホストJSPへのforwardだけを担当する。一覧データはReactが `/api/follows?type=followers`
 * から取得する。</p>
 */
@WebServlet("/FollowerList")
public class FollowerList extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /** Reactホストに必要な対象ユーザーだけをJSPへ渡す。 */
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

        request.setAttribute("targetUser", targetUser);

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
