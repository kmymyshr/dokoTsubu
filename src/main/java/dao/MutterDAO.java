package dao;

import java.util.List;

import com.example.dokotsubu.persistence.DataAccessBridge;
import model.Mutter;
import model.MutterFeedItem;
import org.springframework.dao.DataAccessException;

/**
 * 既存Servlet/LogicのAPIを保ったままSpring Data JDBCへ委譲する互換アダプター。
 */
public class MutterDAO {

    public List<Mutter> findLatest(int limit) {
        try {
            return DataAccessBridge.get().findLatest(limit);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Mutter> findByCursor(int cursor, int limit) {
        try {
            return DataAccessBridge.get().findByCursor(cursor, limit);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Mutter> findPage(String keyword, Integer cursor, int limit) {
        try {
            return DataAccessBridge.get().findMutterPage(keyword, cursor, limit);
        } catch (DataAccessException e) {
            throw new RuntimeException("つぶやき一覧の取得に失敗しました。", e);
        }
    }

    public List<MutterFeedItem> findFeedPage(
            String keyword, Integer cursor, int limit, int viewerId) {
        try {
            return DataAccessBridge.get().findFeedPage(keyword, cursor, limit, viewerId);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to fetch the mutter feed", e);
        }
    }

    public Mutter findById(int mutterId) {
        try {
            return DataAccessBridge.get().findMutterById(mutterId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Mutter createAndReturn(Mutter mutter) {
        try {
            return DataAccessBridge.get().createMutter(mutter);
        } catch (DataAccessException e) {
            throw new RuntimeException("つぶやきの作成に失敗しました。", e);
        }
    }

    public boolean create(Mutter mutter) {
        try {
            return DataAccessBridge.get().createMutter(mutter) != null;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Mutter mutter) {
        try {
            return DataAccessBridge.get().updateMutter(mutter);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int mutterId, int userId) {
        try {
            return DataAccessBridge.get().deleteMutter(mutterId, userId);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Mutter> search(String keyword) {
        try {
            return DataAccessBridge.get().searchMutters(keyword);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
}
