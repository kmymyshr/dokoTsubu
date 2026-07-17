package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.DBUtil;

public class LikeDAO {

    /**
     * つぶやきに対するいいねを切り替える。
     * すでにいいね済みなら削除し、まだなら追加する。
     */
    public boolean toggleLike(int mutterId, int userId) {
        try (Connection conn = DBUtil.getConnection()) {
            if (hasLiked(conn, mutterId, userId)) {
                return removeLike(conn, mutterId, userId);
            }
            return addLike(conn, mutterId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addLike(int mutterId, int userId) {
        try (Connection conn = DBUtil.getConnection()) {
            return addLike(conn, mutterId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeLike(int mutterId, int userId) {
        try (Connection conn = DBUtil.getConnection()) {
            return removeLike(conn, mutterId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasLiked(int mutterId, int userId) {
        try (Connection conn = DBUtil.getConnection()) {
            return hasLiked(conn, mutterId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countLikes(int mutterId) {
        String sql = "SELECT COUNT(*) FROM MUTTER_LIKES WHERE MUTTER_ID = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, mutterId);
            try (ResultSet rs = pStmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean addLike(Connection conn, int mutterId, int userId) throws SQLException {
        String sql = "INSERT INTO MUTTER_LIKES (MUTTER_ID, USER_ID) VALUES (?, ?)";
        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, mutterId);
            pStmt.setInt(2, userId);
            return pStmt.executeUpdate() == 1;
        }
    }

    private boolean removeLike(Connection conn, int mutterId, int userId) throws SQLException {
        String sql = "DELETE FROM MUTTER_LIKES WHERE MUTTER_ID = ? AND USER_ID = ?";
        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, mutterId);
            pStmt.setInt(2, userId);
            return pStmt.executeUpdate() == 1;
        }
    }

    private boolean hasLiked(Connection conn, int mutterId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM MUTTER_LIKES WHERE MUTTER_ID = ? AND USER_ID = ?";
        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, mutterId);
            pStmt.setInt(2, userId);
            try (ResultSet rs = pStmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
