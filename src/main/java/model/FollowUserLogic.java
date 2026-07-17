package model;

import java.util.List;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.SocialService;

public class FollowUserLogic {
    private final SocialService social;

    public FollowUserLogic() {
        this(ApplicationServiceBridge.social());
    }

    public FollowUserLogic(SocialService social) {
        this.social = social;
    }

    public boolean execute(int followerId, int followeeId) {
        return social.toggleFollow(followerId, followeeId);
    }

    public boolean isFollowing(int followerId, int followeeId) {
        return social.isFollowing(followerId, followeeId);
    }

    public int countFollowers(int userId) {
        return social.countFollowers(userId);
    }

    public int countFollowing(int userId) {
        return social.countFollowing(userId);
    }

    public List<User> findFollowingUsers(int userId) {
        return social.findFollowingUsers(userId);
    }

    public List<User> findFollowerUsers(int userId) {
        return social.findFollowerUsers(userId);
    }
}
