# dokoTsubu

[![CI](https://github.com/kmymyshr/dokoTsubu/actions/workflows/ci.yml/badge.svg)](https://github.com/kmymyshr/dokoTsubu/actions/workflows/ci.yml)

Java Servlet / JSP をベースに開発したミニSNSを、Spring Boot・Spring Security・Flyway・Spring Data JDBC・Service層・React + REST API へ段階的に移行しているアプリケーションです。

ユーザー登録、ログイン、投稿、検索、編集、削除、いいね、フォローなどの基本機能に加えて、CSRF対策、楽観ロック、PostgreSQL向けスキーマ管理、CIによる検証を整備しています。

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

## React統合

Phase6では、ログイン後のメイン投稿画面をReactアプリとして正式に扱うよう整理しました。

- `/Main` Servletは認証確認とReactホストJSPへのforwardに責務を縮小
- `main.jsp` はReactのroot要素とVite成果物を読み込むホストに変更
- React側で投稿一覧、投稿作成、検索、編集、削除、いいね、フォローを操作
- `SessionApiServlet` でログインユーザー情報とCSRFトークンをReactへ提供
- ReactコンポーネントとAPIクライアントの文言・コメントを日本語化

Phase7では、プロフィール画面を今後React化しやすいJSP互換画面として整理しました。

- `Profile` Servletの責務を認証確認、表示用データ準備、自己紹介更新に整理
- `profile.jsp` の表示文言を日本語化し、JSP側の責務を表示と最小限のフォームに限定
- 当時のJSP向けフォロー切り替え処理を整理し、後続PhaseのReactコンポーネントへ移しやすい形に変更

Phase8では、フォロー中/フォロワー一覧をReact化するためのAPIと画面整理を追加しました。

- `/api/follows` でフォロー中/フォロワー一覧をJSONとして取得可能
- `FollowListResponse` / `FollowUserSummaryResponse` で一覧表示用DTOを追加
- `followerList.jsp` / `followingList.jsp` はJSP互換を維持しつつ、React化時の置き換え箇所が分かるようコメントを追加
- 既存JSP画面の非同期フォロー操作を、後続PhaseのReact化に向けて一時的に集約

Phase9では、フォロー中/フォロワー一覧の表示をReactへ移しました。

- `FollowList` Reactコンポーネントを追加し、`/api/follows` のレスポンスで一覧を描画
- `followerList.jsp` / `followingList.jsp` はReactホストへ縮小
- `FollowerList` / `FollowingList` Servletは認証確認、対象ユーザー確認、Reactホストへのforwardに責務を縮小
- 旧JSP用の `followList.js` は削除

Phase10では、ユーザー登録画面をReact + REST APIへ移しました。

- `RegisterPage` Reactコンポーネントを追加し、入力・送信・完了表示をReactで担当
- `/api/register` を追加し、登録処理をJSON APIとして提供
- `Register` ServletはReactホスト表示に責務を縮小し、旧POST処理は互換用として保持
- `registerView.jsp` はReactホストへ変更し、CSRFトークンをReactへ渡す

Phase11では、プロフィール画面をReact + REST APIへ移しました。

- `ProfilePage` Reactコンポーネントを追加し、プロフィール表示、自己紹介編集、フォロー切り替えを担当
- `/api/profile` を追加し、プロフィール表示データ取得と本人の自己紹介更新をJSON APIとして提供
- `Profile` ServletはReactホスト表示に責務を縮小し、旧POST処理は互換用として保持
- `profile.jsp` はReactホストへ変更し、対象ユーザーIDとCSRFトークンをReactへ渡す

Phase12では、ログイン画面をReactホストへ移しました。

- `LoginPage` Reactコンポーネントを追加し、ログインフォーム、失敗メッセージ、ログアウト後メッセージを表示
- `index.jsp` はReactホストへ変更し、CSRFトークンとログイン結果フラグをReactへ渡す
- 認証処理はSpring Securityの `/Login` フォーム認証を維持
- ログイン成功時は `Main` へredirectし、失敗/ログアウト後はReactログイン画面へ戻す
- `loginResult.jsp` / `logout.jsp` はこの時点では互換用として残し、後続Phaseで削除対象にする

Phase13では、React移行後に参照されなくなった旧静的フロント資産を削除しました。

- `src/main/webapp/js/` 配下の旧JSP向けJavaScriptを削除
- `src/main/webapp/css/main.css` を削除し、Reactビルド成果物のCSSへ一本化
- Spring Securityの公開静的パスから未使用の `/css/**` と `/js/**` を削除

Phase14では、投稿タイムラインがReact + `/api/mutters` に一本化された後の旧投稿導線を削除しました。

- `/MutterList` / `/SearchMutter` / `/UpdateMutter` / `/DeleteMutter` の旧Servletを削除
- 旧編集画面 `updateMutter.jsp` を削除
- 投稿系の旧Logic互換クラスを削除し、投稿操作の入口を `MutterApiServlet` + `MutterService` に集約
- `MutterService` から旧Servlet専用の一覧/検索互換メソッドを削除

JSPは一部の互換画面や編集画面に残っていますが、投稿タイムライン、フォロー一覧、ユーザー登録、プロフィール、ログインはReact中心の構成へ移行しています。

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
- 移行意図が後から追えるよう、主要ファイルに役割・処理ブロックのコメントを追加

## 今後の改善予定

- 残りJSP互換画面の整理
- CI/CDの継続改善
- 移行用の互換DAO / Logicブリッジを段階的に縮小
- Service層とReactコンポーネントのテスト拡充

## 学習を通して

本プロジェクトでは、従来型のJava Webアプリケーションをベースに、React、REST API、Docker、PostgreSQL、Spring Security、Flyway、Spring Data JDBC、Service層を組み合わせながら、段階的にモダンな構成へ移行しています。

機能追加だけでなく、保守性、セキュリティ、データ整合性、CIで検証できる設計を重視しています。

## Phase15 登録画面の旧JSP完了導線を整理

Phase15では、ユーザー登録画面がReact + `/api/register` に移行済みであることを前提に、旧JSPフォーム互換として残っていた登録POST処理と完了JSPを整理しました。

- `/Register` ServletはReactホストJSPを表示するGET専用の入口に縮小
- 旧JSPフォームPOST用の登録処理を `/Register` から削除
- 登録完了表示はReact側で行うため、`registerResult.jsp` を削除
- 登録処理の正式な入口を `/api/register` に一本化

## Phase16 ログイン・登録の旧Logic互換クラスを整理

Phase16では、ログインがSpring Security、登録がReact + `/api/register` に移行済みであることを前提に、旧Servlet/JSP時代の薄いLogic互換クラスを削除しました。

- `LoginLogic` を削除し、認証判定の責務を `UserService.authenticate` に集約
- `RegisterUserLogic` を削除し、登録処理の責務を `UserService.register` と `/api/register` に集約
- `UserService` のコメントを現在の責務に合わせて更新

## Phase17 未使用JSP互換ファイルを整理

Phase17では、ログイン画面がReactホスト化され、ログイン成功/失敗/ログアウト後の遷移もSpring Security + Reactログイン画面に一本化済みであることを前提に、参照されなくなったJSPを削除しました。

- `loginResult.jsp` を削除
- `logout.jsp` を削除
- どこからもincludeされていなかった `footer.jsp` を削除
- 残るJSPはReactホストとして使う画面に絞り込み
