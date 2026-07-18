package com.example.dokotsubu.web;

import static org.hamcrest.Matchers.instanceOf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import support.TestDatabaseSupport;

/**
 * Phase21で追加した画面ホストControllerの結合テスト。
 *
 * <p>旧画面Servletを削除した後も、既存URLがReactホストJSPへforwardされることと、
 * JSPが必要とする対象ユーザー属性が渡ることを確認する。</p>
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pageHostController;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PageHostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    private int aliceId;

    @BeforeEach
    void resetTables() {
        TestDatabaseSupport.clearAllTables(jdbc);
        jdbc.update("INSERT INTO USERS(NAME, PASS, BIO) VALUES (?, ?, ?)",
                "alice", "hashed-pass", "");
        aliceId = jdbc.queryForObject(
                "SELECT ID FROM USERS WHERE NAME = ?",
                Integer.class,
                "alice");
    }

    @Test
    void registerPageForwardsToReactHostWithoutLogin() throws Exception {
        mockMvc.perform(get("/Register"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/WEB-INF/jsp/registerView.jsp"));
    }

    @Test
    void mainPageForwardsToReactHostWhenLoginUserExists() throws Exception {
        mockMvc.perform(get("/Main")
                        .with(user("alice"))
                        .sessionAttr("loginUser", new User(aliceId, "alice", "hashed-pass")))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/WEB-INF/jsp/main.jsp"));
    }

    @Test
    void profilePagePassesTargetUserIdToReactHost() throws Exception {
        mockMvc.perform(get("/Profile")
                        .param("userId", String.valueOf(aliceId))
                        .with(user("alice"))
                        .sessionAttr("loginUser", new User(aliceId, "alice", "hashed-pass")))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/WEB-INF/jsp/profile.jsp"))
                .andExpect(model().attribute("targetUserId", aliceId));
    }

    @Test
    void followerPagePassesTargetUserToReactHost() throws Exception {
        mockMvc.perform(get("/FollowerList")
                        .param("userId", String.valueOf(aliceId))
                        .with(user("alice"))
                        .sessionAttr("loginUser", new User(aliceId, "alice", "hashed-pass")))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/WEB-INF/jsp/followerList.jsp"))
                .andExpect(request().attribute("targetUser", instanceOf(User.class)));
    }
}
