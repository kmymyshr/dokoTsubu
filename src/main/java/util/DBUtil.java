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

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2ドライバが見つかりません。", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getProperty("db.url", DEFAULT_JDBC_URL);
        String user = System.getProperty("db.user", DEFAULT_DB_USER);
        String password = System.getProperty("db.password", DEFAULT_DB_PASS);
        return DriverManager.getConnection(url, user, password);
    }
}