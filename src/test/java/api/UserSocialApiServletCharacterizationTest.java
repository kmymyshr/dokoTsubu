package api;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * Phase20で追加したユーザー系ソーシャルAPIの振る舞いを固定するテスト。
 *
 * <p>旧 {@code /FollowUser} から {@code /api/users/{id}/follow} へ移したため、
 * URL解析、フォロー切り替え、件数返却、代表的なエラーをここで確認する。</p>
 */
@SpringBootTest(classes = com.example.dokotsubu.DokoTsubuApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:userSocialApiCharacterization;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserSocialApiServletCharacterizationTest {

    private final UserSocialApiServlet servlet = new UserSocialApiServlet();
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
    void doPost_togglesFollowAndReturnsCurrentState() throws Exception {
        User loginUser = createUser("alice");
        User targetUser = createUser("bob");
        HttpServletRequest request = mockRequest(loginUser);
        when(request.getPathInfo()).thenReturn("/" + targetUser.getId() + "/follow");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPost(request, response);

        JsonNode json = readJson(body);
        assertEquals(true, json.get("following").asBoolean());
        assertEquals(1, json.get("followers").asInt());
        assertEquals(true, followDAO.isFollowing(loginUser.getId(), targetUser.getId()));
    }

    @Test
    void doPost_returns400_whenFollowingSelf() throws Exception {
        User loginUser = createUser("alice");
        HttpServletRequest request = mockRequest(loginUser);
        when(request.getPathInfo()).thenReturn("/" + loginUser.getId() + "/follow");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPost(request, response);

        verify(response).setStatus(400);
        assertEquals("CANNOT_FOLLOW_SELF", readJson(body).get("code").asText());
    }

    @Test
    void doPost_returns404_whenTargetUserDoesNotExist() throws Exception {
        User loginUser = createUser("alice");
        HttpServletRequest request = mockRequest(loginUser);
        when(request.getPathInfo()).thenReturn("/9999/follow");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPost(request, response);

        verify(response).setStatus(404);
        assertEquals("USER_NOT_FOUND", readJson(body).get("code").asText());
    }
}
