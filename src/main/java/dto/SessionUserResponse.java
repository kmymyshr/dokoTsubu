package dto;

import model.User;

/** ログイン中ユーザーをパスワードなしで返すJSON。 */
public record SessionUserResponse(int id, String name) {
    public static SessionUserResponse from(User user) {
        return new SessionUserResponse(user.getId(), user.getName());
    }
}
