package com.example.dokotsubu.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * デプロイ環境からアプリケーションの起動状態を確認するためのヘルスチェックController。
 *
 * <p>Phase23でCD構築前の足場として追加した。Renderなどの外部サービスは、認証が必要な画面ではなく
 * この軽量な公開エンドポイントを叩くことで、アプリが起動してHTTP応答できる状態かを判定できる。</p>
 */
@RestController
public class HealthController {

    /**
     * アプリケーションがHTTPリクエストを受け付けられる状態であることを返す。
     *
     * <p>DBの詳細診断までは行わず、コンテナ・Spring Boot・ルーティングの生存確認に責務を絞る。
     * DB接続やFlywayの検証は起動時とCIのテストで確認する。</p>
     */
    @GetMapping("/health")
    ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "dokoTsubu"));
    }
}
