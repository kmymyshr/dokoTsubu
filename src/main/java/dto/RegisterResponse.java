package dto;

/**
 * ユーザー登録APIの成功レスポンス。
 *
 * <p>登録直後は自動ログインせず、既存導線と同じくログイン画面へ戻るため、
 * React側が完了メッセージと遷移先を表示できる最小限の情報を返す。</p>
 */
public record RegisterResponse(String name, String message, String loginUrl) {
}
