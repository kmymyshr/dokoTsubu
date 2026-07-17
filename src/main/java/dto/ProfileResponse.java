package dto;

import model.User;

/**
 * プロフィール画面向けのJSONレスポンス。
 *
 * <p>Phase11でプロフィール画面をReact化するため、JSPに渡していたユーザー情報、
 * フォロー状態、フォロー数をAPIレスポンスとしてまとめる。パスワードは返さず、
 * Reactが画面表示に必要な最小限の情報だけを持つ。</p>
 */
public record ProfileResponse(
        UserSummary user,
        boolean ownProfile,
        boolean following,
        int followers,
        int followingCount) {

    /** model.Userから表示専用のプロフィール情報へ変換する。 */
    public static ProfileResponse from(
            User profileUser,
            boolean ownProfile,
            boolean following,
            int followers,
            int followingCount) {
        return new ProfileResponse(
                UserSummary.from(profileUser),
                ownProfile,
                following,
                followers,
                followingCount);
    }

    /** プロフィール表示で使うユーザー情報。 */
    public record UserSummary(int id, String name, String bio) {
        public static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getName(), user.getBio());
        }
    }
}
