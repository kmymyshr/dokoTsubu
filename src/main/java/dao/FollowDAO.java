package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.User;
import util.DBUtil;

public class FollowDAO {

    /**
     * フォロー状態を切り替える。
     * すでにフォロー済みなら解除し、まだなら追加する。
     */
    public boolean toggleFollow(int followerId, int followeeId) {
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
        try (Connection conn = DBUtil.getConnection()) {
            return follow(conn, followerId, followeeId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unfollow(int followerId, int followeeId) {
        try (Connection conn = DBUtil.getConnection()) {
            return unfollow(conn, followerId, followeeId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isFollowing(int followerId, int followeeId) {
        try (Connection conn = DBUtil.getConnection()) {
            return isFollowing(conn, followerId, followeeId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countFollowers(int userId) {
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

    public List<User> findFollowingUsers(int userId) {
        String sql = "SELECT U.ID, U.NAME, U.PASS, U.BIO "
                + "FROM FOLLOWS F "
                + "JOIN USERS U ON F.FOLLOWEE_ID = U.ID "
                + "WHERE F.FOLLOWER_ID = ? "
                + "ORDER BY U.NAME ASC";
        List<User> users = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, userId);
            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                            rs.getInt("ID"),
                            rs.getString("NAME"),
                            rs.getString("PASS"),
                            rs.getString("BIO")));
                }
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
            return users;
        }
    }

    public List<User> findFollowerUsers(int userId) {
        String sql = "SELECT U.ID, U.NAME, U.PASS, U.BIO "
                + "FROM FOLLOWS F "
                + "JOIN USERS U ON F.FOLLOWER_ID = U.ID "
                + "WHERE F.FOLLOWEE_ID = ? "
                + "ORDER BY U.NAME ASC";
        List<User> users = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, userId);
            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                            rs.getInt("ID"),
                            rs.getString("NAME"),
                            rs.getString("PASS"),
                            rs.getString("BIO")));
                }
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
            return users;
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
}
