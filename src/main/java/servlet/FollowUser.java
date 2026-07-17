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

@WebServlet("/FollowUser")
public class FollowUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

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

        // リクエストボディは JSON で送られてくるため、パラメータではなくボディを読み取る。
        // 画面からの操作はこの形で送られるため、ここで受け取る。
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

        FollowUserLogic logic = new FollowUserLogic();
        boolean following = logic.execute(loginUser.getId(), followeeId);
        int followers = logic.countFollowers(followeeId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"following\":" + following + ",\"followers\":" + followers + "}");
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
