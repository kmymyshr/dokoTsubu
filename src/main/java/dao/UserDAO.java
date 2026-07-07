package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.User;
import util.DBUtil;

public class UserDAO {

	/**
	 * ユーザー名をもとに、ユーザー情報をデータベースから取得する。
	 * ログイン時に利用する。
	 */
	public User findByName(String name) {
		String sql = "SELECT ID, NAME, PASS FROM USERS WHERE NAME = ?";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql)) {

			pStmt.setString(1, name);

			ResultSet rs = pStmt.executeQuery();

			if (rs.next()) {
				int id = rs.getInt("ID");
				String userName = rs.getString("NAME");
				String pass = rs.getString("PASS");

				return new User(id, userName, pass);
			}
			return null;
		}

		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 新しいユーザーをデータベースに登録する。
	 */
	public boolean create(User user) {
		String sql = "INSERT INTO USERS (NAME, PASS) VALUES (?, ?)";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql)) {

			pStmt.setString(1, user.getName());
			pStmt.setString(2, user.getPass());

			int result = pStmt.executeUpdate();
			return result == 1;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}