package com.example.dokotsubu.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * Spring管理外に残る既存Servlet/Logicから、Spring管理のServiceへ接続する移行用ブリッジ。
 *
 * <p>Phase5では業務処理の入口をService層へ集約したが、既存のServlet/JSP構成には
 * `new XxxLogic()` で処理を呼び出す箇所がまだ残っている。そのため、全面的なDI化が
 * 完了するまでの中継点として、このクラスから現在のService Beanを取得できるようにしている。</p>
 */
@Component
public class ApplicationServiceBridge {
    /** Springが生成した最新のブリッジを、既存コードから参照できるように保持する。 */
    private static volatile ApplicationServiceBridge current;

    /** ユーザー、投稿、ソーシャル機能を用途別に分けたService。 */
    private final UserService users;
    private final MutterService mutters;
    private final SocialService social;

    public ApplicationServiceBridge(UserService users, MutterService mutters, SocialService social) {
        this.users = users;
        this.mutters = mutters;
        this.social = social;
        current = this;
    }

    /** 既存の認証・プロフィール処理からユーザーServiceを参照する。 */
    public static UserService users() {
        return get().users;
    }

    /** 既存の投稿処理から投稿Serviceを参照する。 */
    public static MutterService mutters() {
        return get().mutters;
    }

    /** 既存のいいね・フォロー処理からソーシャルServiceを参照する。 */
    public static SocialService social() {
        return get().social;
    }

    /** Springコンテキスト初期化前に呼ばれた場合は、原因が分かる例外にする。 */
    private static ApplicationServiceBridge get() {
        ApplicationServiceBridge bridge = current;
        if (bridge == null) {
            throw new IllegalStateException("アプリケーションServiceが初期化されていません");
        }
        return bridge;
    }

    /** テストや再起動で古いApplicationContextのBeanを参照し続けないように切り離す。 */
    @PreDestroy
    void detach() {
        if (current == this) {
            current = null;
        }
    }
}
