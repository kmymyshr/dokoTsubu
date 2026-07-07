package api;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.ApiErrorResponse;
import dto.SessionUserResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import security.CsrfTokenManager;
import util.ObjectMapperFactory;

/** JavaScript画面へログイン中ユーザーの公開情報だけを返します。 */
@WebServlet("/api/session")
public class SessionApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (loginUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            OBJECT_MAPPER.writeValue(response.getWriter(),
                    new ApiErrorResponse(401, "UNAUTHORIZED", "ログインが必要です"));
            return;
        }
        OBJECT_MAPPER.writeValue(response.getWriter(), SessionUserResponse.from(loginUser, CsrfTokenManager.getOrCreate(session)));
    }
}
