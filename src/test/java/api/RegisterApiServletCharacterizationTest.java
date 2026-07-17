package api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static support.ServletTestSupport.captureResponseBody;
import static support.ServletTestSupport.mockRequest;
import static support.ServletTestSupport.withJsonBody;

import java.io.StringWriter;

import com.fasterxml.jackson.databind.JsonNode;
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
 * RegisterApiServletのレスポンス形状を固定する特性テスト。
 *
 * <p>Phase10で登録画面をReact化したため、Reactが扱う成功/失敗JSONの形をここで守る。</p>
 */
@SpringBootTest(classes = com.example.dokotsubu.DokoTsubuApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:registerApiCharacterization;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RegisterApiServletCharacterizationTest {

    private final RegisterApiServlet servlet = new RegisterApiServlet();
    private final UserDAO userDAO = new UserDAO();

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void resetTables() {
        TestDatabaseSupport.clearAllTables(jdbc);
    }

    private JsonNode readJson(StringWriter body) throws Exception {
        return ObjectMapperFactory.getObjectMapper().readTree(body.toString());
    }

    @Test
    void doPost_registersUserAndReturnsCreated() throws Exception {
        HttpServletRequest request = mockRequest(null);
        withJsonBody(request, "{\"name\":\"alice\",\"pass\":\"secret\"}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPost(request, response);

        verify(response).setStatus(201);
        JsonNode json = readJson(body);
        assertEquals("alice", json.get("name").asText());
        assertEquals("ユーザー登録が完了しました", json.get("message").asText());
        assertEquals("/", json.get("loginUrl").asText());
    }

    @Test
    void doPost_returns400_whenRequiredFieldsAreMissing() throws Exception {
        HttpServletRequest request = mockRequest(null);
        withJsonBody(request, "{\"name\":\"\",\"pass\":\"\"}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPost(request, response);

        verify(response).setStatus(400);
        assertEquals("REQUIRED_FIELD_MISSING", readJson(body).get("code").asText());
    }

    @Test
    void doPost_returns409_whenUserAlreadyExists() throws Exception {
        userDAO.create(new User("alice", "hashed-pass"));
        HttpServletRequest request = mockRequest(null);
        withJsonBody(request, "{\"name\":\"alice\",\"pass\":\"secret\"}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPost(request, response);

        verify(response).setStatus(409);
        assertEquals("USER_ALREADY_EXISTS", readJson(body).get("code").asText());
    }
}
