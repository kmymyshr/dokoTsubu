package dto;

import java.time.LocalDateTime;

import model.Mutter;

/** つぶやき1件分のJSONレスポンス。 */
public record MutterResponse(
		int id,
		int userId,
		String userName,
		String text,
		int version,
		LocalDateTime createdAt) {

	public static MutterResponse from(Mutter mutter) {
		return new MutterResponse(
				mutter.getId(),
				mutter.getUserId(),
				mutter.getUserName(),
				mutter.getText(),
				mutter.getVersion(),
				mutter.getCreatedAt());
	}
}
