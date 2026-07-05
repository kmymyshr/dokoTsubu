package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.User;
import util.DBUtil;

public class UserDAO {

	// ユーザ情報がUSERSテーブルに存在するかチェックして、ID、NAME、PASSを入れたUserオブジェクトを返す
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

	// ユーザを新規登録する
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