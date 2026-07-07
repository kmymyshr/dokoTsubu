package model;

import dao.FollowDAO;

public class FollowUserLogic {
    /**
     * 指定したユーザーをフォロー/アンフォローする。
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
