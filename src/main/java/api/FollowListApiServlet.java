package api;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.SocialService;
import com.example.dokotsubu.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ApiErrorResponse;
import dto.FollowListResponse;
import dto.FollowUserSummaryResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.ObjectMapperFactory;

/**
 * フォロー中/フォロワー一覧を返すREST API。
 *
 * <p>Phase8では、既存のJSP一覧画面をすぐに廃止せず、React化時に利用できるJSONの入口を先に用意する。
 * これにより、画面の置き換え時はJSPが組み立てている属性ではなく、このAPIレスポンスを描画元にできる。</p>
 */
@WebServlet("/api/follows")
public class FollowListApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();
    private static final String TYPE_FOLLOWERS = "followers";
    private static final String TYPE_FOLLOWING = "following";

    /**
     * `type=followers` ならフォロワー一覧、`type=following` ならフォロー中一覧を返す。
     *
     * <p>ログインユーザーから見たフォロー済み状態も含めることで、React側はボタン表示を
     * 追加問い合わせなしで決められる。</p>
     */
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

        String type = normalizeType(request.getParameter("type"));
        if (type == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_TYPE", "typeにはfollowersまたはfollowingを指定してください");
            return;
        }

        UserService users = ApplicationServiceBridge.users();
        SocialService social = ApplicationServiceBridge.social();
        User targetUser = users.findById(userId);
        if (targetUser == null) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "USER_NOT_FOUND", "指定されたユーザーは存在しません");
            return;
        }

        // ログインユーザーのフォロー中IDを先にSet化し、一覧各行のボタン状態を効率よく判定する。
        Set<Integer> followedUserIds = social.findFollowingUsers(loginUser.getId()).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        List<User> listUsers = TYPE_FOLLOWERS.equals(type)
                ? social.findFollowerUsers(userId)
                : social.findFollowingUsers(userId);
        int count = TYPE_FOLLOWERS.equals(type)
                ? social.countFollowers(userId)
                : social.countFollowing(userId);

        List<FollowUserSummaryResponse> responseUsers = listUsers.stream()
                .map(user -> FollowUserSummaryResponse.from(
                        user,
                        followedUserIds.contains(user.getId()),
                        loginUser.getId()))
                .toList();

        writeJson(response, HttpServletResponse.SC_OK,
                FollowListResponse.from(type, targetUser, count, responseUsers));
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

    /** 一覧種別の表記ゆれを吸収し、許可した値だけをAPI内部で扱う。 */
    private String normalizeType(String value) {
        if (TYPE_FOLLOWERS.equals(value) || TYPE_FOLLOWING.equals(value)) {
            return value;
        }
        return null;
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
