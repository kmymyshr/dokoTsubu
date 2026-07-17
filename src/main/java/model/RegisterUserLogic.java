package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.UserService;

/**
 * ユーザー登録の旧Logic互換クラス。
 *
 * <p>Phase5で登録時のパスワードハッシュ化と永続化を {@link UserService} へ移した。
 * このクラスは既存の呼び出し口を維持するための薄い委譲層である。</p>
 */
public class RegisterUserLogic {
    private final UserService users;

    /** 既存コード向け。Spring管理Serviceは移行用ブリッジから取得する。 */
    public RegisterUserLogic() {
        this(ApplicationServiceBridge.users());
    }

    /** テストではUserServiceを差し替えられるようにする。 */
    public RegisterUserLogic(UserService users) {
        this.users = users;
    }

    /** Service側でパスワードをハッシュ化してから登録する。 */
    public boolean execute(User user) {
        return users.register(user);
    }
}
