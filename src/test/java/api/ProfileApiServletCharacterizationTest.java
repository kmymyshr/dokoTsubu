package api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.ServletTestSupport.captureResponseBody;
import static support.ServletTestSupport.mockRequest;
import static support.ServletTestSupport.withJsonBody;

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
 * ProfileApiServletのレスポンス形状と更新ルールを固定する特性テスト。
 *
 * <p>Phase11でプロフィール画面をReact化したため、JSP属性の代わりになるJSONと
 * 本人だけが自己紹介を更新できる制約をここで守る。</p>
 */
@SpringBootTest(classes = com.example.dokotsubu.DokoTsubuApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:profileApiCharacterization;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ProfileApiServletCharacterizationTest {

    private final ProfileApiServlet servlet = new ProfileApiServlet();
    private final UserDAO userDAO = new UserDAO();
    private final FollowDAO followDAO = new FollowDAO();

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void resetTables() {
        TestDatabaseSupport.clearAllTables(jdbc);
    }

    private User createUser(String name, String bio) {
        userDAO.create(new User(name, "hashed-pass", bio));
        return userDAO.findByName(name);
    }

    private JsonNode readJson(StringWriter body) throws Exception {
        return ObjectMapperFactory.getObjectMapper().readTree(body.toString());
    }

    @Test
    void doGet_returnsProfileForReact() throws Exception {
        User alice = createUser("alice", "hello");
        User bob = createUser("bob", "");
        followDAO.follow(alice.getId(), bob.getId());

        HttpServletRequest request = mockRequest(alice);
        when(request.getParameter("userId")).thenReturn(String.valueOf(bob.getId()));
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        JsonNode json = readJson(body);
        assertEquals(bob.getId(), json.get("user").get("id").asInt());
        assertEquals("bob", json.get("user").get("name").asText());
        assertEquals(false, json.get("ownProfile").asBoolean());
        assertEquals(true, json.get("following").asBoolean());
        assertEquals(1, json.get("followers").asInt());
        assertEquals(0, json.get("followingCount").asInt());
    }

    @Test
    void doPut_updatesOwnBioAndReturnsProfile() throws Exception {
        User alice = createUser("alice", "");
        HttpServletRequest request = mockRequest(alice);
        withJsonBody(request, "{\"userId\":" + alice.getId() + ",\"bio\":\"  hello  \"}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPut(request, response);

        JsonNode json = readJson(body);
        assertEquals("hello", json.get("user").get("bio").asText());
        assertEquals(true, json.get("ownProfile").asBoolean());
    }

    @Test
    void doPut_returns403_whenUpdatingOtherUser() throws Exception {
        User alice = createUser("alice", "");
        User bob = createUser("bob", "");
        HttpServletRequest request = mockRequest(alice);
        withJsonBody(request, "{\"userId\":" + bob.getId() + ",\"bio\":\"bad\"}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPut(request, response);

        verify(response).setStatus(403);
        assertEquals("FORBIDDEN", readJson(body).get("code").asText());
    }
}
