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
import org.springframework.security.web.csrf.CsrfToken;
import util.ObjectMapperFactory;

/**
 * React画面向けに、現在ログイン中のユーザー情報とCSRFトークンを返すAPI。
 *
 * <p>Phase6でメイン画面をReactへ寄せるため、Reactは初期表示時にこのAPIを呼び、
 * セッション上のユーザー名・ユーザーID・CSRFトークンを取得する。以降のPOST/PUT/DELETEでは
 * ここで受け取ったCSRFトークンを `X-CSRF-Token` ヘッダーとして送信する。</p>
 */
@WebServlet("/api/session")
public class SessionApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    /** セッションにログインユーザーがいればJSONで返し、未ログインなら401を返す。 */
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

        // Spring Securityがリクエスト属性に置いたCSRFトークンをReactへ渡す。
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        OBJECT_MAPPER.writeValue(response.getWriter(),
                SessionUserResponse.from(loginUser, csrfToken.getToken()));
    }
}
