package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.User;
import support.TestDatabaseSupport;

/**
 * FollowDAOの現状の挙動を固定する特性テスト。
 * (モダナイゼーション計画 Phase0: 安全網構築)
 */
class FollowDAOCharacterizationTest {

    private final FollowDAO followDAO = new FollowDAO();
    private final UserDAO userDAO = new UserDAO();

    @BeforeAll
    static void setUpDatabase() {
        TestDatabaseSupport.useFreshInMemoryDatabase("followdao_characterization");
    }

    @BeforeEach
    void resetTables() {
        TestDatabaseSupport.clearAllTables();
    }

    private int createUser(String name) {
        userDAO.create(new User(name, "hashed-pass"));
        return userDAO.findByName(name).getId();
    }

    @Test
    void isFollowing_isFalse_beforeAnyFollow() {
        int follower = createUser("alice");
        int followee = createUser("bob");

        assertFalse(followDAO.isFollowing(follower, followee));
    }

    @Test
    void follow_thenIsFollowingIsTrue_andCountsUpdate() {
        int follower = createUser("alice");
        int followee = createUser("bob");

        boolean followed = followDAO.follow(follower, followee);

        assertTrue(followed);
        assertTrue(followDAO.isFollowing(follower, followee));
        assertEquals(1, followDAO.countFollowers(followee));
        assertEquals(1, followDAO.countFollowing(follower));
    }

    @Test
    void follow_returnsFalse_whenAlreadyFollowing() {
        // FOLLOWER_ID+FOLLOWEE_IDにUNIQUE制約があるため、二重フォローは失敗する。
        int follower = createUser("alice");
        int followee = createUser("bob");
        followDAO.follow(follower, followee);

        boolean secondAttempt = followDAO.follow(follower, followee);

        assertFalse(secondAttempt);
        assertEquals(1, followDAO.countFollowers(followee));
    }

    @Test
    void unfollow_makesIsFollowingFalseAgain() {
        int follower = createUser("alice");
        int followee = createUser("bob");
        followDAO.follow(follower, followee);

        boolean unfollowed = followDAO.unfollow(follower, followee);

        assertTrue(unfollowed);
        assertFalse(followDAO.isFollowing(follower, followee));
        assertEquals(0, followDAO.countFollowers(followee));
    }

    @Test
    void toggleFollow_followsWhenNotFollowing_andUnfollowsWhenAlreadyFollowing() {
        int follower = createUser("alice");
        int followee = createUser("bob");

        assertTrue(followDAO.toggleFollow(follower, followee));
        assertTrue(followDAO.isFollowing(follower, followee));

        assertTrue(followDAO.toggleFollow(follower, followee));
        assertFalse(followDAO.isFollowing(follower, followee));
    }

    @Test
    void findFollowingUsers_returnsFolloweesOrderedByName() {
        int follower = createUser("alice");
        int zack = createUser("zack");
        int bob = createUser("bob");
        followDAO.follow(follower, zack);
        followDAO.follow(follower, bob);

        List<User> following = followDAO.findFollowingUsers(follower);

        assertEquals(2, following.size());
        assertEquals("bob", following.get(0).getName());
        assertEquals("zack", following.get(1).getName());
    }

    @Test
    void findFollowerUsers_returnsFollowersOrderedByName() {
        int target = createUser("alice");
        int zack = createUser("zack");
        int bob = createUser("bob");
        followDAO.follow(zack, target);
        followDAO.follow(bob, target);

        List<User> followers = followDAO.findFollowerUsers(target);

        assertEquals(2, followers.size());
        assertEquals("bob", followers.get(0).getName());
        assertEquals("zack", followers.get(1).getName());
    }
}
