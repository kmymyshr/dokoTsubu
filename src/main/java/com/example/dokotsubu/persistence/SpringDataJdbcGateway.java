package com.example.dokotsubu.persistence;

import java.util.List;

import com.example.dokotsubu.persistence.entity.FollowEntity;
import com.example.dokotsubu.persistence.entity.MutterEntity;
import com.example.dokotsubu.persistence.entity.MutterLikeEntity;
import com.example.dokotsubu.persistence.entity.UserEntity;
import com.example.dokotsubu.persistence.repository.FollowRepository;
import com.example.dokotsubu.persistence.repository.MutterLikeRepository;
import com.example.dokotsubu.persistence.repository.MutterRepository;
import com.example.dokotsubu.persistence.repository.UserRepository;
import model.Mutter;
import model.MutterFeedItem;
import model.User;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpringDataJdbcGateway {
    private final UserRepository users;
    private final MutterRepository mutters;
    private final MutterLikeRepository likes;
    private final FollowRepository follows;
    private final MutterReadRepository reads;

    public SpringDataJdbcGateway(
            UserRepository users,
            MutterRepository mutters,
            MutterLikeRepository likes,
            FollowRepository follows,
            MutterReadRepository reads) {
        this.users = users;
        this.mutters = mutters;
        this.likes = likes;
        this.follows = follows;
        this.reads = reads;
    }

    public User findUserById(int id) {
        return users.findById(id).map(this::toUser).orElse(null);
    }

    public User findUserByName(String name) {
        return users.findByName(name).map(this::toUser).orElse(null);
    }

    public boolean createUser(User user) {
        if (users.findByName(user.getName()).isPresent()) {
            return false;
        }
        try {
            UserEntity saved = users.save(new UserEntity(
                    null, user.getName(), user.getPass(), normalizeBio(user.getBio())));
            return saved.id() != null;
        } catch (DbActionExecutionException e) {
            return false;
        }
    }

    @Transactional
    public boolean updateUserBio(int userId, String bio) {
        return users.updateBio(userId, normalizeBio(bio));
    }

    public List<Mutter> findLatest(int limit) {
        return reads.findLatest(limit);
    }

    public List<Mutter> findByCursor(int cursor, int limit) {
        return reads.findByCursor(cursor, limit);
    }

    public List<Mutter> findMutterPage(String keyword, Integer cursor, int limit) {
        return reads.findPage(keyword, cursor, limit);
    }

    public List<MutterFeedItem> findFeedPage(
            String keyword, Integer cursor, int limit, int viewerId) {
        return reads.findFeedPage(keyword, cursor, limit, viewerId);
    }

    public Mutter findMutterById(int id) {
        return reads.findById(id);
    }

    public Mutter createMutter(Mutter mutter) {
        try {
            MutterEntity saved = mutters.save(new MutterEntity(
                    null, mutter.getUserId(), mutter.getText(), null));
            return reads.findById(saved.id());
        } catch (DbActionExecutionException e) {
            return null;
        }
    }

    @Transactional
    public boolean updateMutter(Mutter mutter) {
        return mutters.updateOwned(
                mutter.getId(), mutter.getUserId(), mutter.getText(), mutter.getVersion());
    }

    @Transactional
    public boolean deleteMutter(int mutterId, int userId) {
        return mutters.deleteOwned(mutterId, userId);
    }

    public List<Mutter> searchMutters(String keyword) {
        return reads.search(keyword);
    }

    @Transactional
    public boolean toggleLike(int mutterId, int userId) {
        if (likes.existsByMutterIdAndUserId(mutterId, userId)) {
            return likes.remove(mutterId, userId);
        }
        return likes.save(new MutterLikeEntity(null, mutterId, userId)).id() != null;
    }

    public boolean addLike(int mutterId, int userId) {
        if (likes.existsByMutterIdAndUserId(mutterId, userId)) {
            return false;
        }
        try {
            return likes.save(new MutterLikeEntity(null, mutterId, userId)).id() != null;
        } catch (DbActionExecutionException e) {
            return false;
        }
    }

    @Transactional
    public boolean removeLike(int mutterId, int userId) {
        return likes.remove(mutterId, userId);
    }

    public boolean hasLiked(int mutterId, int userId) {
        return likes.existsByMutterIdAndUserId(mutterId, userId);
    }

    public int countLikes(int mutterId) {
        return Math.toIntExact(likes.countByMutterId(mutterId));
    }

    @Transactional
    public boolean toggleFollow(int followerId, int followeeId) {
        if (follows.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            return follows.remove(followerId, followeeId);
        }
        return follows.save(new FollowEntity(null, followerId, followeeId)).id() != null;
    }

    public boolean follow(int followerId, int followeeId) {
        if (follows.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            return false;
        }
        try {
            return follows.save(new FollowEntity(null, followerId, followeeId)).id() != null;
        } catch (DbActionExecutionException e) {
            return false;
        }
    }

    @Transactional
    public boolean unfollow(int followerId, int followeeId) {
        return follows.remove(followerId, followeeId);
    }

    public boolean isFollowing(int followerId, int followeeId) {
        return follows.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    public int countFollowers(int userId) {
        return Math.toIntExact(follows.countByFolloweeId(userId));
    }

    public int countFollowing(int userId) {
        return Math.toIntExact(follows.countByFollowerId(userId));
    }

    public List<User> findFollowingUsers(int userId) {
        return reads.findFollowingUsers(userId);
    }

    public List<User> findFollowerUsers(int userId) {
        return reads.findFollowerUsers(userId);
    }

    private User toUser(UserEntity entity) {
        return new User(entity.id(), entity.name(), entity.pass(), entity.bio());
    }

    private String normalizeBio(String bio) {
        return bio == null ? "" : bio;
    }
}
