package api;

import java.io.IOException;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.SocialService;
import com.example.dokotsubu.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ApiErrorResponse;
import dto.ProfileResponse;
import dto.ProfileUpdateRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.ObjectMapperFactory;

/**
 * プロフィール表示・更新を扱うREST API。
 *
 * <p>Phase11でプロフィール画面をReactへ移すため、Servlet/JSPが直接組み立てていた
 * 表示用属性をJSONへ移す。本人確認、自己紹介の文字数制限、セッション更新はここに集約し、
 * React側は表示と入力状態の管理に集中する。</p>
 */
@WebServlet("/api/profile")
public class ProfileApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_BIO_LENGTH = 160;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    /** 対象ユーザーのプロフィール情報、フォロー状態、フォロー数を返す。 */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }

        Integer userId = parsePositiveInteger(request.getParameter("userId"));
        if (userId == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_USER_ID", "userIdには正の整数を指定してください");
            return;
        }

        UserService users = ApplicationServiceBridge.users();
        User profileUser = users.findById(userId);
        if (profileUser == null) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "USER_NOT_FOUND", "指定されたユーザーは存在しません");
            return;
        }

        writeJson(response, HttpServletResponse.SC_OK, buildResponse(loginUser, profileUser));
    }

    /**
     * 本人プロフィールの自己紹介を更新する。
     *
     * <p>旧JSPフォームと同じく、本人以外のプロフィール更新は禁止する。更新後はヘッダー等で
     * 参照されるセッション上のloginUserも差し替え、画面とセッションの表示を揃える。</p>
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }

        ProfileUpdateRequest body = readBody(request, response);
        if (body == null) {
            return;
        }

        if (body.userId() != loginUser.getId()) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "FORBIDDEN", "本人のプロフィールだけ更新できます");
            return;
        }

        String bio = normalizeBio(body.bio());
        if (bio.length() > MAX_BIO_LENGTH) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "BIO_TOO_LONG", "自己紹介は160文字以内で入力してください");
            return;
        }

        UserService users = ApplicationServiceBridge.users();
        if (!users.updateBio(loginUser.getId(), bio)) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "PROFILE_UPDATE_FAILED", "自己紹介の更新に失敗しました");
            return;
        }

        User refreshedUser = users.findById(loginUser.getId());
        request.getSession().setAttribute("loginUser", refreshedUser);
        writeJson(response, HttpServletResponse.SC_OK, buildResponse(refreshedUser, refreshedUser));
    }

    /** APIはReactから使うため、未ログイン時はHTMLへredirectせずJSONの401で返す。 */
    private User requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED", "ログインが必要です");
        }
        return loginUser;
    }

    /** Serviceから取得した情報を、Reactが扱いやすい表示用DTOへ変換する。 */
    private ProfileResponse buildResponse(User loginUser, User profileUser) {
        SocialService social = ApplicationServiceBridge.social();
        boolean ownProfile = loginUser.getId() == profileUser.getId();
        return ProfileResponse.from(
                profileUser,
                ownProfile,
                ownProfile ? false : social.isFollowing(loginUser.getId(), profileUser.getId()),
                social.countFollowers(profileUser.getId()),
                social.countFollowing(profileUser.getId()));
    }

    /** JSON本文をProfileUpdateRequestへ変換し、壊れたJSONは400として返す。 */
    private ProfileUpdateRequest readBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            return OBJECT_MAPPER.readValue(request.getReader(), ProfileUpdateRequest.class);
        } catch (IOException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_JSON", "リクエストJSONが不正です");
            return null;
        }
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

    /** 成功レスポンスは常にUTF-8のJSONで返す。 */
    private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }

    /** エラーもReact側で扱いやすい共通形式にそろえる。 */
    private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
        writeJson(response, status, new ApiErrorResponse(status, code, message));
    }
}
