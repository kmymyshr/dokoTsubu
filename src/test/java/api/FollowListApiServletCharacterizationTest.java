package api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.ServletTestSupport.captureResponseBody;
import static support.ServletTestSupport.mockRequest;

import java.io.StringWriter;

import com.fasterxml.jackson.databind.JsonNode;
import dao.FollowDAO;
import dao.UserDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import support.TestDatabaseSupport;
import util.ObjectMapperFactory;

/**
 * FollowListApiServletのレスポンス形状を固定する特性テスト。
 *
 * <p>Phase8でフォロー一覧のReact化に備えて追加したAPIなので、JSPからReactへ移す際に
 * 必要な対象ユーザー、件数、ログインユーザーから見たフォロー状態が崩れないことを確認する。</p>
 */
@SpringBootTest(classes = com.example.dokotsubu.DokoTsubuApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:followListApiCharacterization;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FollowListApiServletCharacterizationTest {

    private final FollowListApiServlet servlet = new FollowListApiServlet();
    private final UserDAO userDAO = new UserDAO();
    private final FollowDAO followDAO = new FollowDAO();

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void resetTables() {
        TestDatabaseSupport.clearAllTables(jdbc);
    }

    private User createUser(String name) {
        userDAO.create(new User(name, "hashed-pass"));
        return userDAO.findByName(name);
    }

    private JsonNode readJson(StringWriter body) throws Exception {
        return ObjectMapperFactory.getObjectMapper().readTree(body.toString());
    }

    @Test
    void doGet_returns401_whenNotLoggedIn() throws Exception {
        HttpServletRequest request = mockRequest(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        verify(response).setStatus(401);
        assertEquals("UNAUTHORIZED", readJson(body).get("code").asText());
    }

    @Test
    void doGet_returnsFollowersWithFollowStateForReact() throws Exception {
        User alice = createUser("alice");
        User bob = createUser("bob");
        User charlie = createUser("charlie");
        followDAO.follow(bob.getId(), alice.getId());
        followDAO.follow(charlie.getId(), alice.getId());
        followDAO.follow(bob.getId(), charlie.getId());

        HttpServletRequest request = mockRequest(bob);
        when(request.getParameter("userId")).thenReturn(String.valueOf(alice.getId()));
        when(request.getParameter("type")).thenReturn("followers");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        JsonNode json = readJson(body);
        assertEquals("followers", json.get("type").asText());
        assertEquals(alice.getId(), json.get("targetUser").get("id").asInt());
        assertEquals("alice", json.get("targetUser").get("name").asText());
        assertEquals(2, json.get("count").asInt());
        assertEquals(2, json.get("users").size());

        JsonNode bobRow = json.get("users").get(0);
        assertEquals("bob", bobRow.get("name").asText());
        assertTrue(bobRow.get("me").asBoolean());
        assertFalse(bobRow.get("followedByMe").asBoolean());

        JsonNode charlieRow = json.get("users").get(1);
        assertEquals("charlie", charlieRow.get("name").asText());
        assertFalse(charlieRow.get("me").asBoolean());
        assertTrue(charlieRow.get("followedByMe").asBoolean());
    }

    @Test
    void doGet_returnsFollowingList() throws Exception {
        User alice = createUser("alice");
        User bob = createUser("bob");
        User charlie = createUser("charlie");
        followDAO.follow(bob.getId(), alice.getId());
        followDAO.follow(bob.getId(), charlie.getId());

        HttpServletRequest request = mockRequest(bob);
        when(request.getParameter("userId")).thenReturn(String.valueOf(bob.getId()));
        when(request.getParameter("type")).thenReturn("following");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        JsonNode json = readJson(body);
        assertEquals("following", json.get("type").asText());
        assertEquals(bob.getId(), json.get("targetUser").get("id").asInt());
        assertEquals(2, json.get("count").asInt());
        assertEquals("alice", json.get("users").get(0).get("name").asText());
        assertTrue(json.get("users").get(0).get("followedByMe").asBoolean());
        assertEquals("charlie", json.get("users").get(1).get("name").asText());
        assertTrue(json.get("users").get(1).get("followedByMe").asBoolean());
    }

    @Test
    void doGet_returns400_whenTypeIsInvalid() throws Exception {
        User viewer = createUser("viewer");
        HttpServletRequest request = mockRequest(viewer);
        when(request.getParameter("userId")).thenReturn(String.valueOf(viewer.getId()));
        when(request.getParameter("type")).thenReturn("unknown");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        verify(response).setStatus(400);
        assertEquals("INVALID_TYPE", readJson(body).get("code").asText());
    }
}
