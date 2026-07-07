package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.LikeMutterLogic;
import model.User;

@WebServlet("/LikeMutter")
public class LikeMutter extends HttpServlet {
    private static final long serialVersionUID = 1L;

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

        Integer mutterId = parsePositiveInteger(request.getParameter("mutterId"));
        if (mutterId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "mutterIdが不正です");
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
