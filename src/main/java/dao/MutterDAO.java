package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Mutter;
import model.MutterFeedItem;
import util.DBUtil;

public class MutterDAO {
	// 接続先情報をここで個別に持たず、DBUtilに一本化した。
	// 従来はDBUtilと同じ接続先(URL/USER/PASS)をこのクラスにも複製しており、
	// 片方だけ変更すると本番とテストで接続先がずれる不具合の温床になっていたため。
	// (モダナイゼーション計画 Phase0/1: DB接続情報の重複解消)

	/**
	 * 最新のつぶやきを指定件数だけ取得する。
	 * 主にトップページの表示で使う。
	 */
	public List<Mutter> findLatest(int limit) {
		List<Mutter> mutterList = new ArrayList<>();
		try (Connection conn = DBUtil.getConnection()) {

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
				mutterList.add(toMutter(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return mutterList;
	}

	/**
	 * 指定したカーソルより古いつぶやきを取得する。
	 * 無限スクロールなどで使う。
	 */
	public List<Mutter> findByCursor(int cursor, int limit) {
		List<Mutter> mutterList = new ArrayList<>();

		String sql = "SELECT m.ID, m.USER_ID, u.NAME, m.TEXT, m.VERSION, m.CREATED_AT "
				+ "FROM MUTTERS m JOIN USERS u ON m.USER_ID = u.ID "
				+ "WHERE m.ID < ? ORDER BY m.ID DESC LIMIT ?";
		try (Connection conn = DBUtil.getConnection();
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

	/**
	 * REST API 用に、検索条件とカーソルをまとめて適用して取得する。
	 * キーワード検索やページネーションに対応する。
	 */
	public List<Mutter> findPage(String keyword, Integer cursor, int limit) {
		List<Mutter> mutterList = new ArrayList<>();
		StringBuilder sql = new StringBuilder(
				"SELECT m.ID, m.USER_ID, u.NAME, m.TEXT, m.VERSION, m.CREATED_AT "
				+ "FROM MUTTERS m JOIN USERS u ON m.USER_ID = u.ID WHERE 1 = 1 ");
		if (keyword != null) sql.append("AND m.TEXT LIKE ? ");
		if (cursor != null) sql.append("AND m.ID < ? ");
		sql.append("ORDER BY m.ID DESC LIMIT ?");
		try (Connection conn = DBUtil.getConnection();
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

	/**
	 * Fetches a feed page and its viewer-specific display data in one SQL statement.
	 */
	public List<MutterFeedItem> findFeedPage(
			String keyword, Integer cursor, int limit, int viewerId) {
		List<MutterFeedItem> items = new ArrayList<>();
		StringBuilder sql = new StringBuilder(
				"WITH page AS ("
				+ "SELECT m.ID, m.USER_ID, u.NAME, m.TEXT, m.VERSION, m.CREATED_AT "
				+ "FROM MUTTERS m JOIN USERS u ON m.USER_ID = u.ID WHERE 1 = 1 ");
		if (keyword != null) sql.append("AND m.TEXT LIKE ? ");
		if (cursor != null) sql.append("AND m.ID < ? ");
		sql.append("ORDER BY m.ID DESC LIMIT ?) ")
				.append("SELECT p.ID, p.USER_ID, p.NAME, p.TEXT, p.VERSION, p.CREATED_AT, ")
				.append("(SELECT COUNT(*) FROM MUTTER_LIKES ml WHERE ml.MUTTER_ID = p.ID) AS LIKE_COUNT, ")
				.append("EXISTS (SELECT 1 FROM MUTTER_LIKES my_like ")
				.append("WHERE my_like.MUTTER_ID = p.ID AND my_like.USER_ID = ?) AS LIKED_BY_ME, ")
				.append("EXISTS (SELECT 1 FROM FOLLOWS f ")
				.append("WHERE f.FOLLOWER_ID = ? AND f.FOLLOWEE_ID = p.USER_ID) AS FOLLOWED_BY_ME ")
				.append("FROM page p ORDER BY p.ID DESC");

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql.toString())) {
			int index = 1;
			if (keyword != null) pStmt.setString(index++, "%" + keyword + "%");
			if (cursor != null) pStmt.setInt(index++, cursor);
			pStmt.setInt(index++, limit);
			pStmt.setInt(index++, viewerId);
			pStmt.setInt(index, viewerId);
			try (ResultSet rs = pStmt.executeQuery()) {
				while (rs.next()) {
					items.add(new MutterFeedItem(
							toMutter(rs),
							rs.getInt("LIKE_COUNT"),
							rs.getBoolean("LIKED_BY_ME"),
							rs.getBoolean("FOLLOWED_BY_ME")));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch the mutter feed", e);
		}
		return items;
	}

	/**
	 * 編集画面や詳細表示で使うため、ID で 1 件だけ取得する。
	 */
	public Mutter findById(int mutterId) {
		String sql = "SELECT m.ID, m.USER_ID, u.NAME, m.TEXT, m.VERSION, m.CREATED_AT "
				+ "FROM MUTTERS m JOIN USERS u ON m.USER_ID = u.ID WHERE m.ID = ?";
		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql)) {
			pStmt.setInt(1, mutterId);
			ResultSet rs = pStmt.executeQuery();
			return rs.next() ? toMutter(rs) : null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 新しく作成したつぶやきを、採番された ID 付きで返す。
	 */
	public Mutter createAndReturn(Mutter mutter) {
		String sql = "INSERT INTO MUTTERS(USER_ID, TEXT) VALUES(?, ?)";
		int generatedId;
		try (Connection conn = DBUtil.getConnection();
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

	/**
	 * つぶやきを新規作成する。
	 * ここでは作成結果の成功/失敗だけを返す。
	 */
	public boolean create(Mutter mutter) {
		try (Connection conn = DBUtil.getConnection()) {
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
		String sql = "UPDATE MUTTERS SET TEXT = ?, VERSION = VERSION + 1 "
				+ "WHERE ID = ? AND USER_ID = ? AND VERSION = ?";
		try (Connection conn = DBUtil.getConnection();
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
		String sql = "DELETE FROM MUTTERS WHERE ID = ? AND USER_ID = ?";
		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql)) {
			pStmt.setInt(1, mutterId);
			pStmt.setInt(2, userId);
			return pStmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * キーワードを含むつぶやきを検索する。
	 * 旧検索機能で使われていたメソッド。
	 */
	public List<Mutter> search(String keyword) {
		List<Mutter> mutterList = new ArrayList<>();

		try (Connection conn = DBUtil.getConnection()) {

			String sql = "SELECT m.ID, m.USER_ID, u.NAME, m.TEXT, m.VERSION, m.CREATED_AT "
					+ "FROM MUTTERS m JOIN USERS u ON m.USER_ID = u.ID "
					+ "WHERE m.TEXT LIKE ? ORDER BY m.ID DESC";

			PreparedStatement pStmt = conn.prepareStatement(sql);

			pStmt.setString(1, "%" + keyword + "%");

			ResultSet rs = pStmt.executeQuery();

			while (rs.next()) {
				mutterList.add(toMutter(rs));
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}

		return mutterList;

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
