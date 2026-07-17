package dao;

import com.example.dokotsubu.persistence.DataAccessBridge;
import org.springframework.dao.DataAccessException;

/**
 * いいね操作をSpring Data JDBCへ委譲する互換アダプター。
 */
public class LikeDAO {

    public boolean toggleLike(int mutterId, int userId) {
        try {
            return DataAccessBridge.get().toggleLike(mutterId, userId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addLike(int mutterId, int userId) {
        try {
            return DataAccessBridge.get().addLike(mutterId, userId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeLike(int mutterId, int userId) {
        try {
            return DataAccessBridge.get().removeLike(mutterId, userId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasLiked(int mutterId, int userId) {
        try {
            return DataAccessBridge.get().hasLiked(mutterId, userId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countLikes(int mutterId) {
        try {
            return DataAccessBridge.get().countLikes(mutterId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
