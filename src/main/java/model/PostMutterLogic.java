package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

/**
 * 投稿作成の旧Logic互換クラス。
 *
 * <p>Phase5で投稿作成処理は {@link MutterService} へ移した。既存Servlet/JSPからの
 * `execute` 呼び出しを維持しつつ、実処理はServiceへ委譲する。</p>
 */
public class PostMutterLogic {
    private final MutterService mutters;

    /** 既存コード向け。Spring管理Serviceは移行用ブリッジから取得する。 */
    public PostMutterLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    /** テストではMutterServiceを差し替えられるようにする。 */
    public PostMutterLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    /** 投稿を作成し、旧仕様に合わせて成功/失敗をbooleanで返す。 */
    public boolean execute(Mutter mutter) {
        return mutters.create(mutter);
    }
}
