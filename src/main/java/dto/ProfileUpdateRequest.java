package dto;

/**
 * プロフィール更新APIで受け取るJSON。
 *
 * <p>Phase11で自己紹介編集をReactから行うため、旧JSPフォームのPOSTではなく
 * JSONとして対象ユーザーIDと自己紹介本文を送る。</p>
 */
public record ProfileUpdateRequest(int userId, String bio) {
}
