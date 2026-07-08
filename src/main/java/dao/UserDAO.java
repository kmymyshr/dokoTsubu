package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.User;
import util.DBUtil;

public class UserDAO {

	public User findById(int id) {
		String sql = "SELECT ID, NAME, PASS, BIO FROM USERS WHERE ID = ?";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql)) {

			pStmt.setInt(1, id);

			ResultSet rs = pStmt.executeQuery();

			if (rs.next()) {
				int userId = rs.getInt("ID");
				String userName = rs.getString("NAME");
				String pass = rs.getString("PASS");
				String bio = rs.getString("BIO");

				return new User(userId, userName, pass, bio);
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public User findByName(String name) {
		String sql = "SELECT ID, NAME, PASS, BIO FROM USERS WHERE NAME = ?";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql)) {

			pStmt.setString(1, name);

			ResultSet rs = pStmt.executeQuery();

			if (rs.next()) {
				int id = rs.getInt("ID");
				String userName = rs.getString("NAME");
				String pass = rs.getString("PASS");
				String bio = rs.getString("BIO");

				return new User(id, userName, pass, bio);
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean create(User user) {
		String sql = "INSERT INTO USERS (NAME, PASS, BIO) VALUES (?, ?, ?)";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql)) {

			pStmt.setString(1, user.getName());
			pStmt.setString(2, user.getPass());
			pStmt.setString(3, normalizeBio(user.getBio()));

			int result = pStmt.executeUpdate();
			return result == 1;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateBio(int userId, String bio) {
		String sql = "UPDATE USERS SET BIO = ? WHERE ID = ?";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql)) {

			pStmt.setString(1, normalizeBio(bio));
			pStmt.setInt(2, userId);

			return pStmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private String normalizeBio(String bio) {
		return bio == null ? "" : bio;
	}
}
