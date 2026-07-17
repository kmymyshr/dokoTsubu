package model;

import java.util.List;

/** A cursor-paginated page whose rows are ready to render in the feed. */
public record MutterFeedPage(
        List<MutterFeedItem> items,
        Integer nextCursor,
        boolean hasNext) {
}
