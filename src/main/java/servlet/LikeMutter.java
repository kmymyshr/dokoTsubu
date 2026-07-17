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
import model.Mutter;
import model.User;
import util.ObjectMapperFactory;

/**
 * 投稿へのいいね状態を切り替えるJSON用Servlet。
 *
 * <p>React画面から利用される既存URLを維持しつつ、Phase19で旧Logicラッパーを撤去した。
 * このServletはHTTP/JSONの入出力と投稿存在確認を担当し、いいね更新は
 * {@link SocialService} に委譲する。</p>
 */
@WebServlet("/LikeMutter")
public class LikeMutter extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    /**
     * JSON本文のmutterIdを受け取り、いいねON/OFFを切り替える。
     *
     * <p>自分の投稿へのいいねは禁止し、切り替え後の状態といいね数をJSONで返す。</p>
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

        Integer mutterId = readId(request, "mutterId");
        if (mutterId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "mutterIdが不正です");
            return;
        }

        Mutter target = ApplicationServiceBridge.mutters().findById(mutterId);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "指定された投稿は存在しません");
            return;
        }
        if (target.getUserId() == loginUser.getId()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "自分の投稿にはいいねできません");
            return;
        }

        SocialService social = ApplicationServiceBridge.social();
        boolean liked = social.toggleLike(mutterId, loginUser.getId());
        int count = social.countLikes(mutterId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"liked\":" + liked + ",\"count\":" + count + "}");
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
