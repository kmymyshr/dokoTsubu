# dokoTsubu

[![CI](https://github.com/kmymyshr/dokoTsubu/actions/workflows/ci.yml/badge.svg)](https://github.com/kmymyshr/dokoTsubu/actions/workflows/ci.yml)

Java Servlet / JSP をベースに開発し、**React + REST API**
へ段階的に移行しているミニSNSアプリです。

ユーザー登録・投稿・検索・いいね・フォローなどの基本機能に加え、CSRF対策や楽観ロックなど、Webアプリケーション開発で重要となる要素を実装しています。

------------------------------------------------------------------------

# デモ

-   **URL**：https://dokotsubu-j38p.onrender.com/dokoTsubu/Main
-   **テストユーザー**：
    -   ID：demo_user1	
    -   Password：DemoPass_101!


------------------------------------------------------------------------

# 使用技術

  分類        技術
  ----------- ---------------------------
  Language    Java 21
  Backend     Spring Boot 3.5 / Jakarta Servlet 6 / JSP
  Frontend    React 19 / Vite
  Database    PostgreSQL / H2 / Flyway
  Build       Maven
  Container   Docker / Embedded Tomcat 10.1
  Deploy      Render
  Test        JUnit5 / Mockito / Vitest

------------------------------------------------------------------------

# CI

GitHub Actionsで、`main`ブランチへのpushと`main`ブランチ向けのPull
Requestを対象に、以下を自動実行します。

- Java・Reactのテスト
- Viteの本番ビルド
- WARファイルの生成と成果物保存
- Dockerイメージのビルド確認

ローカルでは次のコマンドで、テストからWAR生成までを確認できます。

``` shell
mvn --batch-mode --no-transfer-progress clean verify
```

------------------------------------------------------------------------

# ローカル実行

Spring Bootの実行可能WARとして起動します。H2 Serverを既定値で利用する場合は、
次のコマンドで起動後に `http://localhost:8080/dokoTsubu/` へアクセスします。

``` shell
mvn spring-boot:run
```

PostgreSQLなど別のデータベースを利用する場合は、環境変数で接続先を指定します。

``` text
DB_URL=jdbc:postgresql://localhost:5432/dokotsubu
DB_USER=dokotsubu
DB_PASSWORD=change-me
DB_MAXIMUM_POOL_SIZE=10
FLYWAY_BASELINE_ON_MIGRATE=false
PORT=8080
```

Dockerでは次のように起動できます。

``` shell
docker build -t dokotsubu .
docker run --rm -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/dokotsubu \
  -e DB_USER=dokotsubu \
  -e DB_PASSWORD=change-me \
  dokotsubu
```

------------------------------------------------------------------------

# データベースマイグレーション

アプリケーション起動時にFlywayが
`src/main/resources/db/migration`のマイグレーションを検証・適用します。
新規データベースでは、`V1__initial_schema.sql`からスキーマを作成します。
適用履歴は`flyway_schema_history`テーブルで管理されます。

既存のPostgreSQLを初めてFlyway管理へ移す場合は、次の手順で一度だけ
ベースライン化します。

1. データベースをバックアップする
2. `db/migration/postgresql_baseline_preflight.sql`を実行する
3. 不整合件数がすべて0で、テーブル・列・制約がV1と一致することを確認する
4. 初回起動時だけ`FLYWAY_BASELINE_ON_MIGRATE=true`を設定する
5. `flyway_schema_history`にバージョン1のBaselineが記録されたことを確認する
6. 以降は`FLYWAY_BASELINE_ON_MIGRATE=false`へ戻す

`baseline-on-migrate`は、接続先を誤った場合の安全確認を弱めるため、
既定値では無効です。また、ベースライン化は既存スキーマとV1の構造を
自動比較しないため、事前確認を省略しないでください。

既存のマイグレーションファイルは変更せず、今後のスキーマ変更は
`V2__...sql`、`V3__...sql`のように新しいファイルとして追加します。

------------------------------------------------------------------------

# 主な機能

-   ユーザー登録・ログイン
-   投稿・編集・削除
-   キーワード検索
-   いいね機能
-   フォロー機能
-   REST API
-   5秒ごとの自動更新
-   CSRF対策
-   楽観ロックによる更新競合防止

------------------------------------------------------------------------

# アーキテクチャ

``` text
Browser
    │
React / JSP
    │
REST API
    │
Spring Boot
    │
Logic
    │
DAO
    │
PostgreSQL / H2
```

画面表示（React）、業務処理（Logic）、データアクセス（DAO）の責務を分離し、ReactとバックエンドはREST APIを介して通信することで、互いの実装に依存しにくい疎結合な構成としています。

------------------------------------------------------------------------

# 工夫した点

## Reactへの段階的移行

既存のServlet / JSPアプリを一度に作り直すのではなく、REST
APIを追加しながらReactへ段階的に移行しました。既存資産を活かしつつモダナイズする構成を意識しています。

## セキュリティ

-   CSRF対策を実装
-   パスワードをBCryptでハッシュ化
-   更新時は楽観ロックを採用し、同時編集による競合を防止

## データベース設計

公開環境ではPostgreSQLを採用し、

-   外部キー制約
-   UNIQUE制約
-   NOT NULL制約

を設定することでデータ整合性を担保しています。

## 保守性

-   DAO・Logic・Servletで責務を分離
-   DTOを利用してAPIレスポンスを整理
-   ReactからREST API経由でデータ取得を行う構成にしています。

------------------------------------------------------------------------

# 今後の改善予定

-   Spring Security導入
-   Spring Data JDBCへの移行
-   CI/CDの構築
-   旧JSP画面のReact統合

------------------------------------------------------------------------

# 学習を通して

本プロジェクトでは、従来型のJava
Webアプリケーションをベースに、React・REST
API・Docker・PostgreSQLなどを組み合わせながら、段階的にモダンな構成へ移行する開発を行いました。

機能追加だけでなく、保守性・セキュリティ・データ整合性を意識した設計と改善を重視しています。
