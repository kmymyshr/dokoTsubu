package security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.http.HttpSession;

/** セッションごとのCSRFトークンを生成・検証します。 */
public final class CsrfTokenManager {
    public static final String SESSION_ATTRIBUTE = "csrfToken";
    public static final String REQUEST_HEADER = "X-CSRF-Token";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfTokenManager() {
    }

    public static String getOrCreate(HttpSession session) {
        Object existing = session.getAttribute(SESSION_ATTRIBUTE);
        if (existing instanceof String token && !token.isBlank()) {
            return token;
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_ATTRIBUTE, token);
        return token;
    }

    public static boolean matches(HttpSession session, String submittedToken) {
        if (session == null || submittedToken == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_ATTRIBUTE);
        if (!(expected instanceof String expectedToken)) {
            return false;
        }
        return MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                submittedToken.getBytes(StandardCharsets.UTF_8));
    }
}
