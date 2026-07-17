package com.example.dokotsubu.service;

import com.example.dokotsubu.persistence.SpringDataJdbcGateway;
import model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * ユーザー関連の業務処理をまとめるService。
 *
 * <p>Phase5で、ログイン・登録・プロフィール更新の入口をDAO/LogicからこのServiceへ移した。
 * パスワード照合やハッシュ化のような業務ルールをここに置くことで、ServletやSpring Security設定が
 * DBアクセスの詳細を知らなくて済む構成にしている。</p>
 */
@Service
public class UserService {
    private final SpringDataJdbcGateway gateway;
    private final PasswordEncoder passwordEncoder;

    public UserService(SpringDataJdbcGateway gateway, PasswordEncoder passwordEncoder) {
        this.gateway = gateway;
        this.passwordEncoder = passwordEncoder;
    }

    /** 画面表示やセッション更新で使うユーザーID検索。 */
    public User findById(int id) {
        return gateway.findUserById(id);
    }

    /** Spring Securityの認証処理と重複登録チェックで使うユーザー名検索。 */
    public User findByName(String name) {
        return gateway.findUserByName(name);
    }

    /**
     * 平文パスワードを受け取り、保存済みのBCryptハッシュと照合する。
     *
     * <p>Phase16で旧LoginLogicを撤去したため、ログイン判定の業務ルールは
     * このServiceに集約する。Spring Security側から扱いやすいよう、失敗時は
     * 既存仕様に合わせてnullを返す。</p>
     */
    public User authenticate(String name, String rawPassword) {
        User user = findByName(name);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPass())) {
            return null;
        }
        return user;
    }

    /** 新規登録時は、この層で必ずパスワードをハッシュ化してから永続化する。 */
    public boolean register(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPass());
        return gateway.createUser(new User(user.getName(), encodedPassword, user.getBio()));
    }

    /** プロフィール画面からの自己紹介更新をService経由に集約する。 */
    public boolean updateBio(int userId, String bio) {
        return gateway.updateUserBio(userId, bio);
    }
}
