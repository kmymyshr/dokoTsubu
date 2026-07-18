package com.example.dokotsubu.web;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
 * <p>旧画面ServletとJSPホストを削除した後も、既存URLがReactホストHTMLを返すことと、
 * Reactが必要とするdata属性が渡ることを確認する。</p>
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
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("data-react-page=\"register\"")));
    }

    @Test
    void mainPageForwardsToReactHostWhenLoginUserExists() throws Exception {
        mockMvc.perform(get("/Main")
                        .with(user("alice"))
                        .sessionAttr("loginUser", new User(aliceId, "alice", "hashed-pass")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/react/assets/main.js")));
    }

    @Test
    void profilePagePassesTargetUserIdToReactHost() throws Exception {
        mockMvc.perform(get("/Profile")
                        .param("userId", String.valueOf(aliceId))
                        .with(user("alice"))
                        .sessionAttr("loginUser", new User(aliceId, "alice", "hashed-pass")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-react-page=\"profile\"")))
                .andExpect(content().string(containsString("data-target-user-id=\"" + aliceId + "\"")));
    }

    @Test
    void followerPagePassesTargetUserToReactHost() throws Exception {
        mockMvc.perform(get("/FollowerList")
                        .param("userId", String.valueOf(aliceId))
                        .with(user("alice"))
                        .sessionAttr("loginUser", new User(aliceId, "alice", "hashed-pass")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-react-page=\"follow-list\"")))
                .andExpect(content().string(containsString("data-follow-list-type=\"followers\"")))
                .andExpect(content().string(containsString("data-target-user-id=\"" + aliceId + "\"")));
    }

    @Test
    void unauthenticatedMainRedirectsToRoot() throws Exception {
        mockMvc.perform(get("/Main"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/"));
    }
}
