package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.SocialService;

/**
 * いいね機能の旧Logic互換クラス。
 *
 * <p>Phase5で実処理は {@link SocialService} へ移した。既存Servletからの呼び出し形を保つため、
 * 当面はServiceへの委譲層として残している。</p>
 */
public class LikeMutterLogic {
    private final SocialService social;

    /** 既存コード向け。Spring管理Serviceは移行用ブリッジから取得する。 */
    public LikeMutterLogic() {
        this(ApplicationServiceBridge.social());
    }

    /** テストではSocialServiceを差し替えられるようにする。 */
    public LikeMutterLogic(SocialService social) {
        this.social = social;
    }

    /** いいね状態を切り替え、切り替え後の状態を返す。 */
    public boolean execute(int mutterId, int userId) {
        return social.toggleLike(mutterId, userId);
    }

    /** 投稿表示用のいいね数を取得する。 */
    public int countLikes(int mutterId) {
        return social.countLikes(mutterId);
    }

    /** ログインユーザーが対象投稿をいいね済みか確認する。 */
    public boolean hasLiked(int mutterId, int userId) {
        return social.hasLiked(mutterId, userId);
    }
}
