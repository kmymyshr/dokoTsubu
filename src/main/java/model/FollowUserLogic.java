package model;

import java.util.List;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.SocialService;

/**
 * フォロー機能の旧Logic互換クラス。
 *
 * <p>Phase5で実処理は {@link SocialService} へ移した。既存Servlet/JSPからの呼び出し形を
 * 変えすぎないため、このクラスはServiceへの委譲層として残している。</p>
 */
public class FollowUserLogic {
    private final SocialService social;

    /** 既存コード向け。Spring管理Serviceは移行用ブリッジから取得する。 */
    public FollowUserLogic() {
        this(ApplicationServiceBridge.social());
    }

    /** テストではSocialServiceを差し替えられるようにする。 */
    public FollowUserLogic(SocialService social) {
        this.social = social;
    }

    /** フォロー状態を切り替え、切り替え後の状態を返す。 */
    public boolean execute(int followerId, int followeeId) {
        return social.toggleFollow(followerId, followeeId);
    }

    /** 表示中ユーザーをログインユーザーがフォロー済みか確認する。 */
    public boolean isFollowing(int followerId, int followeeId) {
        return social.isFollowing(followerId, followeeId);
    }

    /** プロフィール表示用のフォロワー数を取得する。 */
    public int countFollowers(int userId) {
        return social.countFollowers(userId);
    }

    /** プロフィール表示用のフォロー中人数を取得する。 */
    public int countFollowing(int userId) {
        return social.countFollowing(userId);
    }

    /** フォロー中一覧画面のユーザー一覧を取得する。 */
    public List<User> findFollowingUsers(int userId) {
        return social.findFollowingUsers(userId);
    }

    /** フォロワー一覧画面のユーザー一覧を取得する。 */
    public List<User> findFollowerUsers(int userId) {
        return social.findFollowerUsers(userId);
    }
}
