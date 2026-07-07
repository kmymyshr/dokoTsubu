package model;

import dao.FollowDAO;

public class FollowUserLogic {
    /**
     * フォロー/アンフォローの処理をまとめて担当するクラスです。
     * サーブレット側ではこのクラスを呼ぶだけで処理を分かりやすくできます。
     */
    public boolean execute(int followerId, int followeeId) {
        return new FollowDAO().toggleFollow(followerId, followeeId);
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
}
