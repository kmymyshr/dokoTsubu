package api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.ServletTestSupport.captureResponseBody;
import static support.ServletTestSupport.mockRequest;
import static support.ServletTestSupport.withJsonBody;

import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import com.fasterxml.jackson.databind.JsonNode;

import dao.FollowDAO;
import dao.LikeDAO;
import dao.MutterDAO;
import dao.UserDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Mutter;
import model.User;
import support.TestDatabaseSupport;
import util.ObjectMapperFactory;

/**
 * MutterApiServletの現状の挙動を固定する特性テスト。
 *
 * このServletは一覧取得時に投稿ごとへいいね数/いいね済み/フォロー済みを
 * 追加クエリで問い合わせるN+1構成になっており(Step1分析で指摘済み)、
 * 将来そこを1クエリに最適化する際、レスポンスの形状が変わっていないことを
 * このテストで検知できるようにしておく。
 * (モダナイゼーション計画 Phase0: 安全網構築)
 */
@SpringBootTest(classes = com.example.dokotsubu.DokoTsubuApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:dataJdbcCharacterization;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MutterApiServletCharacterizationTest {

    private final MutterApiServlet servlet = new MutterApiServlet();
    private final UserDAO userDAO = new UserDAO();
    private final MutterDAO mutterDAO = new MutterDAO();
    private final LikeDAO likeDAO = new LikeDAO();
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

    private Mutter createMutter(int userId, String text) {
        return mutterDAO.createAndReturn(new Mutter(userId, text));
    }

    private JsonNode readJson(StringWriter body) throws Exception {
        return ObjectMapperFactory.getObjectMapper().readTree(body.toString());
    }

    // ---------- doGet ----------

    @Test
    void doGet_returns401_whenNotLoggedIn() throws Exception {
        HttpServletRequest request = mockRequest(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        captureResponseBody(response);

        servlet.doGet(request, response);

        verify(response).setStatus(401);
    }

    @Test
    void doGet_singleResource_returnsMutterWithLikeAndFollowFlags() throws Exception {
        User author = createUser("alice");
        User viewer = createUser("bob");
        Mutter mutter = createMutter(author.getId(), "hello");
        likeDAO.addLike(mutter.getId(), viewer.getId());
        followDAO.follow(viewer.getId(), author.getId());

        HttpServletRequest request = mockRequest(viewer);
        when(request.getPathInfo()).thenReturn("/" + mutter.getId());
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        JsonNode json = readJson(body);
        assertEquals(mutter.getId(), json.get("id").asInt());
        assertEquals("alice", json.get("userName").asText());
        assertEquals(1, json.get("likeCount").asInt());
        assertTrue(json.get("likedByMe").asBoolean());
        assertTrue(json.get("followedByMe").asBoolean());
    }

    @Test
    void doGet_singleResource_returns404_whenNotFound() throws Exception {
        User viewer = createUser("bob");
        HttpServletRequest request = mockRequest(viewer);
        when(request.getPathInfo()).thenReturn("/9999");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        verify(response).setStatus(404);
        assertEquals("MUTTER_NOT_FOUND", readJson(body).get("code").asText());
    }

    @Test
    void doGet_singleResource_returns404_whenIdIsNotNumeric() throws Exception {
        // 現状の実装は、パスが "/数字" の形になっていない場合は
        // 「不正なID(400)」ではなく「リソースが存在しない(404)」を返す。
        User viewer = createUser("bob");
        HttpServletRequest request = mockRequest(viewer);
        when(request.getPathInfo()).thenReturn("/abc");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        verify(response).setStatus(404);
        assertEquals("RESOURCE_NOT_FOUND", readJson(body).get("code").asText());
    }

    @Test
    void doGet_singleResource_returns400_whenIdIsZero() throws Exception {
        // "/0" は正規表現 \d+ にはマッチするが、0以下は不正なIDとして400になる。
        User viewer = createUser("bob");
        HttpServletRequest request = mockRequest(viewer);
        when(request.getPathInfo()).thenReturn("/0");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        verify(response).setStatus(400);
        assertEquals("INVALID_ID", readJson(body).get("code").asText());
    }

    @Test
    void doGet_list_returnsPageWithNextCursorWhenMoreExist() throws Exception {
        User author = createUser("alice");
        createMutter(author.getId(), "first");
        createMutter(author.getId(), "second");
        Mutter third = createMutter(author.getId(), "third");

        HttpServletRequest request = mockRequest(author);
        when(request.getParameter("limit")).thenReturn("2");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        JsonNode json = readJson(body);
        assertEquals(2, json.get("mutters").size());
        assertEquals("third", json.get("mutters").get(0).get("text").asText());
        assertTrue(json.get("hasNext").asBoolean());
        assertEquals(third.getId() - 1, json.get("nextCursor").asInt());
    }

    @Test
    void doGet_list_returnsAggregatedLikeAndFollowData() throws Exception {
        User author = createUser("alice");
        User viewer = createUser("bob");
        User anotherUser = createUser("carol");
        Mutter mutter = createMutter(author.getId(), "hello");
        likeDAO.addLike(mutter.getId(), viewer.getId());
        likeDAO.addLike(mutter.getId(), anotherUser.getId());
        followDAO.follow(viewer.getId(), author.getId());

        HttpServletRequest request = mockRequest(viewer);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        JsonNode item = readJson(body).get("mutters").get(0);
        assertEquals(2, item.get("likeCount").asInt());
        assertTrue(item.get("likedByMe").asBoolean());
        assertTrue(item.get("followedByMe").asBoolean());
    }

    @Test
    void doGet_list_returns400_onKeywordTooLong() throws Exception {
        User author = createUser("alice");
        HttpServletRequest request = mockRequest(author);
        when(request.getParameter("keyword")).thenReturn("a".repeat(101));
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        verify(response).setStatus(400);
        assertEquals("KEYWORD_TOO_LONG", readJson(body).get("code").asText());
    }

    @Test
    void doGet_list_returns400_onInvalidCursor() throws Exception {
        User author = createUser("alice");
        HttpServletRequest request = mockRequest(author);
        when(request.getParameter("cursor")).thenReturn("not-a-number");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        verify(response).setStatus(400);
        assertEquals("INVALID_CURSOR", readJson(body).get("code").asText());
    }

    @Test
    void doGet_list_returns400_onInvalidLimit() throws Exception {
        User author = createUser("alice");
        HttpServletRequest request = mockRequest(author);
        when(request.getParameter("limit")).thenReturn("not-a-number");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doGet(request, response);

        verify(response).setStatus(400);
        assertEquals("INVALID_LIMIT", readJson(body).get("code").asText());
    }

    // ---------- doPost ----------

    @Test
    void doPost_returns401_whenNotLoggedIn() throws Exception {
        HttpServletRequest request = mockRequest(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        captureResponseBody(response);

        servlet.doPost(request, response);

        verify(response).setStatus(401);
    }

    @Test
    void doPost_returns405_whenResourceIdInPath() throws Exception {
        User author = createUser("alice");
        HttpServletRequest request = mockRequest(author);
        when(request.getPathInfo()).thenReturn("/5");
        HttpServletResponse response = mock(HttpServletResponse.class);
        captureResponseBody(response);

        servlet.doPost(request, response);

        verify(response).setStatus(405);
    }

    @Test
    void doPost_returns400_onBlankText() throws Exception {
        User author = createUser("alice");
        HttpServletRequest request = mockRequest(author);
        withJsonBody(request, "{\"text\":\"   \"}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPost(request, response);

        verify(response).setStatus(400);
        assertEquals("TEXT_REQUIRED", readJson(body).get("code").asText());
    }

    @Test
    void doPost_returns201_withLocationHeader_onSuccess() throws Exception {
        User author = createUser("alice");
        HttpServletRequest request = mockRequest(author);
        withJsonBody(request, "{\"text\":\"hello world\"}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPost(request, response);

        verify(response).setStatus(201);
        verify(response).setHeader("Location", "/api/mutters/1");
        JsonNode json = readJson(body);
        assertEquals("hello world", json.get("text").asText());
        assertEquals(0, json.get("likeCount").asInt());
        assertFalse(json.get("likedByMe").asBoolean());
        assertFalse(json.get("followedByMe").asBoolean());
    }

    // ---------- doPut ----------

    @Test
    void doPut_returns400_whenVersionMissing() throws Exception {
        User author = createUser("alice");
        Mutter mutter = createMutter(author.getId(), "before");
        HttpServletRequest request = mockRequest(author);
        when(request.getPathInfo()).thenReturn("/" + mutter.getId());
        withJsonBody(request, "{\"text\":\"after\"}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPut(request, response);

        verify(response).setStatus(400);
        assertEquals("INVALID_VERSION", readJson(body).get("code").asText());
    }

    @Test
    void doPut_returns404_whenMutterNotFound() throws Exception {
        User author = createUser("alice");
        HttpServletRequest request = mockRequest(author);
        when(request.getPathInfo()).thenReturn("/9999");
        withJsonBody(request, "{\"text\":\"after\",\"version\":0}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPut(request, response);

        verify(response).setStatus(404);
        assertEquals("MUTTER_NOT_FOUND", readJson(body).get("code").asText());
    }

    @Test
    void doPut_returns403_whenNotOwner() throws Exception {
        User author = createUser("alice");
        User otherUser = createUser("bob");
        Mutter mutter = createMutter(author.getId(), "before");
        HttpServletRequest request = mockRequest(otherUser);
        when(request.getPathInfo()).thenReturn("/" + mutter.getId());
        withJsonBody(request, "{\"text\":\"after\",\"version\":0}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPut(request, response);

        verify(response).setStatus(403);
        assertEquals("FORBIDDEN", readJson(body).get("code").asText());
    }

    @Test
    void doPut_returns409_onStaleVersion() throws Exception {
        User author = createUser("alice");
        Mutter mutter = createMutter(author.getId(), "before");
        mutterDAO.update(new Mutter(mutter.getId(), author.getId(), "alice", "already updated", 0));

        HttpServletRequest request = mockRequest(author);
        when(request.getPathInfo()).thenReturn("/" + mutter.getId());
        withJsonBody(request, "{\"text\":\"stale update\",\"version\":0}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPut(request, response);

        verify(response).setStatus(409);
        assertEquals("UPDATE_CONFLICT", readJson(body).get("code").asText());
    }

    @Test
    void doPut_returns200_onSuccess() throws Exception {
        User author = createUser("alice");
        Mutter mutter = createMutter(author.getId(), "before");
        HttpServletRequest request = mockRequest(author);
        when(request.getPathInfo()).thenReturn("/" + mutter.getId());
        withJsonBody(request, "{\"text\":\"after\",\"version\":0}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = captureResponseBody(response);

        servlet.doPut(request, response);

        verify(response, org.mockito.Mockito.never()).setStatus(org.mockito.ArgumentMatchers.intThat(s -> s >= 400));
        JsonNode json = readJson(body);
        assertEquals("after", json.get("text").asText());
        assertEquals(1, json.get("version").asInt());
    }

    // ---------- doDelete ----------

    @Test
    void doDelete_returns404_whenMutterNotFound() throws Exception {
        User author = createUser("alice");
        HttpServletRequest request = mockRequest(author);
        when(request.getPathInfo()).thenReturn("/9999");
        HttpServletResponse response = mock(HttpServletResponse.class);
        captureResponseBody(response);

        servlet.doDelete(request, response);

        verify(response).setStatus(404);
    }

    @Test
    void doDelete_returns403_whenNotOwner() throws Exception {
        User author = createUser("alice");
        User otherUser = createUser("bob");
        Mutter mutter = createMutter(author.getId(), "to delete");
        HttpServletRequest request = mockRequest(otherUser);
        when(request.getPathInfo()).thenReturn("/" + mutter.getId());
        HttpServletResponse response = mock(HttpServletResponse.class);
        captureResponseBody(response);

        servlet.doDelete(request, response);

        verify(response).setStatus(403);
        assertNotNull(mutterDAO.findById(mutter.getId()));
    }

    @Test
    void doDelete_returns204_onSuccess() throws Exception {
        User author = createUser("alice");
        Mutter mutter = createMutter(author.getId(), "to delete");
        HttpServletRequest request = mockRequest(author);
        when(request.getPathInfo()).thenReturn("/" + mutter.getId());
        HttpServletResponse response = mock(HttpServletResponse.class);
        captureResponseBody(response);

        servlet.doDelete(request, response);

        verify(response).setStatus(204);
        assertNull(mutterDAO.findById(mutter.getId()));
    }
}
