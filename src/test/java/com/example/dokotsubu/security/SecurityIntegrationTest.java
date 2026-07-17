package com.example.dokotsubu.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:securityContext;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepareUser() {
        jdbcTemplate.update("DELETE FROM USERS");
        jdbcTemplate.update("INSERT INTO USERS(NAME, PASS, BIO) VALUES (?, ?, ?)",
                "alice", passwordEncoder.encode("password"), "");
    }

    @Test
    void protectedPageRedirectsToTopWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/Main"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void apiReturnsJson401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(containsString("\"code\":\"UNAUTHORIZED\"")));
    }

    @Test
    void unsafeJsonRequestWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/FollowUser").with(user("alice")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(containsString("\"code\":\"CSRF_TOKEN_INVALID\"")));
    }

    @Test
    void unsafeJsonRequestReturns401AfterSessionExpires() throws Exception {
        mockMvc.perform(post("/FollowUser"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(containsString("\"code\":\"UNAUTHORIZED\"")));
    }

    @Test
    void loginFailureKeepsLegacyResultPage() throws Exception {
        mockMvc.perform(post("/Login")
                        .with(csrf())
                        .param("name", "unknown")
                        .param("pass", "wrong"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/WEB-INF/jsp/loginResult.jsp"));
    }

    @Test
    void loginAndLogoutUseSpringSecurityWhileKeepingLegacySession() throws Exception {
        MvcResult login = mockMvc.perform(post("/Login")
                        .with(csrf())
                        .param("name", "alice")
                        .param("pass", "password"))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername("alice"))
                .andExpect(forwardedUrl("/WEB-INF/jsp/loginResult.jsp"))
                .andReturn();

        org.assertj.core.api.Assertions.assertThat(
                login.getRequest().getSession().getAttribute("loginUser"))
                .isInstanceOf(model.User.class);

        mockMvc.perform(post("/Logout")
                        .session((org.springframework.mock.web.MockHttpSession)
                                login.getRequest().getSession(false))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(unauthenticated())
                .andExpect(forwardedUrl("/WEB-INF/jsp/logout.jsp"));
    }
}
