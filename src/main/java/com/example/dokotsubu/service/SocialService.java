package com.example.dokotsubu.service;

import java.util.List;

import com.example.dokotsubu.persistence.SpringDataJdbcGateway;
import model.User;
import org.springframework.stereotype.Service;

@Service
public class SocialService {
    private final SpringDataJdbcGateway gateway;

    public SocialService(SpringDataJdbcGateway gateway) {
        this.gateway = gateway;
    }

    public boolean toggleLike(int mutterId, int userId) {
        gateway.toggleLike(mutterId, userId);
        return gateway.hasLiked(mutterId, userId);
    }

    public boolean addLike(int mutterId, int userId) {
        return gateway.addLike(mutterId, userId);
    }

    public boolean removeLike(int mutterId, int userId) {
        return gateway.removeLike(mutterId, userId);
    }

    public boolean hasLiked(int mutterId, int userId) {
        return gateway.hasLiked(mutterId, userId);
    }

    public int countLikes(int mutterId) {
        return gateway.countLikes(mutterId);
    }

    public boolean toggleFollow(int followerId, int followeeId) {
        gateway.toggleFollow(followerId, followeeId);
        return gateway.isFollowing(followerId, followeeId);
    }

    public boolean follow(int followerId, int followeeId) {
        return gateway.follow(followerId, followeeId);
    }

    public boolean unfollow(int followerId, int followeeId) {
        return gateway.unfollow(followerId, followeeId);
    }

    public boolean isFollowing(int followerId, int followeeId) {
        return gateway.isFollowing(followerId, followeeId);
    }

    public int countFollowers(int userId) {
        return gateway.countFollowers(userId);
    }

    public int countFollowing(int userId) {
        return gateway.countFollowing(userId);
    }

    public List<User> findFollowingUsers(int userId) {
        return gateway.findFollowingUsers(userId);
    }

    public List<User> findFollowerUsers(int userId) {
        return gateway.findFollowerUsers(userId);
    }
}
