package api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static support.ServletTestSupport.captureResponseBody;
import static support.ServletTestSupport.mockRequest;

import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import util.ObjectMapperFactory;

/**
 * SessionApiServletの現状の挙動を固定する特性テスト。
 * (モダナイゼーション計画 Phase0: 安全網構築)
 */
class SessionApiServletCharacterizationTest {

    private final SessionApiServlet servlet = new SessionApiServlet();

    @Test
    void doGet_returns401_whenNotLoggedIn() throws Exception {
        HttpServletRequest request = mockRequest(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        verify(response).setStatus(401);
        JsonNode json = ObjectMapperFactory.getObjectMapper().readTree(body.toString());
        assertEquals("UNAUTHORIZED", json.get("code").asText());
    }

    @Test
    void doGet_returnsUserAndCsrfToken_whenLoggedIn() throws Exception {
        User loginUser = new User(1, "alice", "hashed-pass");
        HttpServletRequest request = mockRequest(loginUser);
        org.mockito.Mockito.when(request.getAttribute(
                org.springframework.security.web.csrf.CsrfToken.class.getName()))
                .thenReturn(new DefaultCsrfToken("X-CSRF-Token", "_csrf", "test-token"));
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        JsonNode json = ObjectMapperFactory.getObjectMapper().readTree(body.toString());
        assertEquals(1, json.get("id").asInt());
        assertEquals("alice", json.get("name").asText());
        assertFalse(json.get("csrfToken").asText().isBlank());
    }
}
