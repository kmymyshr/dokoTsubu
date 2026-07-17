package support;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.Flyway;

import util.DBUtil;

/**
 * DAOの特性テスト用に、埋め込みH2(jdbc:h2:mem:...)へ接続先を切り替え、
 * Flywayの本番用マイグレーションを適用してスキーマを用意するヘルパー。
 * テスト専用DDLを持たず、本番とテストのスキーマ定義を同一に保つ。
 *
 * (モダナイゼーション計画 Phase0: 安全網構築)
 */
public final class TestDatabaseSupport {
    private TestDatabaseSupport() {
    }

    /**
     * 呼び出しごとに独立した埋め込みH2を用意し、DBUtilの接続先を切り替える。
     * DB_CLOSE_DELAY=-1 は、Flywayの接続が閉じてからDAO用プールが接続するまでの間に
     * インメモリDBが消えないようにするために必要。
     */
    public static void useFreshInMemoryDatabase(String dbName) {
        String url = "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1";
        System.setProperty("db.url", url);
        System.setProperty("db.user", "sa");
        System.setProperty("db.password", "");
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    /** 各テストの前に呼び、テーブルの中身だけを空にする(スキーマは再作成しない)。 */
    public static void clearAllTables() {
        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM MUTTER_LIKES");
            stmt.execute("DELETE FROM FOLLOWS");
            stmt.execute("DELETE FROM MUTTERS");
            stmt.execute("DELETE FROM USERS");
            stmt.execute("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
            stmt.execute("ALTER TABLE MUTTERS ALTER COLUMN ID RESTART WITH 1");
            stmt.execute("ALTER TABLE MUTTER_LIKES ALTER COLUMN ID RESTART WITH 1");
            stmt.execute("ALTER TABLE FOLLOWS ALTER COLUMN ID RESTART WITH 1");
        } catch (SQLException e) {
            throw new RuntimeException("テスト用テーブルの初期化に失敗しました。", e);
        }
    }
}
