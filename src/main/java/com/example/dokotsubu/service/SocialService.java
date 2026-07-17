package com.example.dokotsubu.service;

import java.util.List;

import com.example.dokotsubu.persistence.SpringDataJdbcGateway;
import model.User;
import org.springframework.stereotype.Service;

/**
 * いいね・フォローなど、ユーザー間の関係性を扱うService。
 *
 * <p>Phase5で、LikeDAO/FollowDAOへ直接寄っていた処理をこのServiceに集約した。
 * 投稿そのものとは別の関心事として分離することで、API・JSP・旧Logicのどこから呼ばれても
 * 同じルールで状態を更新できるようにしている。</p>
 */
@Service
public class SocialService {
    private final SpringDataJdbcGateway gateway;

    public SocialService(SpringDataJdbcGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * いいねのON/OFFを切り替え、切り替え後の状態を返す。
     * 既存APIが「現在いいね済みか」を返す仕様のため、更新後に再取得している。
     */
    public boolean toggleLike(int mutterId, int userId) {
        gateway.toggleLike(mutterId, userId);
        return gateway.hasLiked(mutterId, userId);
    }

    /** 明示的にいいねを追加する処理。互換DAO縮小中の呼び出し口として残している。 */
    public boolean addLike(int mutterId, int userId) {
        return gateway.addLike(mutterId, userId);
    }

    /** 明示的にいいねを解除する処理。互換DAO縮小中の呼び出し口として残している。 */
    public boolean removeLike(int mutterId, int userId) {
        return gateway.removeLike(mutterId, userId);
    }

    /** 投稿表示時に、ログインユーザーが対象投稿をいいね済みか確認する。 */
    public boolean hasLiked(int mutterId, int userId) {
        return gateway.hasLiked(mutterId, userId);
    }

    /** 投稿表示用のいいね数を取得する。 */
    public int countLikes(int mutterId) {
        return gateway.countLikes(mutterId);
    }

    /**
     * フォローのON/OFFを切り替え、切り替え後の状態を返す。
     * 画面側がボタン状態を更新しやすいように、booleanで現在状態を返している。
     */
    public boolean toggleFollow(int followerId, int followeeId) {
        gateway.toggleFollow(followerId, followeeId);
        return gateway.isFollowing(followerId, followeeId);
    }

    /** 明示的にフォローする処理。将来のAPI分割に備えてServiceに入口を残す。 */
    public boolean follow(int followerId, int followeeId) {
        return gateway.follow(followerId, followeeId);
    }

    /** 明示的にフォロー解除する処理。将来のAPI分割に備えてServiceに入口を残す。 */
    public boolean unfollow(int followerId, int followeeId) {
        return gateway.unfollow(followerId, followeeId);
    }

    /** プロフィール表示やFeed表示でフォロー状態を確認する。 */
    public boolean isFollowing(int followerId, int followeeId) {
        return gateway.isFollowing(followerId, followeeId);
    }

    /** プロフィール表示用のフォロワー数を取得する。 */
    public int countFollowers(int userId) {
        return gateway.countFollowers(userId);
    }

    /** プロフィール表示用のフォロー中人数を取得する。 */
    public int countFollowing(int userId) {
        return gateway.countFollowing(userId);
    }

    /** フォロー中一覧画面で表示するユーザー一覧を取得する。 */
    public List<User> findFollowingUsers(int userId) {
        return gateway.findFollowingUsers(userId);
    }

    /** フォロワー一覧画面で表示するユーザー一覧を取得する。 */
    public List<User> findFollowerUsers(int userId) {
        return gateway.findFollowerUsers(userId);
    }
}
