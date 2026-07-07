package dto;

import model.User;

/** ログイン中ユーザーをパスワードなしで返すJSON。 */
public record SessionUserResponse(int id, String name, String csrfToken) {
    public static SessionUserResponse from(User user, String csrfToken) {
        return new SessionUserResponse(user.getId(), user.getName(), csrfToken);
    }
}
