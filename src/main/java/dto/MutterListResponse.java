package dto;

import java.util.List;

import model.MutterPage;

/** カーソルページネーションを含む一覧APIのJSONレスポンス。 */
public record MutterListResponse(
		List<MutterResponse> mutters,
		Integer nextCursor,
		boolean hasNext) {

	public static MutterListResponse from(MutterPage page) {
		List<MutterResponse> mutters = page.getMutters().stream()
				.map(MutterResponse::from)
				.toList();

		return new MutterListResponse(
				mutters,
				page.getNextCursor(),
				page.isHasNext());
	}
}
