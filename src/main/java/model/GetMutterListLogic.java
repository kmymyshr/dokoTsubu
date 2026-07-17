package model;
import java.util.List;

import dao.MutterDAO;

public class GetMutterListLogic {
	public static final int DEFAULT_LIMIT = 20;

	/**
	 * つぶやき一覧を取得する。
	 * 既定件数で返す。
	 */
	public List<Mutter> execute() {
		return execute(null, DEFAULT_LIMIT).getMutters();
	}

	/**
	 * REST API 向けに、検索条件とカーソルを使ってつぶやき一覧を取得する。
	 */
	public MutterPage execute(String keyword, Integer cursor, int limit) {
		List<Mutter> fetched = new MutterDAO().findPage(keyword, cursor, limit + 1);
		boolean hasNext = fetched.size() > limit;
		List<Mutter> mutters = new java.util.ArrayList<>(
				fetched.subList(0, Math.min(limit, fetched.size())));
		Integer nextCursor = hasNext && !mutters.isEmpty()
				? mutters.get(mutters.size() - 1).getId() : null;
		return new MutterPage(mutters, nextCursor, hasNext);
	}

	/** Returns an enriched page without issuing per-mutter queries. */
	public MutterFeedPage executeFeed(String keyword, Integer cursor, int limit, int viewerId) {
		List<MutterFeedItem> fetched = new MutterDAO()
				.findFeedPage(keyword, cursor, limit + 1, viewerId);
		boolean hasNext = fetched.size() > limit;
		List<MutterFeedItem> items = new java.util.ArrayList<>(
				fetched.subList(0, Math.min(limit, fetched.size())));
		Integer nextCursor = hasNext && !items.isEmpty()
				? items.get(items.size() - 1).mutter().getId() : null;
		return new MutterFeedPage(items, nextCursor, hasNext);
	}

	/**
	 * 通常の一覧表示向けに、カーソル付きでつぶやき一覧を取得する。
	 */
	public MutterPage execute(Integer cursor, int limit) {
		MutterDAO dao = new MutterDAO();

		// 1件多く取得し、次ページが存在するかを判定する。
		List<Mutter> fetched = cursor == null
				? dao.findLatest(limit + 1)
				: dao.findByCursor(cursor, limit + 1);
		if (fetched == null) {
			fetched = new java.util.ArrayList<>();
		}

		boolean hasNext = fetched.size() > limit;
		List<Mutter> mutters = new java.util.ArrayList<>(
				fetched.subList(0, Math.min(limit, fetched.size())));
		Integer nextCursor = hasNext && !mutters.isEmpty()
				? mutters.get(mutters.size() - 1).getId()
				: null;

		return new MutterPage(mutters, nextCursor, hasNext);
	}
}
