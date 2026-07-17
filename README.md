# dokoTsubu

[![CI](https://github.com/kmymyshr/dokoTsubu/actions/workflows/ci.yml/badge.svg)](https://github.com/kmymyshr/dokoTsubu/actions/workflows/ci.yml)

Java Servlet / JSP をベースに開発したミニSNSを、Spring Boot・Spring Security・Flyway・Spring Data JDBC・Service層・React + REST API へ段階的に移行しているアプリケーションです。

ユーザー登録、ログイン、投稿、検索、いいね、フォローなどの基本機能に加えて、CSRF対策、楽観ロック、PostgreSQL向けスキーマ管理、CIによる検証を整備しています。

## デモ

- URL: https://dokotsubu-j38p.onrender.com/dokoTsubu/Main
- テストユーザー:
  - ID: `demo_user1`
  - Password: `demoPass_101!`

## 使用技術

| 分類 | 技術 |
| --- | --- |
| Language | Java 21 |
| Backend | Spring Boot 3.5 / Spring Security 6.5 / Spring Data JDBC / Jakarta Servlet 6 / JSP |
| Frontend | React 19 / Vite |
| Database | PostgreSQL / H2 / Flyway |
| Build | Maven |
| Container | Docker / Embedded Tomcat 10.1 |
| Deploy | Render |
| Test | JUnit 5 / Mockito / Vitest |

## CI

GitHub Actionsで、`main` ブランチへのpushと `main` 向けPull Requestを対象に検証を実行します。

- JavaとReactのテスト
- Viteの本番ビルド
- WARファイルの生成と成果物保存
- Dockerイメージのビルド確認

ローカルでは次のコマンドで、テストからWAR生成まで確認できます。

```shell
mvn --batch-mode --no-transfer-progress clean verify
```

## ローカル実行

Spring Bootの実行可能WARとして起動します。標準設定ではH2 Serverを使用するため、起動後に `http://localhost:8080/dokoTsubu/` へアクセスします。

```shell
mvn spring-boot:run
```

PostgreSQLなど別のデータベースを使用する場合は、環境変数で接続先を指定します。

```text
DB_URL=jdbc:postgresql://localhost:5432/dokotsubu
DB_USER=dokotsubu
DB_PASSWORD=change-me
DB_MAXIMUM_POOL_SIZE=10
FLYWAY_BASELINE_ON_MIGRATE=false
PORT=8080
```

Dockerでは次のように起動できます。

```shell
docker build -t dokotsubu .
docker run --rm -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/dokotsubu \
  -e DB_USER=dokotsubu \
  -e DB_PASSWORD=change-me \
  dokotsubu
```

## データベースマイグレーション

アプリケーション起動時にFlywayが `src/main/resources/db/migration` のマイグレーションを検証・適用します。新規データベースでは `V1__initial_schema.sql` からスキーマを作成し、適用履歴は `flyway_schema_history` テーブルで管理します。

既存のPostgreSQLをFlyway管理へ移す場合は、次の手順で一度だけベースライン化します。

1. データベースをバックアップする
2. `db/migration/postgresql_baseline_preflight.sql` を実行する
3. 不整合件数がすべて0で、テーブル・列・制約がV1と一致することを確認する
4. 初回起動時だけ `FLYWAY_BASELINE_ON_MIGRATE=true` を設定する
5. `flyway_schema_history` にバージョン1のBaselineが記録されたことを確認する
6. 以降は `FLYWAY_BASELINE_ON_MIGRATE=false` に戻す

既存のマイグレーションファイルは変更せず、今後のスキーマ変更は `V2__...sql`、`V3__...sql` のように新しいファイルとして追加します。

## データアクセスとService層

Phase4で手書きJDBCと共有 `DataSource` ブリッジをSpring Data JDBCへ移行し、Phase5で業務処理の入口をService層へ集約しました。

- `UserService` がユーザー検索、登録、認証、プロフィール更新を担当
- `MutterService` が投稿の作成、取得、検索、更新、削除、タイムライン取得を担当
- `SocialService` がいいね、フォロー、フォロワー/フォロー中一覧を担当
- 既存のServlet/Logicからは移行用ブリッジ経由でServiceを利用
- DBアクセスの実体はSpring Data JDBC Repository / Query Repositoryへ集約

この段階では既存画面とAPIの呼び出し形を保ちながら、業務処理とDBアクセスをSpring管理へ寄せています。

## 主な機能

- ユーザー登録、ログイン、ログアウト
- 投稿、検索、編集、削除
- いいね、フォロー
- REST API
- 5秒ごとの自動更新
- CSRF対策
- 投稿更新時の楽観ロック

## アーキテクチャ

```text
Browser
  |
React / JSP
  |
REST API / Servlet
  |
Spring Boot / Spring Security
  |
Service
  |
Spring Data JDBC Repository / Query Repository
  |
PostgreSQL / H2
```

画面表示、業務処理、データアクセスの責務を分離し、ReactとバックエンドはREST APIを介して通信します。移行中のためJSPとReactが共存していますが、認証・認可、Service、DBアクセスはSpring管理へ集約しています。

## 工夫した点

### Reactへの段階的移行

既存のServlet / JSPアプリケーションを一度に作り直すのではなく、REST APIを追加しながらReactへ段階的に移行しています。既存資産を活かしつつ、画面単位で置き換えられる構成にしています。

### セキュリティ

- Spring Securityでログイン、ログアウト、URL認可を一元管理
- Spring Securityのセッション方式でCSRF対策を実装
- APIでは未認証とCSRFエラーをJSONで返却
- パスワードをBCryptでハッシュ化
- 認証成功時にセッションIDを変更
- 更新時の楽観ロックで同時編集による競合を防止

### データベース設計

公開環境ではPostgreSQLを使用し、外部キー制約、UNIQUE制約、NOT NULL制約でデータ整合性を保証します。H2はFlyway適用済みスキーマで特性テストに利用します。

### 保守性

- Servlet / API、Service、Repositoryの責務を分離
- DTOを利用してAPIレスポンスを整理
- 複雑な読み取りSQLを専用Repositoryに集約
- Spring Data JDBCにより基本CRUDとトランザクションをSpring管理へ移行

## 今後の改善予定

- 旧JSP画面のReact統合
- CI/CDの継続改善
- 移行用の互換DAO / Logicブリッジを段階的に縮小
- Service層のテスト拡充

## 学習を通して

本プロジェクトでは、従来型のJava Webアプリケーションをベースに、React、REST API、Docker、PostgreSQL、Spring Security、Flyway、Spring Data JDBC、Service層を組み合わせながら、段階的にモダンな構成へ移行しています。

機能追加だけでなく、保守性、セキュリティ、データ整合性、CIで検証できる設計を重視しています。
