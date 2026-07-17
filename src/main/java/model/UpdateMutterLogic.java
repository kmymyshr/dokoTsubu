package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

/**
 * 投稿更新の旧Logic互換クラス。
 *
 * <p>Phase5で投稿更新処理は {@link MutterService} へ移した。楽観ロックを含む更新条件は
 * Service/Repository側に集約し、このクラスは旧呼び出し口として残す。</p>
 */
public class UpdateMutterLogic {
    private final MutterService mutters;

    /** 既存コード向け。Spring管理Serviceは移行用ブリッジから取得する。 */
    public UpdateMutterLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    /** テストではMutterServiceを差し替えられるようにする。 */
    public UpdateMutterLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    /** 投稿を更新し、旧仕様に合わせて成功/失敗をbooleanで返す。 */
    public boolean execute(Mutter mutter) {
        return mutters.update(mutter);
    }
}
