package dao;

import java.util.List;

import com.example.dokotsubu.persistence.DataAccessBridge;
import model.User;
import org.springframework.dao.DataAccessException;

/**
 * フォロー操作をSpring Data JDBCへ委譲する互換アダプター。
 */
public class FollowDAO {

    public boolean toggleFollow(int followerId, int followeeId) {
        try {
            return DataAccessBridge.get().toggleFollow(followerId, followeeId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean follow(int followerId, int followeeId) {
        try {
            return DataAccessBridge.get().follow(followerId, followeeId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unfollow(int followerId, int followeeId) {
        try {
            return DataAccessBridge.get().unfollow(followerId, followeeId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isFollowing(int followerId, int followeeId) {
        try {
            return DataAccessBridge.get().isFollowing(followerId, followeeId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countFollowers(int userId) {
        try {
            return DataAccessBridge.get().countFollowers(userId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int countFollowing(int userId) {
        try {
            return DataAccessBridge.get().countFollowing(userId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<User> findFollowingUsers(int userId) {
        try {
            return DataAccessBridge.get().findFollowingUsers(userId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public List<User> findFollowerUsers(int userId) {
        try {
            return DataAccessBridge.get().findFollowerUsers(userId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
}
