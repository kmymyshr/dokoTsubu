package servlet;

import java.io.IOException;

import dao.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.FollowUserLogic;
import model.User;

@WebServlet("/Profile")
public class Profile extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_BIO_LENGTH = 160;

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

        User profileUser = new UserDAO().findById(userId);
        if (profileUser == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "指定されたユーザーは存在しません");
            return;
        }

        prepareProfileAttributes(request, loginUser, profileUser);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        Integer userId = parsePositiveInteger(request.getParameter("userId"));
        if (userId == null || userId != loginUser.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "本人のプロフィールだけ更新できます");
            return;
        }

        String bio = normalizeBio(request.getParameter("bio"));
        if (bio.length() > MAX_BIO_LENGTH) {
            User profileUser = new UserDAO().findById(userId);
            request.setAttribute("errorMsg", "自己紹介は160文字以内で入力してください");
            request.setAttribute("submittedBio", bio);
            prepareProfileAttributes(request, loginUser, profileUser);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp");
            dispatcher.forward(request, response);
            return;
        }

        UserDAO userDAO = new UserDAO();
        if (!userDAO.updateBio(userId, bio)) {
            User profileUser = userDAO.findById(userId);
            request.setAttribute("errorMsg", "自己紹介の更新に失敗しました");
            request.setAttribute("submittedBio", bio);
            prepareProfileAttributes(request, loginUser, profileUser);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp");
            dispatcher.forward(request, response);
            return;
        }

        User refreshedUser = userDAO.findById(userId);
        session.setAttribute("loginUser", refreshedUser);
        response.sendRedirect(request.getContextPath() + "/Profile?userId=" + userId + "&updated=1");
    }

    private void prepareProfileAttributes(HttpServletRequest request, User loginUser, User profileUser) {
        boolean ownProfile = loginUser.getId() == profileUser.getId();
        FollowUserLogic logic = new FollowUserLogic();

        request.setAttribute("profileUser", profileUser);
        request.setAttribute("ownProfile", ownProfile);
        request.setAttribute("following", ownProfile ? false : logic.isFollowing(loginUser.getId(), profileUser.getId()));
        request.setAttribute("followers", logic.countFollowers(profileUser.getId()));
        request.setAttribute("followingCount", logic.countFollowing(profileUser.getId()));
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

    private String normalizeBio(String bio) {
        return bio == null ? "" : bio.trim();
    }
}
