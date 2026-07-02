package model;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import util.PasswordUtil;

public class UserRegister {

    public static void main(String[] args) {

        // 本来は画面やフォームから受け取る値
        String loginId = "tanaka";
        String plainPassword = "password123";

        // DB接続情報
        String url = "jdbc:h2:tcp://localhost/~/dokoTsubuHashed";
        String dbUser = "sa";
        String dbPassword = "";

        // パスワードをbcryptでハッシュ化
        String passwordHash = PasswordUtil.hashPassword(plainPassword);

        String sql = "INSERT INTO users (login_id, password_hash) VALUES (?, ?)";

        try (
            Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, loginId);
            ps.setString(2, passwordHash);

            int result = ps.executeUpdate();

            if (result == 1) {
                System.out.println("ユーザー登録成功");
            } else {
                System.out.println("ユーザー登録失敗");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}