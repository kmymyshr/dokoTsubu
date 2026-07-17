package dto;

/**
 * ユーザー登録APIで受け取るJSON。
 *
 * <p>Phase10で登録画面をReact化するため、JSPフォームのname/passパラメータではなく
 * JSONとしてユーザー名とパスワードを受け取る。</p>
 */
public record RegisterRequest(String name, String pass) {
}
