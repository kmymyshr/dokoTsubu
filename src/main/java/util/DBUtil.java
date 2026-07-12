package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    // 本番のデフォルト接続先。システムプロパティ(db.url/db.user/db.password)で上書きできるようにし、
    // 特性テストでは埋め込みH2(jdbc:h2:mem:...)を指せるようにした。
    // プロパティ未設定時は従来と全く同じ接続先になるため、本番の挙動は変わらない。
    // (モダナイゼーション計画 Phase0: 安全網構築のためのテスト用シーム)
    private static final String DEFAULT_JDBC_URL =
            "jdbc:h2:tcp://localhost/~/dokoTsubu";
    private static final String DEFAULT_DB_USER = "sa";
    private static final String DEFAULT_DB_PASS = "";

    public static Connection getConnection() throws SQLException {
        String url = getSetting("db.url", "DB_URL", DEFAULT_JDBC_URL);
        String user = getSetting("db.user", "DB_USER", DEFAULT_DB_USER);
        String password = getSetting("db.password", "DB_PASSWORD", DEFAULT_DB_PASS);
        loadDriver(url);
        return DriverManager.getConnection(url, user, password);
    }

    private static void loadDriver(String url) throws SQLException {
        try {
            if (url.startsWith("jdbc:postgresql:")) {
                Class.forName("org.postgresql.Driver");
            } else if (url.startsWith("jdbc:h2:")) {
                Class.forName("org.h2.Driver");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not found for URL: " + url, e);
        }
    }

    private static String getSetting(String systemProperty, String environmentVariable, String defaultValue) {
        String propertyValue = System.getProperty(systemProperty);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }
        String environmentValue = System.getenv(environmentVariable);
        return environmentValue == null || environmentValue.isBlank() ? defaultValue : environmentValue;
    }
}
