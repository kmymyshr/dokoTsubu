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
import dao.MutterDAO;
import model.LikeMutterLogic;
import model.Mutter;
import model.User;
import util.ObjectMapperFactory;

@WebServlet("/LikeMutter")
public class LikeMutter extends HttpServlet {
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

        // リクエストボディは JSON で送られてくるためパラメータではなくボディを読み取る
        JsonNode body;
        try {
            body = OBJECT_MAPPER.readTree(request.getReader());
        } catch (JsonProcessingException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JSONの形式が不正です");
            return;
        }
        Integer mutterId = null;
        if (body != null && body.has("mutterId")) {
            JsonNode idNode = body.get("mutterId");
            if (idNode.isInt()) mutterId = idNode.asInt();
            else if (idNode.isTextual()) mutterId = parsePositiveInteger(idNode.asText());
        }
        if (mutterId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "mutterIdが不正です");
            return;
        }

        // 自分の投稿にはいいねできないようにする
        Mutter target = new MutterDAO().findById(mutterId);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "指定されたつぶやきは存在しません");
            return;
        }
        if (target.getUserId() == loginUser.getId()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "自分の投稿にはいいねできません");
            return;
        }

        LikeMutterLogic logic = new LikeMutterLogic();
        boolean liked = logic.execute(mutterId, loginUser.getId());
        int count = logic.countLikes(mutterId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"liked\":" + liked + ",\"count\":" + count + "}");
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
