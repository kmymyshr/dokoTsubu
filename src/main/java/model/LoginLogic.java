package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.UserService;

/**
 * ログイン処理の旧Logic互換クラス。
 *
 * <p>Phase5でパスワード照合を {@link UserService} へ移した。旧Servlet/JSPの呼び出し形を
 * 残すため、このクラスはServiceへの委譲に限定している。</p>
 */
public class LoginLogic {
    private final UserService users;

    /** 既存コード向け。Spring管理Serviceは移行用ブリッジから取得する。 */
    public LoginLogic() {
        this(ApplicationServiceBridge.users());
    }

    /** テストではUserServiceを差し替えられるようにする。 */
    public LoginLogic(UserService users) {
        this.users = users;
    }

    /** 入力されたユーザー名/パスワードをServiceで認証する。 */
    public User login(User user) {
        return users.authenticate(user.getName(), user.getPass());
    }
}
