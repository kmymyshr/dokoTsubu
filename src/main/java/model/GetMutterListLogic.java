package model;

import java.util.List;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

/**
 * 投稿一覧取得の旧Logic互換クラス。
 *
 * <p>Phase5で実際の業務処理は {@link MutterService} へ移したが、既存Servlet/JSPやテストが
 * `new GetMutterListLogic()` を前提としているため、当面はServiceへの薄い委譲層として残している。</p>
 */
public class GetMutterListLogic {
    public static final int DEFAULT_LIMIT = MutterService.DEFAULT_LIMIT;

    private final MutterService mutters;

    /** 既存コード向け。Spring管理Serviceは移行用ブリッジから取得する。 */
    public GetMutterListLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    /** テストではServiceを差し替えられるようにコンストラクタ注入も用意する。 */
    public GetMutterListLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    /** 旧メイン画面の初期一覧取得。 */
    public List<Mutter> execute() {
        return mutters.findAll();
    }

    /** キーワード検索とカーソルページングを含む一覧取得。 */
    public MutterPage execute(String keyword, Integer cursor, int limit) {
        return mutters.findPage(keyword, cursor, limit);
    }

    /** React/API向けに、いいね数などの表示情報を含むFeed一覧を取得する。 */
    public MutterFeedPage executeFeed(String keyword, Integer cursor, int limit, int viewerId) {
        return mutters.findFeedPage(keyword, cursor, limit, viewerId);
    }

    /** タイムラインの追加読み込み用。 */
    public MutterPage execute(Integer cursor, int limit) {
        return mutters.findTimelinePage(cursor, limit);
    }
}
