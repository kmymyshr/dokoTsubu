package model;

import java.util.List;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

/**
 * 投稿検索の旧Logic互換クラス。
 *
 * <p>Phase5で検索処理は {@link MutterService} へ移した。既存検索画面の呼び出し形を残し、
 * 実際の検索はService/Repositoryへ委譲する。</p>
 */
public class SearchMutterLogic {
    private final MutterService mutters;

    /** 既存コード向け。Spring管理Serviceは移行用ブリッジから取得する。 */
    public SearchMutterLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    /** テストではMutterServiceを差し替えられるようにする。 */
    public SearchMutterLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    /** キーワードに一致する投稿を検索する。 */
    public List<Mutter> execute(String keyword) {
        return mutters.search(keyword);
    }
}
