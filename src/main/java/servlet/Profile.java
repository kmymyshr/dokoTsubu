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
 * プロフィール画面の入口Servlet。
 *
 * <p>Phase11でプロフィール画面をReact化したため、GETはReactホストJSPへ対象ユーザーIDを渡すだけに縮小する。
 * POSTは旧JSPフォーム互換として残すが、通常の表示・更新はReactから `/api/profile` を呼ぶ。</p>
 */
@WebServlet("/Profile")
public class Profile extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_BIO_LENGTH = 160;

    /**
     * Reactプロフィール画面を読み込むJSPホストへforwardする。
     *
     * <p>対象ユーザーが存在するかはここで確認し、React側には対象IDだけを渡す。
     * 実際の表示データは `/api/profile` から取得する。</p>
     */
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

        request.setAttribute("targetUserId", profileUser.getId());
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * 旧JSPフォーム互換の自己紹介更新処理。
     *
     * <p>Phase11以降の通常導線では `/api/profile` のPUTを使うが、段階移行中に古いフォームから
     * POSTされても既存仕様で更新できるよう残している。</p>
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

        // ヘッダー等で参照するセッション上のloginUserも、更新後のユーザー情報に差し替える。
        User refreshedUser = users.findById(userId);
        session.setAttribute("loginUser", refreshedUser);
        response.sendRedirect(request.getContextPath() + "/Profile?userId=" + userId + "&updated=1");
    }

    /** JSPが表示に必要とするプロフィール属性をまとめて設定する。 */
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

    /** 未入力の自己紹介は空文字として扱い、前後の空白は保存しない。 */
    private String normalizeBio(String bio) {
        return bio == null ? "" : bio.trim();
    }
}
