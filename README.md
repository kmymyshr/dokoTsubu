# dokoTsubu

[![CI](https://github.com/kmymyshr/dokoTsubu/actions/workflows/ci.yml/badge.svg)](https://github.com/kmymyshr/dokoTsubu/actions/workflows/ci.yml)

Java Servlet / JSP をベースに開発したミニSNSを、Spring Boot・Spring Security・Flyway・Spring Data JDBC・React + REST API へ段階的に移行しているアプリケーションです。

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

GitHub Actionsで、`main` ブランチへのpushと `main` 向けPull Requestを対象に次を自動実行します。

- JavaとReactのテスト
- Viteの本番ビルド
- WARファイルの生成と成果物保存
- Dockerイメージのビルド確認

ローカルでは次のコマンドで、テストからWAR生成まで確認できます。

```shell
mvn --batch-mode --no-transfer-progress clean verify
```

## ローカル実行

Spring Bootの実行可能WARとして起動します。標準設定ではH2 Serverを使うため、起動後に `http://localhost:8080/dokoTsubu/` へアクセスします。

```shell
mvn spring-boot:run
```

PostgreSQLなど別のデータベースを使う場合は、環境変数で接続先を指定します。

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

既存のPostgreSQLを初めてFlyway管理へ移す場合は、次の手順で一度だけベースライン化します。

1. データベースをバックアップする
2. `db/migration/postgresql_baseline_preflight.sql` を実行する
3. 不整合件数がすべて0で、テーブル・列・制約がV1と一致することを確認する
4. 初回起動時だけ `FLYWAY_BASELINE_ON_MIGRATE=true` を設定する
5. `flyway_schema_history` にバージョン1のBaselineが記録されたことを確認する
6. 以降は `FLYWAY_BASELINE_ON_MIGRATE=false` に戻す

既存のマイグレーションファイルは変更せず、今後のスキーマ変更は `V2__...sql`、`V3__...sql` のように新しいファイルとして追加します。

## データアクセス

Phase4で、手書きJDBCと共有 `DataSource` ブリッジをSpring Data JDBCへ移行しました。

- `CrudRepository` でユーザー、投稿、いいね、フォローの基本CRUDを管理
- 複雑なタイムライン、検索、フォロー一覧は `NamedParameterJdbcTemplate` の読み取り専用Repositoryで明示的にSQLを管理
- 更新、削除、いいね、フォローはSpringのトランザクション境界で管理
- 既存のServlet/Logicがまだ `new DAO()` を使うため、互換DAOからSpring管理Beanへ委譲

この段階では既存画面とAPIの呼び出し形を保ちつつ、DBアクセスの実体をSpring管理へ寄せています。

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
Logic
  |
DAO互換アダプター
  |
Spring Data JDBC Repository / Query Repository
  |
PostgreSQL / H2
```

画面表示、業務処理、データアクセスの責務を分離し、ReactとバックエンドはREST APIを介して通信します。移行中のためJSPとReactが共存していますが、DBアクセスと認証・認可はSpring管理へ集約し始めています。

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

- Servlet、Logic、DAO、Repositoryの責務を分離
- DTOを利用してAPIレスポンスを整理
- 複雑な読み取りSQLを専用Repositoryに集約
- Spring Data JDBCにより基本CRUDとトランザクションをSpring管理へ移行

## 今後の改善予定

- CI/CDの継続改善
- 旧JSP画面のReact統合
- 互換DAOを段階的に廃止し、Logic層からSpring管理Serviceを直接利用する構成へ移行

## 学習を通して

本プロジェクトでは、従来型のJava Webアプリケーションをベースに、React、REST API、Docker、PostgreSQL、Spring Security、Flyway、Spring Data JDBCを組み合わせながら、段階的にモダンな構成へ移行しています。

機能追加だけでなく、保守性、セキュリティ、データ整合性、CIで検証できる設計を重視しています。
