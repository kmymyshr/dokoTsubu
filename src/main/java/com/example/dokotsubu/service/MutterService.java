package com.example.dokotsubu.service;

import java.util.ArrayList;
import java.util.List;

import com.example.dokotsubu.persistence.SpringDataJdbcGateway;
import model.Mutter;
import model.MutterFeedItem;
import model.MutterFeedPage;
import org.springframework.stereotype.Service;

/**
 * 投稿（つぶやき）関連の業務処理をまとめるService。
 *
 * <p>Phase5で、一覧取得・検索・投稿作成・更新・削除の入口をこのServiceへ集約した。
 * Phase14では旧Servlet/旧Logic経由の投稿導線を削除し、React向けAPIから直接このServiceを
 * 呼ぶ構成に寄せている。ページングやカーソル制御などの投稿固有の判断をここに置く。</p>
 */
@Service
public class MutterService {
    /** 既存画面とAPIで共通利用する標準取得件数。 */
    public static final int DEFAULT_LIMIT = 20;

    private final SpringDataJdbcGateway gateway;

    public MutterService(SpringDataJdbcGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * React/API向けに、投稿本体に加えていいね数やフォロー状態を含むFeedを取得する。
     * limit + 1件取得して、次ページがあるかをService側で判定する。
     */
    public MutterFeedPage findFeedPage(String keyword, Integer cursor, int limit, int viewerId) {
        List<MutterFeedItem> fetched = gateway.findFeedPage(keyword, cursor, limit + 1, viewerId);
        boolean hasNext = fetched.size() > limit;
        List<MutterFeedItem> items = new ArrayList<>(
                fetched.subList(0, Math.min(limit, fetched.size())));
        Integer nextCursor = hasNext && !items.isEmpty()
                ? items.get(items.size() - 1).mutter().getId() : null;
        return new MutterFeedPage(items, nextCursor, hasNext);
    }

    /** 投稿詳細、更新、削除の事前確認で使うID検索。 */
    public Mutter findById(int mutterId) {
        return gateway.findMutterById(mutterId);
    }

    /** APIでは作成後のIDや表示用情報が必要なため、作成した投稿を返す。 */
    public Mutter createAndReturn(Mutter mutter) {
        return gateway.createMutter(mutter);
    }

    /** 旧PostMutterLogicのboolean戻り値に合わせた互換メソッド。 */
    public boolean create(Mutter mutter) {
        return createAndReturn(mutter) != null;
    }

    /** 楽観ロック用のversionを含む投稿更新をRepositoryへ委譲する。 */
    public boolean update(Mutter mutter) {
        return gateway.updateMutter(mutter);
    }

    /** 投稿者本人だけが削除できるよう、userIdもRepositoryへ渡す。 */
    public boolean delete(int mutterId, int userId) {
        return gateway.deleteMutter(mutterId, userId);
    }

}
