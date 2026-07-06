package model;
import java.util.List;

import dao.MutterDAO;

public class GetMutterListLogic {
	public static final int DEFAULT_LIMIT = 20;

	public List<Mutter> execute() {
		return execute(null, DEFAULT_LIMIT).getMutters();
	}

		/** REST API向けに検索とカーソルページネーションを組み合わせる。 */
	public MutterPage execute(String keyword, Integer cursor, int limit) {
		List<Mutter> fetched = new MutterDAO().findPage(keyword, cursor, limit + 1);
		boolean hasNext = fetched.size() > limit;
		List<Mutter> mutters = new java.util.ArrayList<>(
				fetched.subList(0, Math.min(limit, fetched.size())));
		Integer nextCursor = hasNext && !mutters.isEmpty()
				? mutters.get(mutters.size() - 1).getId() : null;
		return new MutterPage(mutters, nextCursor, hasNext);
	}

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
