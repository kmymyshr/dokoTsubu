package util;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBUtil {

    // 本番のデフォルト接続先。システムプロパティ(db.url/db.user/db.password)で上書きできるようにし、
    // 特性テストでは埋め込みH2(jdbc:h2:mem:...)を指せるようにした。
    // プロパティ未設定時は従来と全く同じ接続先になるため、本番の挙動は変わらない。
    // (モダナイゼーション計画 Phase0: 安全網構築のためのテスト用シーム)
    private static final String DEFAULT_JDBC_URL =
            "jdbc:h2:tcp://localhost/~/dokoTsubu";
    private static final String DEFAULT_DB_USER = "sa";
    private static final String DEFAULT_DB_PASS = "";
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
    private static volatile PoolHolder poolHolder;

    public static Connection getConnection() throws SQLException {
        String url = getSetting("db.url", "DB_URL", DEFAULT_JDBC_URL);
        String user = getSetting("db.user", "DB_USER", DEFAULT_DB_USER);
        String password = getSetting("db.password", "DB_PASSWORD", DEFAULT_DB_PASS);
        int maximumPoolSize = getPositiveIntSetting(
                "db.maximumPoolSize", "DB_MAXIMUM_POOL_SIZE", DEFAULT_MAXIMUM_POOL_SIZE);
        PoolSettings settings = new PoolSettings(url, user, password, maximumPoolSize);
        return getOrCreateDataSource(settings).getConnection();
    }

    private static HikariDataSource getOrCreateDataSource(PoolSettings settings) throws SQLException {
        PoolHolder current = poolHolder;
        if (current != null && current.settings().equals(settings)) {
            return current.dataSource();
        }
        synchronized (DBUtil.class) {
            current = poolHolder;
            if (current != null && current.settings().equals(settings)) {
                return current.dataSource();
            }
            loadDriver(settings.url());
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(settings.url());
            config.setUsername(settings.user());
            config.setPassword(settings.password());
            config.setMaximumPoolSize(settings.maximumPoolSize());
            config.setMinimumIdle(0);
            config.setConnectionTimeout(10_000);
            config.setIdleTimeout(60_000);
            config.setMaxLifetime(30 * 60_000L);
            config.setPoolName("dokoTsubu-db");
            HikariDataSource replacement = new HikariDataSource(config);
            poolHolder = new PoolHolder(settings, replacement);
            if (current != null) {
                current.dataSource().close();
            }
            return replacement;
        }
    }

    /** Closes the application pool during web-application shutdown. */
    public static synchronized void shutdown() {
        PoolHolder current = poolHolder;
        poolHolder = null;
        if (current != null) {
            current.dataSource().close();
        }
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

    private static int getPositiveIntSetting(
            String systemProperty, String environmentVariable, int defaultValue) {
        String value = getSetting(systemProperty, environmentVariable, Integer.toString(defaultValue));
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private record PoolSettings(String url, String user, String password, int maximumPoolSize) {
    }

    private record PoolHolder(PoolSettings settings, HikariDataSource dataSource) {
    }
}
