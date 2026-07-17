package com.example.dokotsubu.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * Spring管理外の既存Servlet/Logicから、Spring管理Serviceへ接続する移行用ブリッジ。
 */
@Component
public class ApplicationServiceBridge {
    private static volatile ApplicationServiceBridge current;

    private final UserService users;
    private final MutterService mutters;
    private final SocialService social;

    public ApplicationServiceBridge(UserService users, MutterService mutters, SocialService social) {
        this.users = users;
        this.mutters = mutters;
        this.social = social;
        current = this;
    }

    public static UserService users() {
        return get().users;
    }

    public static MutterService mutters() {
        return get().mutters;
    }

    public static SocialService social() {
        return get().social;
    }

    private static ApplicationServiceBridge get() {
        ApplicationServiceBridge bridge = current;
        if (bridge == null) {
            throw new IllegalStateException("アプリケーションServiceが初期化されていません");
        }
        return bridge;
    }

    @PreDestroy
    void detach() {
        if (current == this) {
            current = null;
        }
    }
}
