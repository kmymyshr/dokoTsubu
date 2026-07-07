package model;

import dao.LikeDAO;

public class LikeMutterLogic {
    /**
     * つぶやきに対するいいねを切り替える。
     * すでにいいね済みなら取り消し、まだなら追加する。
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
