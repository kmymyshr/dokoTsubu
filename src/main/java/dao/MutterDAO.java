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

	/** 最新のつぶやきを指定件数だけ取得する。 */
	public List<Mutter> findLatest(int limit) {
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
			String sql = "SELECT m.ID, m.USER_ID, u.NAME, m.TEXT, m.VERSION, m.CREATED_AT "
					+ "FROM MUTTERS m JOIN USERS u ON m.USER_ID = u.ID "
					+ "ORDER BY m.ID DESC LIMIT ?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setInt(1, limit);

			//SELECTを実行し、結果表を取得
			ResultSet rs = pStmt.executeQuery();

			//結果表に格納されたレコードの内容を
			//Mutterインスタンスに設定し、リストに追加

			while (rs.next()) {
				int id = rs.getInt("ID");
				int userId = rs.getInt("USER_ID");
				String userName = rs.getString("NAME");
				String text = rs.getString("TEXT");
				int version = rs.getInt("VERSION");
				java.time.LocalDateTime createdAt = rs.getTimestamp("CREATED_AT").toLocalDateTime();
				Mutter mutter = new Mutter(id, userId, userName, text, version, createdAt);
				mutterList.add(mutter);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return mutterList;
	}

	/** 指定したIDより古いつぶやきを指定件数だけ取得する。 */
	public List<Mutter> findByCursor(int cursor, int limit) {
		List<Mutter> mutterList = new ArrayList<>();
		loadDriver();

		String sql = "SELECT m.ID, m.USER_ID, u.NAME, m.TEXT, m.VERSION, m.CREATED_AT "
				+ "FROM MUTTERS m JOIN USERS u ON m.USER_ID = u.ID "
				+ "WHERE m.ID < ? ORDER BY m.ID DESC LIMIT ?";
		try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
				PreparedStatement pStmt = conn.prepareStatement(sql)) {
			pStmt.setInt(1, cursor);
			pStmt.setInt(2, limit);

			ResultSet rs = pStmt.executeQuery();
			while (rs.next()) {
				mutterList.add(toMutter(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return mutterList;
	}

		/** REST API用に、検索条件とカーソルを同時に適用して取得する。 */
	public List<Mutter> findPage(String keyword, Integer cursor, int limit) {
		List<Mutter> mutterList = new ArrayList<>();
		loadDriver();
		StringBuilder sql = new StringBuilder(
				"SELECT m.ID, m.USER_ID, u.NAME, m.TEXT, m.VERSION, m.CREATED_AT "
				+ "FROM MUTTERS m JOIN USERS u ON m.USER_ID = u.ID WHERE 1 = 1 ");
		if (keyword != null) sql.append("AND m.TEXT LIKE ? ");
		if (cursor != null) sql.append("AND m.ID < ? ");
		sql.append("ORDER BY m.ID DESC LIMIT ?");
		try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
				PreparedStatement pStmt = conn.prepareStatement(sql.toString())) {
			int index = 1;
			if (keyword != null) pStmt.setString(index++, "%" + keyword + "%");
			if (cursor != null) pStmt.setInt(index++, cursor);
			pStmt.setInt(index, limit);
			try (ResultSet rs = pStmt.executeQuery()) {
				while (rs.next()) mutterList.add(toMutter(rs));
			}
		} catch (SQLException e) {
			throw new RuntimeException("つぶやき一覧の取得に失敗しました。", e);
		}
		return mutterList;
	}

	/** 編集画面で使うつぶやきをIDで1件取得する。 */
	public Mutter findById(int mutterId) {
		loadDriver();

		String sql = "SELECT m.ID, m.USER_ID, u.NAME, m.TEXT, m.VERSION, m.CREATED_AT "
				+ "FROM MUTTERS m JOIN USERS u ON m.USER_ID = u.ID WHERE m.ID = ?";
		try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
				PreparedStatement pStmt = conn.prepareStatement(sql)) {
			pStmt.setInt(1, mutterId);
			ResultSet rs = pStmt.executeQuery();
			return rs.next() ? toMutter(rs) : null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

		/** 作成したつぶやきを採番済みIDを含めて返す。 */
	public Mutter createAndReturn(Mutter mutter) {
		loadDriver();
		String sql = "INSERT INTO MUTTERS(USER_ID, TEXT) VALUES(?, ?)";
		int generatedId;
		try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
				PreparedStatement pStmt = conn.prepareStatement(
						sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
			pStmt.setInt(1, mutter.getUserId());
			pStmt.setString(2, mutter.getText());
			if (pStmt.executeUpdate() != 1) return null;
			try (ResultSet keys = pStmt.getGeneratedKeys()) {
				if (!keys.next()) return null;
				generatedId = keys.getInt(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException("つぶやきの作成に失敗しました。", e);
		}
		return findById(generatedId);
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

	/**
	 * 指定されたユーザー自身のつぶやきを更新する。
	 */
	public boolean update(Mutter mutter) {
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("JDBCドライバのロードに失敗しました。", e);
		}

		String sql = "UPDATE MUTTERS SET TEXT = ?, VERSION = VERSION + 1 "
				+ "WHERE ID = ? AND USER_ID = ? AND VERSION = ?";
		try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
				PreparedStatement pStmt = conn.prepareStatement(sql)) {
			pStmt.setString(1, mutter.getText());
			pStmt.setInt(2, mutter.getId());
			pStmt.setInt(3, mutter.getUserId());
			pStmt.setInt(4, mutter.getVersion());
			return pStmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 指定されたユーザー自身のつぶやきを削除する。
	 */
	public boolean delete(int mutterId, int userId) {
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("JDBCドライバのロードに失敗しました。", e);
		}

		String sql = "DELETE FROM MUTTERS WHERE ID = ? AND USER_ID = ?";
		try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
				PreparedStatement pStmt = conn.prepareStatement(sql)) {
			pStmt.setInt(1, mutterId);
			pStmt.setInt(2, userId);
			return pStmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
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

			String sql = "SELECT m.ID, m.USER_ID, u.NAME, m.TEXT, m.VERSION, m.CREATED_AT "
					+ "FROM MUTTERS m JOIN USERS u ON m.USER_ID = u.ID "
					+ "WHERE m.TEXT LIKE ? ORDER BY m.ID DESC";

			PreparedStatement pStmt = conn.prepareStatement(sql);

			pStmt.setString(1, "%" + keyword + "%");

			ResultSet rs = pStmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("ID");
				int userId = rs.getInt("USER_ID");
				String userName = rs.getString("NAME");
				String text = rs.getString("TEXT");
				int version = rs.getInt("VERSION");
				java.time.LocalDateTime createdAt = rs.getTimestamp("CREATED_AT").toLocalDateTime();
				Mutter mutter = new Mutter(id, userId, userName, text, version, createdAt);
				mutterList.add(mutter);
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}

		return mutterList;

	}

	private void loadDriver() {
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("JDBCドライバのロードに失敗しました。", e);
		}
	}

	private Mutter toMutter(ResultSet rs) throws SQLException {
		int id = rs.getInt("ID");
		int userId = rs.getInt("USER_ID");
		String userName = rs.getString("NAME");
		String text = rs.getString("TEXT");
		int version = rs.getInt("VERSION");
		java.time.LocalDateTime createdAt = rs.getTimestamp("CREATED_AT").toLocalDateTime();
		return new Mutter(id, userId, userName, text, version, createdAt);
	}
}
