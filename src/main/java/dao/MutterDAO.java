package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Mutter;

public class MutterDAO {
//データベース接続に使用する情報
	private final String JDBC_URL = "jdbc:h2:tcp://localhost/~/dokoTsubu";
	private final String DB_USER = "sa";
	private final String DB_PASS = "";
	
	public List<Mutter> findALL(){
		List<Mutter> mutterList = new ArrayList<>();
	//JDBCドライバを読み込む
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(
					"JDBCドライバのロードに失敗しました。");
		}
		//データベース接続
		try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
			
					
			//Mutters.USER_ID=USERS.IDを条件にJOIN,USERS.NAMEを取得するように変更
			String sql = "SELECT MUTTERS.ID, USERS.NAME, MUTTERS.TEXT FROM MUTTERS JOIN USERS ON MUTTERS.USER_ID = USERS.ID ORDER BY MUTTERS.ID DESC";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			
						
			//SELECTを実行し、結果表を取得
			ResultSet rs = pStmt.executeQuery();
						
			//結果表に格納されたレコードの内容を
			//Mutterインスタンスに設定し、リストに追加
						
			while (rs.next()) {
				int userId = rs.getInt("USER_ID");
				String userName = rs.getString("NAME");
				String text = rs.getString("TEXT");
				Mutter mutter = new Mutter(userId, userName, text);
				mutterList.add(mutter);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	return mutterList;	
}
	
	
		public boolean create(Mutter mutter) {
			//JDBCドライバを読み込む
			try {
				Class.forName("org.h2.Driver");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(
						"JDBCドライバのロードに失敗しました。");
			}
			//データベース接続
			try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
				//INSERT文を準備
				String sql = "INSERT INTO MUTTERS(USER_ID, TEXT) VALUES(?, ?)";
				PreparedStatement pStmt = conn.prepareStatement(sql);
				//INSERT文中の「?」に使用する値を設定しSQLを完成
				
				pStmt.setInt(1, mutter.getUserId());
				pStmt.setString(2, mutter.getText());
				//INSERTを実行
				int result = pStmt.executeUpdate();
				if (result != 1) {
					return false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

	
		public List<Mutter> search(String keyword) {
			List<Mutter> mutterList = new ArrayList<>();
			
		//JDBCドライバを読み込む
			try {
				Class.forName("org.h2.Driver");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(
						"JDBCドライバのロードに失敗しました。");
			}
			//データベース接続
			try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
				
				
				String sql = 
						"SELECT MUTTERS.ID, USERS.NAME, MUTTERS.TEXT FROM MUTTERS JOIN USERS ON MUTTERS.USER_ID = USERS.ID "
						+ "WHERE MUTTERS.TEXT LIKE ？"
						+ "ORDER BY MUTTERS.ID DESC";
						
				
				PreparedStatement pStmt = conn.prepareStatement(sql);
				
				pStmt.setString(1, "%" + keyword + "%");
				
				ResultSet rs = pStmt.executeQuery();
				
				while (rs.next()) {
					int userId = rs.getInt("USER_ID");
					String userName = rs.getString("NAME");
					String text = rs.getString("TEXT");
					Mutter mutter = new Mutter(userId, userName, text);
					mutterList.add(mutter);
				}
			
			
			
			} catch (SQLException e) {
				e.printStackTrace();
			
			}
			System.out.println("keyword = " + keyword);
			System.out.println("mutterList = " + mutterList);
			
		return mutterList;	
	
		}				
}
