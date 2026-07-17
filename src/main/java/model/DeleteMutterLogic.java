package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

/**
 * 投稿削除の旧Logic互換クラス。
 *
 * <p>Phase5で削除処理は {@link MutterService} へ移した。投稿者本人のみ削除できる条件は
 * Service/Repository側へ委譲し、このクラスは旧Servletからの呼び出し口として残す。</p>
 */
public class DeleteMutterLogic {
    private final MutterService mutters;

    /** 既存コード向け。Spring管理Serviceは移行用ブリッジから取得する。 */
    public DeleteMutterLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    /** テストではMutterServiceを差し替えられるようにする。 */
    public DeleteMutterLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    /** 投稿IDとユーザーIDを渡して、本人の投稿だけ削除する。 */
    public boolean execute(int mutterId, int userId) {
        return mutters.delete(mutterId, userId);
    }
}
