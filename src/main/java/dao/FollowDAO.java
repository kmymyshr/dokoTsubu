package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.DBUtil;

public class FollowDAO {

    /**
     * フォロー状態を切り替える。
     * すでにフォロー済みなら解除し、まだなら追加する。
     */
    public boolean toggleFollow(int followerId, int followeeId) {
        ensureSchema();
        try (Connection conn = DBUtil.getConnection()) {
            if (isFollowing(conn, followerId, followeeId)) {
                return unfollow(conn, followerId, followeeId);
            }
            return follow(conn, followerId, followeeId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean follow(int followerId, int followeeId) {
        ensureSchema();
        try (Connection conn = DBUtil.getConnection()) {
            return follow(conn, followerId, followeeId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unfollow(int followerId, int followeeId) {
        ensureSchema();
        try (Connection conn = DBUtil.getConnection()) {
            return unfollow(conn, followerId, followeeId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isFollowing(int followerId, int followeeId) {
        ensureSchema();
        try (Connection conn = DBUtil.getConnection()) {
            return isFollowing(conn, followerId, followeeId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countFollowers(int userId) {
        ensureSchema();
        String sql = "SELECT COUNT(*) FROM FOLLOWS WHERE FOLLOWEE_ID = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, userId);
            try (ResultSet rs = pStmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int countFollowing(int userId) {
        ensureSchema();
        String sql = "SELECT COUNT(*) FROM FOLLOWS WHERE FOLLOWER_ID = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, userId);
            try (ResultSet rs = pStmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean follow(Connection conn, int followerId, int followeeId) throws SQLException {
        String sql = "INSERT INTO FOLLOWS (FOLLOWER_ID, FOLLOWEE_ID) VALUES (?, ?)";
        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, followerId);
            pStmt.setInt(2, followeeId);
            return pStmt.executeUpdate() == 1;
        }
    }

    private boolean unfollow(Connection conn, int followerId, int followeeId) throws SQLException {
        String sql = "DELETE FROM FOLLOWS WHERE FOLLOWER_ID = ? AND FOLLOWEE_ID = ?";
        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, followerId);
            pStmt.setInt(2, followeeId);
            return pStmt.executeUpdate() == 1;
        }
    }

    private boolean isFollowing(Connection conn, int followerId, int followeeId) throws SQLException {
        String sql = "SELECT 1 FROM FOLLOWS WHERE FOLLOWER_ID = ? AND FOLLOWEE_ID = ?";
        try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, followerId);
            pStmt.setInt(2, followeeId);
            try (ResultSet rs = pStmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void ensureSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS FOLLOWS "
                + "(ID INT AUTO_INCREMENT PRIMARY KEY, "
                + "FOLLOWER_ID INT NOT NULL, "
                + "FOLLOWEE_ID INT NOT NULL, "
                + "CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "UNIQUE(FOLLOWER_ID, FOLLOWEE_ID))";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
