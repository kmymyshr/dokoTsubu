package servlet;

import java.io.IOException;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.SocialService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.ObjectMapperFactory;

/**
 * フォロー状態を切り替えるJSON用Servlet。
 *
 * <p>React画面から利用される既存URLを維持しつつ、Phase19で旧Logicラッパーを撤去した。
 * このServletはHTTP/JSONの入出力とログイン確認を担当し、実際のフォロー更新は
 * {@link SocialService} に委譲する。</p>
 */
@WebServlet("/FollowUser")
public class FollowUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    /**
     * JSON本文のfolloweeIdを受け取り、フォローON/OFFを切り替える。
     *
     * <p>レスポンスには、切り替え後のフォロー状態と対象ユーザーのフォロワー数を返す。</p>
     */
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

        Integer followeeId = readId(request, "followeeId");
        if (followeeId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "followeeIdが不正です");
            return;
        }
        if (followeeId == loginUser.getId()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "自分自身はフォローできません");
            return;
        }

        SocialService social = ApplicationServiceBridge.social();
        boolean following = social.toggleFollow(loginUser.getId(), followeeId);
        int followers = social.countFollowers(followeeId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"following\":" + following + ",\"followers\":" + followers + "}");
    }

    /** JSON本文から指定されたID項目を正の整数として読み取る。 */
    private Integer readId(HttpServletRequest request, String fieldName) throws IOException {
        JsonNode body;
        try {
            body = OBJECT_MAPPER.readTree(request.getReader());
        } catch (JsonProcessingException e) {
            return null;
        }
        if (body == null || !body.has(fieldName)) {
            return null;
        }
        JsonNode idNode = body.get(fieldName);
        if (idNode.isInt()) {
            int id = idNode.asInt();
            return id > 0 ? id : null;
        }
        return idNode.isTextual() ? parsePositiveInteger(idNode.asText()) : null;
    }

    /** 文字列で送られたIDも安全に扱えるよう、正の整数だけを受け入れる。 */
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
