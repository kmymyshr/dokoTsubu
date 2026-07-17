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

@WebServlet("/FollowerList")
public class FollowerList extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
