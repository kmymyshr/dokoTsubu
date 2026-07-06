package model;

import java.util.List;

/** カーソルページネーション1ページ分の結果。 */
public class MutterPage {
	private final List<Mutter> mutters;
	private final Integer nextCursor;
	private final boolean hasNext;

	public MutterPage(List<Mutter> mutters, Integer nextCursor, boolean hasNext) {
		this.mutters = mutters;
		this.nextCursor = nextCursor;
		this.hasNext = hasNext;
	}

	public List<Mutter> getMutters() {
		return mutters;
	}

	public Integer getNextCursor() {
		return nextCursor;
	}

	public boolean isHasNext() {
		return hasNext;
	}
}
