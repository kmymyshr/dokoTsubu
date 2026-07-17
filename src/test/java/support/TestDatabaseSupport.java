package support;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Spring Data JDBC特性テストで、Flyway適用済みテーブルのデータを初期化する。
 */
public final class TestDatabaseSupport {
    private TestDatabaseSupport() {
    }

    public static void clearAllTables(JdbcTemplate jdbc) {
        jdbc.execute("DELETE FROM MUTTER_LIKES");
        jdbc.execute("DELETE FROM FOLLOWS");
        jdbc.execute("DELETE FROM MUTTERS");
        jdbc.execute("DELETE FROM USERS");
        jdbc.execute("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
        jdbc.execute("ALTER TABLE MUTTERS ALTER COLUMN ID RESTART WITH 1");
        jdbc.execute("ALTER TABLE MUTTER_LIKES ALTER COLUMN ID RESTART WITH 1");
        jdbc.execute("ALTER TABLE FOLLOWS ALTER COLUMN ID RESTART WITH 1");
    }
}
