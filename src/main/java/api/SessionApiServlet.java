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

/**
 * JavaScript 画面向けに、ログイン中ユーザーの公開情報を返す API です。
 * フロントエンドが現在のログイン状態を確認するために使います。
 */
@WebServlet("/api/session")
public class SessionApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    /**
     * セッションにログインユーザーがあるか確認し、結果を JSON で返す。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. セッションからログインユーザーを取得する。
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 2. ログインしていなければエラーを返す。
        if (loginUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            OBJECT_MAPPER.writeValue(response.getWriter(),
                    new ApiErrorResponse(401, "UNAUTHORIZED", "ログインが必要です"));
            return;
        }

        // 3. ログイン済みなら、ユーザー情報と CSRF トークンを返す。
        OBJECT_MAPPER.writeValue(response.getWriter(), SessionUserResponse.from(loginUser, CsrfTokenManager.getOrCreate(session)));
    }
}
