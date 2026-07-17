package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * LikeDAOの現状の挙動を固定する特性テスト。
 * (モダナイゼーション計画 Phase0: 安全網構築)
 */
@SpringBootTest(classes = com.example.dokotsubu.DokoTsubuApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:dataJdbcCharacterization;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LikeDAOCharacterizationTest {

    private final LikeDAO likeDAO = new LikeDAO();
    private final UserDAO userDAO = new UserDAO();
    private final MutterDAO mutterDAO = new MutterDAO();

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

    private int createMutter(int userId, String text) {
        return mutterDAO.createAndReturn(new Mutter(userId, text)).getId();
    }

    @Test
    void hasLiked_isFalse_beforeAnyLike() {
        int userId = createUser("alice");
        int mutterId = createMutter(userId, "hello");

        assertFalse(likeDAO.hasLiked(mutterId, userId));
        assertEquals(0, likeDAO.countLikes(mutterId));
    }

    @Test
    void addLike_thenHasLikedIsTrue_andCountIncrements() {
        int author = createUser("alice");
        int liker = createUser("bob");
        int mutterId = createMutter(author, "hello");

        boolean added = likeDAO.addLike(mutterId, liker);

        assertTrue(added);
        assertTrue(likeDAO.hasLiked(mutterId, liker));
        assertEquals(1, likeDAO.countLikes(mutterId));
    }

    @Test
    void addLike_returnsFalse_whenAlreadyLiked() {
        // MUTTER_ID+USER_IDにUNIQUE制約があるため、二重にいいねしようとすると失敗する。
        int author = createUser("alice");
        int liker = createUser("bob");
        int mutterId = createMutter(author, "hello");
        likeDAO.addLike(mutterId, liker);

        boolean secondAttempt = likeDAO.addLike(mutterId, liker);

        assertFalse(secondAttempt);
        assertEquals(1, likeDAO.countLikes(mutterId));
    }

    @Test
    void removeLike_makesHasLikedFalseAgain() {
        int author = createUser("alice");
        int liker = createUser("bob");
        int mutterId = createMutter(author, "hello");
        likeDAO.addLike(mutterId, liker);

        boolean removed = likeDAO.removeLike(mutterId, liker);

        assertTrue(removed);
        assertFalse(likeDAO.hasLiked(mutterId, liker));
        assertEquals(0, likeDAO.countLikes(mutterId));
    }

    @Test
    void toggleLike_addsWhenNotLiked_andRemovesWhenAlreadyLiked() {
        int author = createUser("alice");
        int liker = createUser("bob");
        int mutterId = createMutter(author, "hello");

        assertTrue(likeDAO.toggleLike(mutterId, liker));
        assertTrue(likeDAO.hasLiked(mutterId, liker));

        assertTrue(likeDAO.toggleLike(mutterId, liker));
        assertFalse(likeDAO.hasLiked(mutterId, liker));
    }

    @Test
    void countLikes_countsMultipleUsers() {
        int author = createUser("alice");
        int likerA = createUser("bob");
        int likerB = createUser("carol");
        int mutterId = createMutter(author, "hello");

        likeDAO.addLike(mutterId, likerA);
        likeDAO.addLike(mutterId, likerB);

        assertEquals(2, likeDAO.countLikes(mutterId));
    }
}
