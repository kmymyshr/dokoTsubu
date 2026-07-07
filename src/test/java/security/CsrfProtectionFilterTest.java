package security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

class CsrfProtectionFilterTest {
    private final CsrfProtectionFilter filter = new CsrfProtectionFilter();

    @Test
    void allowsSafeGetWithoutToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getMethod()).thenReturn("GET");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void rejectsLoggedInPostWithoutToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        FilterChain chain = mock(FilterChain.class);
        StringWriter body = new StringWriter();
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("loginUser")).thenReturn(new User(1, "test", "hash"));
        when(session.getAttribute(CsrfTokenManager.SESSION_ATTRIBUTE)).thenReturn("expected");
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        filter.doFilter(request, response, chain);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
        assertTrue(body.toString().contains("CSRF_TOKEN_INVALID"));
    }

    @Test
    void allowsLoggedInDeleteWithMatchingToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("loginUser")).thenReturn(new User(1, "test", "hash"));
        when(session.getAttribute(CsrfTokenManager.SESSION_ATTRIBUTE)).thenReturn("expected");
        when(request.getHeader(CsrfTokenManager.REQUEST_HEADER)).thenReturn("expected");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void allowsLegacyFormPostWithHiddenToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("loginUser")).thenReturn(new User(1, "test", "hash"));
        when(session.getAttribute(CsrfTokenManager.SESSION_ATTRIBUTE)).thenReturn("expected");
        when(request.getParameter("_csrf")).thenReturn("expected");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }
    @Test
    void createsOneTokenPerSession() {
        HttpSession session = mock(HttpSession.class);
        AtomicReference<Object> stored = new AtomicReference<>();
        when(session.getAttribute(CsrfTokenManager.SESSION_ATTRIBUTE))
                .thenAnswer(invocation -> stored.get());
        doAnswer(invocation -> {
            stored.set(invocation.getArgument(1));
            return null;
        }).when(session).setAttribute(eq(CsrfTokenManager.SESSION_ATTRIBUTE), anyString());
        String first = CsrfTokenManager.getOrCreate(session);
        String second = CsrfTokenManager.getOrCreate(session);
        assertNotNull(first);
        assertEquals(first, second);
    }
}
