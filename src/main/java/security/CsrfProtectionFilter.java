package security;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.ApiErrorResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.ObjectMapperFactory;

/** Cookie認証された変更系APIにCSRFトークンを要求します。 */
@WebFilter(urlPatterns = { "/api/*", "/Main", "/UpdateMutter", "/DeleteMutter" })
public class CsrfProtectionFilter implements Filter {
    private static final Set<String> PROTECTED_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (!PROTECTED_METHODS.contains(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            chain.doFilter(request, response);
            return;
        }

        String submittedToken = httpRequest.getHeader(CsrfTokenManager.REQUEST_HEADER);
        if (submittedToken == null) {
            submittedToken = httpRequest.getParameter("_csrf");
        }
        if (!CsrfTokenManager.matches(session, submittedToken)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            OBJECT_MAPPER.writeValue(httpResponse.getWriter(),
                    new ApiErrorResponse(403, "CSRF_TOKEN_INVALID",
                            "CSRFトークンが不正です。画面を再読み込みしてください"));
            return;
        }
        chain.doFilter(request, response);
    }
}
