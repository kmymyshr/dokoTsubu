package support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

/**
 * Servletの特性テストで繰り返し必要になる、リクエスト/レスポンスのモック生成をまとめたヘルパー。
 * (モダナイゼーション計画 Phase0: 安全網構築)
 */
public final class ServletTestSupport {
    private ServletTestSupport() {
    }

    /** ログイン中ユーザー付きのリクエストモックを作る。userがnullなら未ログイン状態を表す。 */
    public static HttpServletRequest mockRequest(User loginUser) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("");
        if (loginUser != null) {
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute("loginUser")).thenReturn(loginUser);
            when(request.getSession(false)).thenReturn(session);
            when(request.getSession()).thenReturn(session);
        }
        return request;
    }

    /** リクエストボディとしてJSON文字列を読めるようにする。 */
    public static void withJsonBody(HttpServletRequest request, String json) throws IOException {
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
    }

    /** レスポンスへ書き込まれるJSON文字列を後から検証できるようにする。 */
    public static StringWriter captureResponseBody(HttpServletResponse response) throws IOException {
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        return body;
    }
}
