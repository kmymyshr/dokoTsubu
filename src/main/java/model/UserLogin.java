package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.PasswordUtil;

public class UserLogin {

    public static void main(String[] args) {

        // 本来は画面やフォームから受け取る値
        String inputLoginId = "tanaka";
        String inputPassword = "password123";

        // DB接続情報
        String url = "jdbc:h2:tcp://localhost/~/dokoTsubuHashed";
        String dbUser = "sa";
        String dbPassword = "";

        String sql = "SELECT password_hash FROM users WHERE login_id = ?";

        try (
            Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, inputLoginId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    System.out.println("ログイン失敗：ユーザーが存在しません");
                    return;
                }

                String storedHash = rs.getString("password_hash");

                boolean isLogin = PasswordUtil.checkPassword(inputPassword, storedHash);

                if (isLogin) {
                    System.out.println("ログイン成功");
                } else {
                    System.out.println("ログイン失敗：パスワードが違います");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

