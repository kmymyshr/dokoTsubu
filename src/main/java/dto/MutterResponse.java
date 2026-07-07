package dto;

import java.time.LocalDateTime;

import model.Mutter;

/** つぶやき1件分のJSONレスポンス（いいね情報を含む）。 */
public record MutterResponse(
		int id,
		int userId,
		String userName,
		String text,
		int version,
		LocalDateTime createdAt,
		int likeCount,
		boolean likedByMe) {

	public static MutterResponse from(Mutter mutter, int likeCount, boolean likedByMe) {
		return new MutterResponse(
				mutter.getId(),
				mutter.getUserId(),
				mutter.getUserName(),
				mutter.getText(),
				mutter.getVersion(),
				mutter.getCreatedAt(),
				likeCount,
				likedByMe);
	}
}
