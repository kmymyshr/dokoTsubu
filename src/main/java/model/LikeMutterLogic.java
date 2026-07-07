package model;

import dao.LikeDAO;

public class LikeMutterLogic {
    /**
     * サーブレットから呼ばれる、いいねの切り替え処理の入口です。
     * ここではDAOに処理を委譲し、コントロール層を簡潔に保ちます。
     */
    public boolean execute(int mutterId, int userId) {
        return new LikeDAO().toggleLike(mutterId, userId);
    }

    public int countLikes(int mutterId) {
        return new LikeDAO().countLikes(mutterId);
    }

    public boolean hasLiked(int mutterId, int userId) {
        return new LikeDAO().hasLiked(mutterId, userId);
    }
}
