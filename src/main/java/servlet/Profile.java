package servlet;

import java.io.IOException;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.UserService;
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
 * JSP版プロフィール画面を表示・更新するServlet。
 *
 * <p>React移行前のJSP画面を残しつつ、Phase5でユーザー取得と自己紹介更新を
 * {@link UserService} 経由に変更した。画面遷移やrequest属性名は既存JSPに合わせて維持している。</p>
 */
@WebServlet("/Profile")
public class Profile extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_BIO_LENGTH = 160;

    /** プロフィール画面を表示する。本人/他人の違いやフォロー状態もJSP用属性に詰める。 */
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

        UserService users = ApplicationServiceBridge.users();
        User profileUser = users.findById(userId);
        if (profileUser == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "指定されたユーザーは存在しません");
            return;
        }

        prepareProfileAttributes(request, loginUser, profileUser);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * 自己紹介を更新する。
     *
     * <p>本人以外のプロフィール更新を防ぐため、URLパラメータのuserIdとセッションユーザーIDを照合する。</p>
     */
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

        UserService users = ApplicationServiceBridge.users();
        String bio = normalizeBio(request.getParameter("bio"));
        if (bio.length() > MAX_BIO_LENGTH) {
            User profileUser = users.findById(userId);
            request.setAttribute("errorMsg", "自己紹介は160文字以内で入力してください");
            request.setAttribute("submittedBio", bio);
            prepareProfileAttributes(request, loginUser, profileUser);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp");
            dispatcher.forward(request, response);
            return;
        }

        if (!users.updateBio(userId, bio)) {
            User profileUser = users.findById(userId);
            request.setAttribute("errorMsg", "自己紹介の更新に失敗しました");
            request.setAttribute("submittedBio", bio);
            prepareProfileAttributes(request, loginUser, profileUser);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // セッション上のloginUserも更新後の内容に差し替え、ヘッダー等の表示と整合させる。
        User refreshedUser = users.findById(userId);
        session.setAttribute("loginUser", refreshedUser);
        response.sendRedirect(request.getContextPath() + "/Profile?userId=" + userId + "&updated=1");
    }

    /** JSPが必要とするプロフィール表示用の属性をまとめて設定する。 */
    private void prepareProfileAttributes(HttpServletRequest request, User loginUser, User profileUser) {
        boolean ownProfile = loginUser.getId() == profileUser.getId();
        FollowUserLogic logic = new FollowUserLogic();

        request.setAttribute("profileUser", profileUser);
        request.setAttribute("ownProfile", ownProfile);
        request.setAttribute("following", ownProfile ? false : logic.isFollowing(loginUser.getId(), profileUser.getId()));
        request.setAttribute("followers", logic.countFollowers(profileUser.getId()));
        request.setAttribute("followingCount", logic.countFollowing(profileUser.getId()));
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

    /** 未入力の自己紹介を空文字として扱い、前後の空白は保存しない。 */
    private String normalizeBio(String bio) {
        return bio == null ? "" : bio.trim();
    }
}
