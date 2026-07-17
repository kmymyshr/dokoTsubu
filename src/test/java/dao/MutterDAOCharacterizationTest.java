package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import model.Mutter;
import model.User;
import support.TestDatabaseSupport;

/**
 * MutterDAOの「今の挙動」をそのまま固定するテスト。
 * リファクタリング(接続プール導入・N+1解消・Repository化)の際に、
 * SQLの結果や境界条件が意図せず変わっていないかを検知するための安全網であり、
 * 「あるべき仕様」を検証するテストではない。
 * (モダナイゼーション計画 Phase0: 安全網構築)
 */
@SpringBootTest(classes = com.example.dokotsubu.DokoTsubuApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:dataJdbcCharacterization;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MutterDAOCharacterizationTest {

    private final MutterDAO mutterDAO = new MutterDAO();
    private final UserDAO userDAO = new UserDAO();

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void resetTables() {
        TestDatabaseSupport.clearAllTables(jdbc);
    }

    private int createUser(String name) {
        userDAO.create(new User(name, "hashed-pass"));
        return userDAO.findByName(name).getId();
    }

    @Test
    void findLatest_returnsNewestFirst_andRespectsLimit() {
        int userId = createUser("alice");
        mutterDAO.create(new Mutter(userId, "first"));
        mutterDAO.create(new Mutter(userId, "second"));
        mutterDAO.create(new Mutter(userId, "third"));

        List<Mutter> latestTwo = mutterDAO.findLatest(2);

        assertEquals(2, latestTwo.size());
        assertEquals("third", latestTwo.get(0).getText());
        assertEquals("second", latestTwo.get(1).getText());
        assertEquals("alice", latestTwo.get(0).getUserName());
    }

    @Test
    void findByCursor_returnsOnlyOlderThanCursor() {
        int userId = createUser("alice");
        Mutter first = mutterDAO.createAndReturn(new Mutter(userId, "first"));
        mutterDAO.createAndReturn(new Mutter(userId, "second"));
        mutterDAO.createAndReturn(new Mutter(userId, "third"));

        List<Mutter> olderThanThird = mutterDAO.findByCursor(first.getId() + 2, 10);

        assertEquals(2, olderThanThird.size());
        assertTrue(olderThanThird.stream().allMatch(m -> m.getId() < first.getId() + 2));
    }

    @Test
    void findPage_combinesKeywordAndCursor() {
        int userId = createUser("alice");
        mutterDAO.createAndReturn(new Mutter(userId, "hello world"));
        Mutter helloAgain = mutterDAO.createAndReturn(new Mutter(userId, "hello again"));
        mutterDAO.createAndReturn(new Mutter(userId, "unrelated"));

        List<Mutter> page = mutterDAO.findPage("hello", helloAgain.getId(), 10);

        assertEquals(1, page.size());
        assertEquals("hello world", page.get(0).getText());
    }

    @Test
    void findPage_keywordIsNotEscapedForSqlWildcards() {
        // 現状の実装は "%" + keyword + "%" をそのままLIKEに渡しており、
        // キーワードに含まれる % や _ はSQLワイルドカードとして働いてしまう。
        // これは既存の既知の挙動であり、Phase0では「直さず・そのまま固定」する。
        int userId = createUser("alice");
        mutterDAO.createAndReturn(new Mutter(userId, "100% done"));
        mutterDAO.createAndReturn(new Mutter(userId, "abc"));

        List<Mutter> page = mutterDAO.findPage("%", null, 10);

        // "%" はワイルドカードとして解釈されるため、全件がヒットしてしまう。
        assertEquals(2, page.size());
    }

    @Test
    void findById_returnsNull_whenNotFound() {
        assertNull(mutterDAO.findById(9999));
    }

    @Test
    void createAndReturn_assignsIdAndDefaultVersionAndTimestamp() {
        int userId = createUser("alice");

        Mutter created = mutterDAO.createAndReturn(new Mutter(userId, "hello"));

        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals(0, created.getVersion());
        assertNotNull(created.getCreatedAt());
    }

    @Test
    void create_rejectsUnknownUserIdByForeignKey() {
        boolean created = mutterDAO.create(new Mutter(999999, "orphan mutter"));

        assertFalse(created);
    }

    @Test
    void update_succeedsAndIncrementsVersion_whenVersionMatches() {
        int userId = createUser("alice");
        Mutter created = mutterDAO.createAndReturn(new Mutter(userId, "before"));

        boolean updated = mutterDAO.update(
                new Mutter(created.getId(), userId, "alice", "after", created.getVersion()));

        assertTrue(updated);
        Mutter reloaded = mutterDAO.findById(created.getId());
        assertEquals("after", reloaded.getText());
        assertEquals(created.getVersion() + 1, reloaded.getVersion());
    }

    @Test
    void update_returnsFalse_onStaleVersion() {
        int userId = createUser("alice");
        Mutter created = mutterDAO.createAndReturn(new Mutter(userId, "before"));
        int staleVersion = created.getVersion();
        mutterDAO.update(new Mutter(created.getId(), userId, "alice", "first update", staleVersion));

        boolean conflicting = mutterDAO.update(
                new Mutter(created.getId(), userId, "alice", "second update", staleVersion));

        assertFalse(conflicting);
        assertEquals("first update", mutterDAO.findById(created.getId()).getText());
    }

    @Test
    void update_returnsFalse_whenNotOwner() {
        int owner = createUser("alice");
        int otherUser = createUser("bob");
        Mutter created = mutterDAO.createAndReturn(new Mutter(owner, "before"));

        boolean updated = mutterDAO.update(
                new Mutter(created.getId(), otherUser, "bob", "hijacked", created.getVersion()));

        assertFalse(updated);
        assertEquals("before", mutterDAO.findById(created.getId()).getText());
    }

    @Test
    void delete_succeedsForOwner_andFailsForNonOwner() {
        int owner = createUser("alice");
        int otherUser = createUser("bob");
        Mutter created = mutterDAO.createAndReturn(new Mutter(owner, "to be deleted"));

        assertFalse(mutterDAO.delete(created.getId(), otherUser));
        assertNotNull(mutterDAO.findById(created.getId()));

        assertTrue(mutterDAO.delete(created.getId(), owner));
        assertNull(mutterDAO.findById(created.getId()));
    }

    @Test
    void search_returnsSubstringMatchesNewestFirst() {
        int userId = createUser("alice");
        mutterDAO.createAndReturn(new Mutter(userId, "foo bar"));
        mutterDAO.createAndReturn(new Mutter(userId, "no match"));
        mutterDAO.createAndReturn(new Mutter(userId, "another bar"));

        List<Mutter> results = mutterDAO.search("bar");

        assertEquals(2, results.size());
        assertEquals("another bar", results.get(0).getText());
        assertEquals("foo bar", results.get(1).getText());
    }
}
