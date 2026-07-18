package api;

import java.io.IOException;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.SocialService;
import com.example.dokotsubu.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.ObjectMapperFactory;

/**
 * ユーザーに紐づくソーシャル操作を扱うREST API。
 *
 * <p>Phase20で旧 {@code /FollowUser} Servletからフォロー切り替えを移し、
 * {@code /api/users/{id}/follow} としてReact側の正式な呼び出し口にするために追加した。
 * 画面名由来のServlet URLを隠し、ユーザーリソースに対する操作として読める形へ整理する。</p>
 */
@WebServlet("/api/users/*")
public class UserSocialApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    /**
     * 指定ユーザーへのフォロー状態を切り替える。
     *
     * <p>ログイン確認、URL上のユーザーID検証、対象ユーザーの存在確認を行ってから
     * {@link SocialService} へ委譲し、Reactが即座に表示更新できるよう現在状態とフォロワー数を返す。</p>
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }

        Integer followeeId = parseFollowResourceId(request, response);
        if (followeeId == null) {
            return;
        }
        if (followeeId == loginUser.getId()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "CANNOT_FOLLOW_SELF", "自分自身はフォローできません");
            return;
        }

        UserService users = ApplicationServiceBridge.users();
        if (users.findById(followeeId) == null) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "USER_NOT_FOUND", "指定されたユーザーは存在しません");
            return;
        }

        SocialService social = ApplicationServiceBridge.social();
        boolean following = social.toggleFollow(loginUser.getId(), followeeId);
        int followers = social.countFollowers(followeeId);
        writeJson(response, HttpServletResponse.SC_OK,
                java.util.Map.of("following", following, "followers", followers));
    }

    /** API共通のログイン確認。未ログイン時はReactが扱いやすいJSONエラーを返す。 */
    private User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED", "ログインが必要です");
        }
        return loginUser;
    }

    /** `/api/users/{id}/follow` のユーザーID部分を解析する。 */
    private Integer parseFollowResourceId(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String path = request.getPathInfo();
        if (path == null || !path.matches("/\\d+/follow/?")) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "RESOURCE_NOT_FOUND", "指定されたAPIリソースは存在しません");
            return null;
        }
        String idPart = path.replaceFirst("^/", "").replaceFirst("/follow/?$", "");
        try {
            int id = Integer.parseInt(idPart);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_ID", "IDには正の整数を指定してください");
            return null;
        }
    }

    /** JSONレスポンスを書き込む共通処理。 */
    private void writeJson(HttpServletResponse response, int status, Object body)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }

    /** APIで共通利用するエラーレスポンスを書き込む。 */
    private void writeError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        writeJson(response, status, new ApiErrorResponse(status, code, message));
    }
}
