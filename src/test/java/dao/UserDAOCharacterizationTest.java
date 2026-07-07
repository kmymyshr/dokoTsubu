package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.User;
import support.TestDatabaseSupport;

/**
 * UserDAOの現状の挙動を固定する特性テスト。
 * (モダナイゼーション計画 Phase0: 安全網構築)
 */
class UserDAOCharacterizationTest {

    private final UserDAO userDAO = new UserDAO();

    @BeforeAll
    static void setUpDatabase() {
        TestDatabaseSupport.useFreshInMemoryDatabase("userdao_characterization");
    }

    @BeforeEach
    void resetTables() {
        TestDatabaseSupport.clearAllTables();
    }

    @Test
    void findById_returnsNull_whenNotFound() {
        assertNull(userDAO.findById(9999));
    }

    @Test
    void findByName_returnsNull_whenNotFound() {
        assertNull(userDAO.findByName("nobody"));
    }

    @Test
    void create_persistsNameAndPassAndBio() {
        boolean created = userDAO.create(new User("alice", "hashed-pass", "hello"));

        assertTrue(created);
        User found = userDAO.findByName("alice");
        assertEquals("alice", found.getName());
        assertEquals("hashed-pass", found.getPass());
        assertEquals("hello", found.getBio());
    }

    @Test
    void create_normalizesNullBioToEmptyString() {
        userDAO.create(new User("alice", "hashed-pass", null));

        assertEquals("", userDAO.findByName("alice").getBio());
    }

    @Test
    void create_returnsFalse_onDuplicateName() {
        userDAO.create(new User("alice", "hashed-pass"));

        boolean secondAttempt = userDAO.create(new User("alice", "another-pass"));

        assertFalse(secondAttempt);
    }

    @Test
    void updateBio_persistsNewBio() {
        userDAO.create(new User("alice", "hashed-pass"));
        int id = userDAO.findByName("alice").getId();

        boolean updated = userDAO.updateBio(id, "new bio");

        assertTrue(updated);
        assertEquals("new bio", userDAO.findById(id).getBio());
    }

    @Test
    void updateBio_normalizesNullToEmptyString() {
        userDAO.create(new User("alice", "hashed-pass", "old bio"));
        int id = userDAO.findByName("alice").getId();

        userDAO.updateBio(id, null);

        assertEquals("", userDAO.findById(id).getBio());
    }

    @Test
    void updateBio_returnsFalse_whenUserDoesNotExist() {
        assertFalse(userDAO.updateBio(9999, "bio"));
    }
}
