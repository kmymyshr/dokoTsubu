package model;

/** A mutter together with the viewer-specific data required by the feed. */
public record MutterFeedItem(
        Mutter mutter,
        int likeCount,
        boolean likedByMe,
        boolean followedByMe) {
}
