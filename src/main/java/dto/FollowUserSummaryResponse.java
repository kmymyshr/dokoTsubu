package dto;

import model.User;

/**
 * フォロー一覧APIで返すユーザー1件分のJSON。
 *
 * <p>画面表示に必要な最小限のユーザー情報と、ログインユーザーから見たフォロー状態をまとめる。
 * 将来フォロー一覧をReact化するとき、JSPのEL式に依存せずこのDTOだけで描画できるようにする。</p>
 */
public record FollowUserSummaryResponse(
        int id,
        String name,
        boolean followedByMe,
        boolean me) {

    /** ドメインのUserから、画面表示用の安全なレスポンス形へ変換する。 */
    public static FollowUserSummaryResponse from(User user, boolean followedByMe, int loginUserId) {
        return new FollowUserSummaryResponse(
                user.getId(),
                user.getName(),
                followedByMe,
                user.getId() == loginUserId);
    }
}
