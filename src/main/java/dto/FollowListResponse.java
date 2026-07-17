package dto;

import java.util.List;

import model.User;

/**
 * フォロー中/フォロワー一覧APIのJSONレスポンス。
 *
 * <p>Phase8では既存JSPを残しながら、React画面が同じ一覧データをAPIから取得できる入口を用意する。
 * 一覧種別、対象ユーザー、件数、ユーザー一覧をまとめて返すことで、画面側の分岐を小さく保つ。</p>
 */
public record FollowListResponse(
        String type,
        UserSummary targetUser,
        int count,
        List<FollowUserSummaryResponse> users) {

    /** 対象プロフィールユーザーは、パスワードなどを含まない表示専用DTOへ変換する。 */
    public static FollowListResponse from(
            String type,
            User targetUser,
            int count,
            List<FollowUserSummaryResponse> users) {
        return new FollowListResponse(type, UserSummary.from(targetUser), count, users);
    }

    /** フォロー一覧の見出しやプロフィール導線に必要な対象ユーザー情報。 */
    public record UserSummary(int id, String name) {
        public static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getName());
        }
    }
}
