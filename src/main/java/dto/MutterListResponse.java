package dto;

import java.util.List;

import model.MutterPage;

/** カーソルページネーションを含む一覧APIのJSONレスポンス。 */
public record MutterListResponse(
		List<MutterResponse> mutters,
		Integer nextCursor,
		boolean hasNext) {

	public static MutterListResponse from(List<MutterResponse> mutters, MutterPage page) {
		return new MutterListResponse(
				mutters,
				page.getNextCursor(),
				page.isHasNext());
	}
}
