package model;

import java.util.List;

import dao.FollowDAO;

public class FollowUserLogic {
    /**
     * フォロー/アンフォローの処理をまとめて担当するクラスです。
     * サーブレット側ではこのクラスを呼ぶだけで処理を分かりやすくできます。
     */
    public boolean execute(int followerId, int followeeId) {
        FollowDAO dao = new FollowDAO();
        dao.toggleFollow(followerId, followeeId);
        return dao.isFollowing(followerId, followeeId);
    }

    public boolean isFollowing(int followerId, int followeeId) {
        return new FollowDAO().isFollowing(followerId, followeeId);
    }

    public int countFollowers(int userId) {
        return new FollowDAO().countFollowers(userId);
    }

    public int countFollowing(int userId) {
        return new FollowDAO().countFollowing(userId);
    }

    public List<User> findFollowingUsers(int userId) {
        return new FollowDAO().findFollowingUsers(userId);
    }
}
