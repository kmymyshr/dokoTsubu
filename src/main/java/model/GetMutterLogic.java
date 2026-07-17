package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

/**
 * 投稿1件取得の旧Logic互換クラス。
 *
 * <p>Phase5で投稿取得処理は {@link MutterService} へ移した。既存の呼び出し口を残しつつ、
 * DBアクセスの詳細はService/Repository側へ隠す。</p>
 */
public class GetMutterLogic {
    private final MutterService mutters;

    /** 既存コード向け。Spring管理Serviceは移行用ブリッジから取得する。 */
    public GetMutterLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    /** テストではMutterServiceを差し替えられるようにする。 */
    public GetMutterLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    /** 投稿IDから対象投稿を取得する。 */
    public Mutter execute(int mutterId) {
        return mutters.findById(mutterId);
    }
}
