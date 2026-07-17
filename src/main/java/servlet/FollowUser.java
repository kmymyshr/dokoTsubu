package servlet;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.FollowUserLogic;
import model.User;
import util.ObjectMapperFactory;

/**
 * React/JSP双方から利用する、フォロー切り替え用のServlet。
 *
 * <p>Phase6ではメイン画面のフォローボタンをReactから呼び出すため、JSONリクエストを受け取り、
 * 切り替え後のフォロー状態とフォロワー数をJSONで返す。実処理は旧Logic互換層を通じて
 * SocialServiceへ委譲している。</p>
 */
@WebServlet("/FollowUser")
public class FollowUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    /** フォロー状態を切り替え、画面更新に必要な状態をJSONで返す。 */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        JsonNode body;
        try {
            body = OBJECT_MAPPER.readTree(request.getReader());
        } catch (JsonProcessingException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JSONの形式が不正です");
            return;
        }

        Integer followeeId = null;
        if (body != null && body.has("followeeId")) {
            JsonNode idNode = body.get("followeeId");
            if (idNode.isInt()) followeeId = idNode.asInt();
            else if (idNode.isTextual()) followeeId = parsePositiveInteger(idNode.asText());
        }
        if (followeeId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "followeeIdが不正です");
            return;
        }
        if (followeeId == loginUser.getId()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "自分自身はフォローできません");
            return;
        }

        FollowUserLogic logic = new FollowUserLogic();
        boolean following = logic.execute(loginUser.getId(), followeeId);
        int followers = logic.countFollowers(followeeId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"following\":" + following + ",\"followers\":" + followers + "}");
    }

    /** JSON内のfolloweeIdが文字列で送られた場合にも安全に扱う。 */
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
