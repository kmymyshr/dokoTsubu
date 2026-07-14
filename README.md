# dokoTsubu

Java Servlet / JSP と React を組み合わせた、ひとこと投稿アプリです。
ログイン・投稿・検索・編集・削除に加えて、いいね、フォロー、REST API、CSRF 対策までを一通り含んだ学習用アプリとして構成しています。

現在は、JSP ベースの画面を残しつつ、メイン画面は React から REST API を呼び出す構成に移行しています。ローカルの H2 環境から、Render + Supabase PostgreSQL を用いた公開環境への移行も進めています。

<!-- スクリーンショット（任意）
![メイン画面](docs/screenshot-main.png)
-->

---

## 目次

1. [プロジェクト概要](#1-プロジェクト概要)
2. [主な機能](#2-主な機能)
3. [技術スタック](#3-技術スタック)
4. [アーキテクチャ](#4-アーキテクチャ)
5. [セットアップ](#5-セットアップ)
6. [データベース](#6-データベース)
7. [H2からPostgreSQLへの移行](#7-h2からpostgresqlへの移行)
8. [本番環境・デプロイ](#8-本番環境デプロイ)
9. [REST API](#9-rest-api)
10. [React実装](#10-react実装)
11. [テスト](#11-テスト)
12. [今後の改善予定](#12-今後の改善予定)
13. [変更履歴](#13-変更履歴)

---

## 1. プロジェクト概要

### アプリ概要

「どこつぶ」は、短い「つぶやき」を投稿・共有できるミニSNSです。ユーザー登録・ログイン後、つぶやきの投稿、キーワード検索、いいね、フォローといった一般的なSNSの基本機能をひと通り実装しています。

### 特徴

- JSP / Servlet ベースの従来型 Web アプリを土台にし、メイン画面は React + REST API 構成へ移行済です。
- 状態変更系リクエストに対する CSRF 対策を実装
- 楽観ロック（バージョン番号）による更新競合チェックを実装
- ローカル開発は H2、公開環境は PostgreSQL（Supabase）という異なる DB 構成を、同一の DAO / マイグレーションSQLで扱えるように整理
- Docker + Tomcat コンテナとして Render 上にデプロイ可能な構成

### 作成目的

このプロジェクトは、以下を学ぶための題材として使用しました。

- JSP / Servlet の従来型 MVC から React + REST API への段階的な移行の進め方
- DAO / Servlet / Logic といったレイヤー構成と責務分離
- CSRF 対策や楽観ロックなどの防御的実装
- H2（開発・テスト用）から PostgreSQL（公開環境）への移行手順
- Docker を使ったコンテナ化とクラウド（Render）へのデプロイ

---

## 2. 主な機能

- ユーザー登録
- ログイン / ログアウト
- つぶやきの投稿
- つぶやき一覧表示
- キーワード検索
- 自分のつぶやきの編集
- 自分のつぶやきの削除
- いいね / いいね解除
- フォロー / フォロー解除
- 5 秒ごとの自動更新
- カーソル方式の追加読み込み
- JSON REST API
- CSRF 対策
- 更新競合チェック（楽観ロック）

---

## 3. 技術スタック

| 分類 | 内容 |
|---|---|
| 言語 | Java 17 |
| Web | Jakarta Servlet 6.0 / JSP |
| フロントエンド | React 19 / Vite 8 |
| ビルド | Maven / frontend-maven-plugin |
| DB | PostgreSQL（公開環境・Supabase）/ H2（ローカル開発・テスト） |
| JSON | Jackson |
| パスワード | jBCrypt |
| コンテナ | Docker / Tomcat 11 |
| デプロイ先 | Render Web Service |
| テスト | JUnit 5 / Mockito / Vitest |

---

## 4. アーキテクチャ

```text
Browser
  ├─ JSP / Servlet
  └─ React UI
        ↓
     REST API
        ↓
      Logic
        ↓
       DAO
        ↓
    PostgreSQL（本番）/ H2（ローカル）
```

役割は次のように分けています。

- `servlet/`: JSP 画面遷移や従来画面向けのコントローラ
- `api/`: React から呼ぶ JSON API
- `model/`: アプリケーションロジック
- `dao/`: DB アクセス
- `dto/`: API レスポンス用 DTO
- `security/`: CSRF 対策
- `validation/`: 入力値検証
- `frontend/src/`: React アプリ

### ディレクトリ構成

```text
dokoTsubu/
├─ db/
│  └─ migration/
├─ docker/
│  └─ start-tomcat.sh
├─ Dockerfile
├─ frontend/
│  ├─ src/
│  │  ├─ components/
│  │  ├─ App.jsx
│  │  ├─ api.js
│  │  └─ main.jsx
│  ├─ package.json
│  └─ vite.config.js
├─ src/main/java/
│  ├─ api/
│  ├─ dao/
│  ├─ dto/
│  ├─ model/
│  ├─ security/
│  ├─ servlet/
│  └─ validation/
├─ src/main/resources/db/migration/
├─ src/main/webapp/
│  ├─ index.jsp
│  └─ WEB-INF/jsp/
├─ src/test/java/
├─ .gitignore
├─ pom.xml
└─ README.md
```

---

## 5. セットアップ

### 必要環境

- JDK 21
- Maven
- Tomcat 11 など Jakarta Servlet 6 対応コンテナ
- PostgreSQL（公開環境）
- H2 Database サーバー（従来のローカル確認用）
- Docker（Render やコンテナとして動かす場合）

Docker / Render では JDK 21 + Tomcat 11 を使用しています。
H2 は過去のローカル開発・移行元・一部テスト用途として残しており、公開環境では PostgreSQL を使用します。


### ローカル起動方法

#### PostgreSQL

公開環境相当で動かす場合は、あらかじめ PostgreSQL データベースを用意し、[6. データベース](#6-データベース)のスキーマを適用します。

#### H2（従来のローカル環境）

```text
jdbc:h2:tcp://localhost/~/dokoTsubu
user: sa
password: なし
```

特別な設定をしない場合は、このローカル H2 に接続します。

#### 環境変数

PostgreSQL 接続時は、公開環境の Secret またはローカルの環境変数へ設定します。

```text
DB_URL=jdbc:postgresql://localhost:5432/dokotsubu
DB_USER=dokotsubu_app
DB_PASSWORD=推測困難なパスワード
```

Java のシステムプロパティ `db.url` / `db.user` / `db.password` はテスト用途として引き続き環境変数より優先されます。未設定時のみ従来のローカル H2 へ接続します。

PostgreSQL 接続時は、Tomcat 上で JDBC ドライバの自動検出が効かない場合に備えて、`DBUtil` が URL に応じてドライバを明示ロードします。

- `jdbc:postgresql:` → `org.postgresql.Driver`
- `jdbc:h2:` → `org.h2.Driver`

#### ビルド

```sh
mvn clean package
```

Maven 実行時に frontend-maven-plugin が React 側もビルドし、WAR に同梱します。

生成物:

```text
target/dokoTsubu.war
```

Tomcat に配置した場合の想定 URL:

```text
http://localhost:8080/dokoTsubu/
```

#### ローカルTomcat 11での起動例

Pleiades 同梱 Tomcat 11 を使う場合の例です。環境に合わせてパスは読み替えてください。

```powershell
$env:JAVA_HOME="C:\pleiades\2025-12\java\21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

$env:DB_URL="jdbc:postgresql://localhost:5432/dokotsubu"
$env:DB_USER="dokotsubu_app"
$env:DB_PASSWORD="your_password"
```

WAR をビルドします。

```powershell
mvn clean package
```

Tomcat を停止したうえで、WAR を `webapps` 直下へ配置します。展開済みフォルダが残っている場合は削除してから置き換えると確実です。

```powershell
Remove-Item "C:\pleiades\2025-12\tomcat\11\webapps\dokoTsubu" -Recurse -Force

Copy-Item `
  "C:\Users\kmymy\VScode\Workspace\dokoTsubu\target\dokoTsubu.war" `
  "C:\pleiades\2025-12\tomcat\11\webapps\dokoTsubu.war" `
  -Force
```

起動ログを同じ PowerShell に表示したい場合は、`startup.bat` ではなく `catalina.bat run` を使います。環境変数も同じ PowerShell から渡せるため、接続確認時に便利です。

```powershell
cd "C:\pleiades\2025-12\tomcat\11\bin"
.\catalina.bat run
```

---

## 6. データベース

### 主なテーブル

- `USERS`
- `MUTTERS`
- `MUTTER_LIKES`
- `FOLLOWS`

`MUTTER_LIKES` と `FOLLOWS` は一意制約で重複登録を防いでいます。

<!-- ER図（将来追加してもよい） -->

### PostgreSQL（公開環境）での堅牢化

公開環境では、アプリの入力チェックだけに依存せず、データベースでも参照整合性を保証します。

- `MUTTERS.USER_ID` は存在する `USERS.ID` のみ参照可能（外部キー）
- `MUTTER_LIKES` は存在する投稿・ユーザーのみ参照可能（外部キー）
- `FOLLOWS` は存在するフォロワー・フォロー対象のみ参照可能（外部キー）
- いいね・フォローの組み合わせは複合 UNIQUE 制約で重複不可
- 全テーブルの `CREATED_AT` を `NOT NULL` とし、作成日時の欠落を防止
- 投稿削除時は関連するいいねを `ON DELETE CASCADE` で削除
- ユーザー削除時は関連するフォロー関係を `ON DELETE CASCADE` で削除

外部キーは、バグや管理操作によって「存在しないユーザーの投稿」のような孤児データが作られることを最後の砦として防ぎます。`NOT NULL` は日時を前提とする並び替えや監査処理を単純かつ確実にします。制約付きの初期スキーマは `src/main/resources/db/migration/V1__initial_postgresql_schema.sql` に置いています。

DAO がリクエストごとに DDL を実行する方式は廃止しました。スキーマ変更をバージョン管理された SQL へ集約することで、ローカル・検証・公開環境で同じ定義を再現できます。このファイル名は Flyway のバージョン付きマイグレーション規則にも対応しています。

より具体的には、以前は、DAOがリクエストを処理するたびにテーブル作成SQL（DDL）を実行していました。現在はその方式を廃止し、データベースのテーブル定義を V1__initial_postgresql_schema.sql にまとめています。これにより、ローカル・検証環境・本番環境で同じデータベース構成を再現しやすくなりました。また、このファイル名はFlywayの命名規則に従っているため、将来的にFlywayを導入した場合もそのまま利用できます（現状はFlyway自体はまだ導入していません。Spring Bootへ移行した際にFlywayを組みこむ予定です）。

Flyway:データベースの変更をバージョン管理するためのツール

### H2（ローカル・移行元）との違い

ローカルの H2 環境は、開発・テストの手軽さを優先しており、上記のような外部キー制約や NOT NULL 制約が一部緩い状態でした。PostgreSQL への移行にあたり、これらの制約を明示的に追加しています。そのため、H2 側で不整合なデータ（孤児データや NULL の日時）が残っていると、PostgreSQL への移行時に制約違反として検出されます。

---

## 7. H2からPostgreSQLへの移行

GitHub はソースコードの保管・共有先であり、Java アプリや PostgreSQL を常時実行するデプロイ先ではありません。デプロイ先に依存しない SQL と接続設定を用意し、実行環境に合わせて同じ手順を適用します。DB パスワードや CSV は GitHub へ登録しないでください。

### 1. H2データを検査する

PostgreSQLでは外部キー制約や NOT NULL 制約が有効になるため、移行前にH2のデータに不整合がないか確認します。問題のあるデータが残っていると、移行途中でエラーになり中断する可能性があります。

H2 コンソールで `db/migration/h2_pre_migration_checks.sql` を実行します。全ての `INVALID_COUNT` が `0` であることを確認してください。

NULL の作成日時だけが見つかった場合は、日時が不明になることを了承したうえで次のように補完できます。

```sql
UPDATE MUTTER_LIKES SET CREATED_AT = CURRENT_TIMESTAMP WHERE CREATED_AT IS NULL;
UPDATE FOLLOWS SET CREATED_AT = CURRENT_TIMESTAMP WHERE CREATED_AT IS NULL;
```

孤児データが見つかった場合は機械的に削除せず、対象行を確認して、正しい親IDへの修正または不要行の削除を判断します。

### 2. H2からCSVを出力する

先にリポジトリ直下へ `migration-export` ディレクトリを作り、H2コンソールで以下を実行します。`C:/absolute/path/...` は実際の絶対パスへ置き換えてください。

```sql
CALL CSVWRITE('C:/absolute/path/migration-export/users.csv',
  'SELECT ID, NAME, PASS, BIO FROM USERS ORDER BY ID');
CALL CSVWRITE('C:/absolute/path/migration-export/mutters.csv',
  'SELECT ID, USER_ID, TEXT, VERSION, CREATED_AT FROM MUTTERS ORDER BY ID');
CALL CSVWRITE('C:/absolute/path/migration-export/mutter_likes.csv',
  'SELECT ID, MUTTER_ID, USER_ID, CREATED_AT FROM MUTTER_LIKES ORDER BY ID');
CALL CSVWRITE('C:/absolute/path/migration-export/follows.csv',
  'SELECT ID, FOLLOWER_ID, FOLLOWEE_ID, CREATED_AT FROM FOLLOWS ORDER BY ID');
```

`migration-export/` は `.gitignore` に登録済みです。移行用CSVにはユーザー情報やパスワードハッシュが含まれるため、GitHubへ登録しないでください。

Windows環境では、H2のCSV出力や `psql` のクライアント文字コードがShift_JIS系になることがあります。PostgreSQLへ取り込む前に、CSVをUTF-8へ変換し、`psql` 側でも次を指定してから `\copy` すると安定します。

```sql
\encoding UTF8
```

また、CSVフィールド内に改行を含む自己紹介文などがある場合は、単純な行単位変換ではCSV構造が崩れることがあります。CSVとして読み取り、CSVとしてUTF-8再出力する方法で変換してください。

### 3. PostgreSQLへ初期スキーマを作る

空のデータベースに対して次を実行します。

```sh
psql -U dokotsubu_app -d dokotsubu -h localhost -f src/main/resources/db/migration/V1__initial_postgresql_schema.sql
```

### 4. 親テーブルから順番にCSVを取り込む

`psql` に接続し、ファイルパスを置き換えて実行します。

```sql
\encoding UTF8
\copy users (id, name, pass, bio) FROM 'migration-export/users.csv' WITH (FORMAT csv, HEADER true)
\copy mutters (id, user_id, text, version, created_at) FROM 'migration-export/mutters.csv' WITH (FORMAT csv, HEADER true)
\copy mutter_likes (id, mutter_id, user_id, created_at) FROM 'migration-export/mutter_likes.csv' WITH (FORMAT csv, HEADER true)
\copy follows (id, follower_id, followee_id, created_at) FROM 'migration-export/follows.csv' WITH (FORMAT csv, HEADER true)
```

`users → mutters → mutter_likes/follows` の順にすることで、外部キー制約を無効化せず移行できます。制約違反で停止した場合はH2側の不整合を修正してからやり直します。

`mutters.csv` に `VERSION` 列が出力されていない場合は、取り込み列から `version` を外し、PostgreSQL側の既定値 `0` を使います。

```sql
\copy mutters (id, user_id, text, created_at) FROM 'migration-export/mutters.csv' WITH (FORMAT csv, HEADER true)
```

### 5. 自動採番と件数を検証する

CSVではIDを明示的に取り込むため、最後に次を実行してPostgreSQLの採番位置を調整し、件数を確認します。

```sh
psql -U dokotsubu_app -d dokotsubu -h localhost -f db/migration/postgresql_post_import.sql
```

移行直前にH2で再集計した件数を正として照合してください。

---

## 8. 本番環境・デプロイ

現在の公開環境は、ローカルの H2 ベース構成から、PostgreSQL ベースのデプロイ可能な構成へ移行済みです。

| 項目 | 現在の構成 |
|---|---|
| アプリのホスティング | Render Web Service |
| 実行環境 | Docker + Tomcat 11 |
| ビルド成果物 | `target/dokoTsubu.war` |
| データベース | Supabase PostgreSQL |
| DB接続情報 | Render の環境変数 |
| 公開パス | `/dokoTsubu/` |

Maven ビルド時に `frontend-maven-plugin` が React フロントエンドもビルドし、WAR に同梱します。

### Docker

`Dockerfile` はマルチステージビルドで、Maven で WAR をビルドしたのち、Tomcat 11 イメージへ配置します（Mavenまで入れたまま公開するとDockerイメージが巨大になるため）。

mvn clean package の実行→target/dokoTsubu.war 生成→Tomcatの webapps にコピーを自動化したものです。

```dockerfile
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY frontend ./frontend
COPY src ./src
COPY db ./db
RUN mvn clean package -DskipTests

FROM tomcat:11.0-jdk21-temurin
ENV PORT=8080
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/dokoTsubu.war /usr/local/tomcat/webapps/dokoTsubu.war
COPY docker/start-tomcat.sh /usr/local/bin/start-tomcat.sh
RUN chmod +x /usr/local/bin/start-tomcat.sh
EXPOSE 8080
CMD ["/usr/local/bin/start-tomcat.sh"]
```

`docker/start-tomcat.sh` は、起動時に以下を行います。

- Tomcat の shutdown ポートを無効化（`8005` → `-1`）し、Render からのヘルスチェック等による `Invalid shutdown command` 警告の反復を防ぐ
- Tomcat の HTTP コネクタのポートを、Render が指定する環境変数 `PORT` へ書き換える

### Render設定

Render は Docker Web Service として設定します。

必要な環境変数:

```text
DB_URL=jdbc:postgresql://aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require
DB_USER=postgres.<supabase-project-ref>
DB_PASSWORD=<your-supabase-db-password>
# 任意。未指定時は10
DB_MAXIMUM_POOL_SIZE=10
```

実際のデータベースパスワード、エクスポート済み CSV、実ユーザーを含む SQL ダンプ、`.env` ファイルはコミットしないでください。

Render の無料プランでは、一定時間アクセスが無いとサービスがスピンダウンします。スピンダウン後の最初のリクエストは数十秒かかることがありますが、これは無料プランの想定内の挙動であり、アプリの不具合ではありません。

### Supabase接続に関する注意

IPv4 のみの環境から `db.<project-ref>.supabase.co` への直接接続が失敗する場合は、Supabase のプーラー接続を使用します。

現在の構成では、JDBC URL に SSL 指定を含めます。

```text
?sslmode=require
```

Supabase のプロジェクト参照名は、プロジェクト URL のサブドメイン部分です。

```text
https://<project-ref>.supabase.co
```

### 公開前チェックリスト

他の人にアプリを共有する前に、以下を確認します。

- `mvn test` がローカルで通ること
- Render のデプロイステータスが `Live` であること
- `/dokoTsubu/` でアプリが開くこと
- 想定しているデモユーザーでログインできること
- 投稿・検索・いいね・フォロー・編集・削除が動作すること
- 実際の秘密情報が GitHub にコミットされていないこと
- 公開時、Supabase にはサンプル・デモデータのみが入っていること

---

## 9. REST API

### 主な画面 URL

| URL | 説明 |
|---|---|
| `/` | ログイン画面 |
| `/Register` | ユーザー登録 |
| `/Main` | メイン画面 |

### API一覧

| メソッド | URL | 説明 |
|---|---|---|
| `GET` | `/api/session` | ログインユーザー情報と CSRF トークン取得 |
| `GET` | `/api/mutters` | 一覧取得、検索、ページング |
| `POST` | `/api/mutters` | 新規投稿 |
| `GET` | `/api/mutters/{id}` | 1 件取得 |
| `PUT` | `/api/mutters/{id}` | 更新 |
| `DELETE` | `/api/mutters/{id}` | 削除 |
| `POST` | `/LikeMutter` | いいね / 解除 |
| `POST` | `/FollowUser` | フォロー / 解除 |
| `POST` | `/Login` | ログイン |
| `GET` | `/Logout` | ログアウト |

一覧 API の各投稿には、本文だけでなく以下も含めています。

- `likeCount`
- `likedByMe`
- `followedByMe`
- `version`
- `createdAt`

これにより React 側で、ボタン状態や編集時の競合制御を扱えるようにしています。

### いいね・フォローの現在の挙動

`like` と `follow` のトグル後は、「処理成功かどうか」ではなく「実際の最新状態」をサーバーが返します。

- `LikeMutter` は `liked` の最新状態を返す
- `FollowUser` は `following` の最新状態を返す
- フロントはその返却値を使ってボタン表示を更新する

これにより、次のような不整合は解消済みです。

- いいね解除したのに `♥` のままになる
- フォロー解除したのに「フォローしました」と表示される
- フォロー解除後もボタンが「フォロー解除」のまま残る

### Reactとの通信 / CSRF

状態変更系リクエストには CSRF 対策を入れています。

流れは次の通りです。

1. `GET /api/session` で CSRF トークンを取得
2. `frontend/src/api.js` がトークンを保持
3. `POST` / `PUT` / `DELETE` 時に `X-CSRF-Token` を付与
4. `CsrfProtectionFilter` で検証

JSP 側の従来フォーム送信では hidden の `_csrf` も利用しています。

### 更新競合チェック

つぶやき編集は楽観ロック方式です。
`MUTTERS.VERSION` を使って、他の更新が先に入った場合は競合として弾きます。

更新 SQL の考え方は次の通りです。

```sql
UPDATE MUTTERS
SET TEXT = ?, VERSION = VERSION + 1
WHERE ID = ? AND USER_ID = ? AND VERSION = ?
```

---

## 10. React実装

### 画面構成

React 側では以下を実装済みです。

- 初期表示時に `/api/session` と `/api/mutters` を取得
- 投稿フォーム
- 検索フォーム
- 一覧表示
- 追加読み込み
- 5 秒ごとの自動更新
- 編集ダイアログ
- 削除確認
- いいね状態の即時反映
- フォロー状態の即時反映

### 自動更新（loading / refreshing）

メイン画面は一定間隔でつぶやき一覧を自動更新します。ボタンのちらつきを避けるため、状態を次のように分離しています。

- 初回・手動読み込み: `loading`
- バックグラウンドの自動更新: `refreshing`

投稿・検索・手動更新ボタンは、バックグラウンドの自動更新中であることを理由に非活性にはなりません。

---

## 11. テスト

```sh
mvn test
```

現在のテスト対象には次が含まれます。

- `MutterInputValidatorTest`
- `CsrfProtectionFilterTest`
- `FollowUserLogicTest`
- `LikeMutterLogicTest`
- `frontend/src/components/__tests__/MutterList.test.jsx`

`mvn test` で Java 側テスト（JUnit 5 / Mockito）と React 側テスト（Vitest）の両方をまとめて実行します。

---

## 12. 今後の改善予定

段階的なモダナイゼーションを計画しています。詳細は [13. 変更履歴](#13-変更履歴)内の「今後の課題」も参照してください。

- 特性テストの範囲拡大（JSP/フォーム経由の旧Servlet、`model/*Logic`の単体テスト）
- CSRF保護対象URLの見直し（`/LikeMutter`, `/FollowUser`を含める）
- Repositoryインターフェース導入によるレイヤー境界の整理（Branch by Abstraction）
- Flywayによるスキーマのコード管理化
- Spring Data JDBCへの置き換え、Spring Boot化
- LIKE検索の`%`/`_`エスケープ対応
- Spring Securityによる認証/CSRFの置き換え
- 旧UI（JSP + vanilla JS）のReact統合と旧コードの撤去
- CI/CDパイプラインの整備

---

## 13. 変更履歴

このプロジェクトは、JSP / Servlet ベースの構成を土台にしながら、React + REST API への分離や責務整理を学べるようにしています。既存の挙動を100%維持したまま、クリーンアーキテクチャ化・テスト整備を段階的に進めています。各変更は「なぜそうしたか」が分かるよう、コード側にもコメントを残しています。

### Phase 0: 安全網構築（DB接続の一本化 + MutterDAOの特性テスト）

- **変更日**: 2026-07-07
- **変更理由**: リファクタリングを安全に進めるための土台として、(1) DB接続情報が`DBUtil`と`MutterDAO`の2箇所に重複していた問題を解消し、(2) 現状のDAOの挙動を固定する特性テスト（Characterization Test）を追加した。DAO層はこれまでテストが1本も無く、リファクタリングの安全網が無い状態だったため。
- **変更箇所**:
  - `pom.xml`: 特性テストで埋め込みH2（`jdbc:h2:mem:...`）を使うため、H2をtestスコープの依存として追加（本番で使用しているバージョン2.4.240に合わせた）。
  - `src/main/java/util/DBUtil.java`: 接続先(URL/USER/PASSWORD)をシステムプロパティで上書き可能にした。プロパティ未設定時は従来と全く同じ値になるため、本番の挙動は変わらない。
  - `src/main/java/dao/MutterDAO.java`: 独自に持っていた接続先情報と`Class.forName`呼び出しを削除し、`DBUtil.getConnection()`に一本化。SQLやロジックには一切手を加えていない。
  - `src/test/java/support/TestDatabaseSupport.java`（新規）: テスト用の埋め込みH2にスキーマを用意するヘルパー。`MUTTERS`テーブルの作成スクリプトが既存コードのどこにも見当たらなかったため、`MutterDAO`/`MutterInputValidator`の使われ方から逆算して定義した。実際の本番DB定義と差異がないか、今後のFlywayベースライン化の際に必ず確認が必要（未検証のリスクとして記録）。
  - `src/test/java/dao/MutterDAOCharacterizationTest.java`（新規）: `findLatest`/`findByCursor`/`findPage`/`findById`/`createAndReturn`/`create`/`update`(楽観ロック競合・他人の投稿への更新拒否を含む)/`delete`/`search`の現状の挙動をテストとして固定。キーワード検索が`%`や`_`をエスケープしていない点、`MUTTERS.USER_ID`に外部キー制約が無く存在しないユーザーIDでもINSERTが成立してしまう点など、既知の問題点はあえて「今の挙動」として記録し、修正はしていない。
- **潜在的リスクと対応**:
  - 推測した`MUTTERS`スキーマが本番と異なる可能性がある → Flywayによるスキーマのコード管理化の際に実DBの定義と突き合わせて確認する。
  - 埋め込みH2と本番のH2（TCPサーバーモード）でSQL方言の細かな差異が出る可能性がある → バージョンを本番と同じ2.4.240に固定することで最小化している。

### Phase 0続き: UserDAO / LikeDAO / FollowDAO の特性テスト

- **変更日**: 2026-07-07
- **変更理由**: `UserDAO`/`LikeDAO`/`FollowDAO`は元々`DBUtil`を利用しており接続情報の重複は無かったため、本番コードは変更せず、現状の挙動を固定する特性テストのみを追加した。DAO層全体のテストカバレッジを揃えることが目的。
- **変更箇所**:
  - `src/test/java/dao/UserDAOCharacterizationTest.java`（新規）: `findById`/`findByName`/`create`(NAME重複時にfalseを返す挙動、BIOが`null`のとき`""`に正規化される挙動を含む)/`updateBio`の現状の挙動を固定。
  - `src/test/java/dao/LikeDAOCharacterizationTest.java`（新規）: `addLike`/`removeLike`/`hasLiked`/`countLikes`/`toggleLike`の現状の挙動を固定。`MUTTER_ID`+`USER_ID`のUNIQUE制約により二重いいねが失敗する挙動も含む。
  - `src/test/java/dao/FollowDAOCharacterizationTest.java`（新規）: `follow`/`unfollow`/`isFollowing`/`toggleFollow`/`countFollowers`/`countFollowing`/`findFollowingUsers`/`findFollowerUsers`(NAME昇順ソート)の現状の挙動を固定。
- **確認結果**: `dao`パッケージの特性テスト計33件、および既存のJavaテスト（`FollowUserLogicTest`, `LikeMutterLogicTest`, `CsrfProtectionFilterTest`, `MutterInputValidatorTest`）を含む全45件が回帰なく成功。
- **潜在的リスクと対応**:
  - `LikeDAO`/`FollowDAO`の一意制約違反時、DAO内部で`SQLException`を`e.printStackTrace()`してから`false`を返しており、テスト実行時にスタックトレースがコンソールへ出力される（テスト失敗ではなく想定内の出力）。将来的にロギングを整備する際の対象として記録。
  - `MUTTER_LIKES.MUTTER_ID`/`FOLLOWS.FOLLOWER_ID`等にも外部キー制約が無く、存在しないIDに対しても操作できてしまう可能性がある点は`MUTTERS.USER_ID`と同様の設計上の課題として記録。

### Phase 0続き: MutterApiServlet / SessionApiServlet の特性テスト

- **変更日**: 2026-07-07
- **変更理由**: `/api/mutters`一覧取得はN+1クエリ（1件ごとに`likeCount`/`likedByMe`/`followedByMe`を追加クエリで問い合わせる）を将来最適化する予定の最重要箇所。最適化の前にレスポンス形状・ステータスコード・エラーコードを固定するテストを用意した。本番コードは変更していない。
- **変更箇所**:
  - `src/test/java/support/ServletTestSupport.java`（新規）: Servletの特性テストで繰り返し必要になる、ログイン中ユーザー付きリクエストのモック生成、JSONリクエストボディの差し込み、レスポンスに書き込まれるJSON文字列のキャプチャをまとめた共通ヘルパー。
  - `src/test/java/api/SessionApiServletCharacterizationTest.java`（新規）: 未ログイン時の401、ログイン時のユーザー情報+CSRFトークン返却を固定（2件）。
  - `src/test/java/api/MutterApiServletCharacterizationTest.java`（新規）: `doGet`(単体取得の成功/404/IDの形式違いによる404と400の使い分け、一覧のカーソル・キーワード・limitのバリデーション)/`doPost`(401/405/バリデーション/201+Locationヘッダー)/`doPut`(バリデーション/404/403/楽観ロック競合409/200)/`doDelete`(404/403/204)の現状の挙動を固定（21件）。
- **確認結果**: 新規23件を含む全68件のJavaテストが回帰なく成功。
- **潜在的リスクと対応**:
  - `doGet`の単体取得は、パスが数字でない場合は404「RESOURCE_NOT_FOUND」、`0`や負の数の場合は400「INVALID_ID」という、一見直感に反する使い分けになっている。既存の挙動としてテストで明示的に固定した（API仕様として維持するか見直すかの判断材料にする）。
  - N+1クエリ自体はこの段階ではまだ修正していない。今後のレスポンス形状の回帰検知にこのテストを使う想定。

### React導入・いいね/フォロー機能・PostgreSQL移行・Renderデプロイ

- ログイン後のメイン画面をReact化し、REST API経由でのやり取りに段階的に移行
- いいね・フォロー機能を追加
- H2からPostgreSQL（Supabase）への移行手順を整備し、外部キー・NOT NULL制約を含む初期スキーマを導入
- DB接続時にJDBCドライバを明示ロードするよう修正（Tomcat上での自動検出失敗に対応）
- Renderデプロイ用のDocker設定を追加し、Tomcatのshutdownポートを無効化
- 自動更新時に投稿・検索・更新ボタンが不必要に非活性化される問題を修正（`loading`/`refreshing`の分離）

### 今後の課題（未着手・次回以降のタスク）

以下は、モダナイゼーション計画で洗い出した項目のうち、まだ着手していないもの。優先順に記載する。

**1. 特性テストの範囲拡大（Phase0の残り）**
- JSP/フォーム経由の旧Servlet（`Main`, `Login`, `Register`, `Profile`, `UpdateMutter`, `DeleteMutter`, `SearchMutter`, `LikeMutter`, `FollowUser`, `FollowerList`, `FollowingList`, `Logout`）にはまだ特性テストが無い。特にJSON POSTを受ける`LikeMutter`/`FollowUser`は次のCSRF修正の前提として先に固めておきたい。
- `model/*Logic`クラス（`GetMutterListLogic`, `PostMutterLogic`等）自体の単体テストも未整備（現状はDAO/Servlet経由の間接的な検証のみ）。

**2. セキュリティ修正（既知の脆弱性、未修正）**
- `/LikeMutter`, `/FollowUser`が`CsrfProtectionFilter`の対象URLに含まれておらず、CSRF保護が効いていない。修正時は上記の特性テスト整備後に着手する。

**3. Branch by Abstraction（未着手）**
- `Repository`インターフェースの導入（`MutterRepository`, `UserRepository`, `LikeRepository`, `FollowRepository`）。既存DAOをラップし、レイヤー境界だけ先に導入する。
- `model/*Logic`のパススルークラスを意味のあるユースケース単位（`PostMutterUseCase`等）に再編。

**4. 基盤の入れ替え（進行中）**
- HikariCPによるコネクションプールを導入済み。最大プールサイズは`DB_MAXIMUM_POOL_SIZE`（既定値10）で調整できる。
- Flywayによるスキーマのコード管理化。特に`MUTTERS`テーブルの実際の定義を本番DBで確認し、`TestDatabaseSupport`で推測したスキーマとの差異を解消する。
- 各DAOの`ensureSchema()`（毎リクエストでの`CREATE TABLE IF NOT EXISTS`実行）を廃止。

**5. 永続層・APIエンドポイントの移行（進行中）**
- DAOをSpring Data JDBC実装へ置き換え。
- `MutterApiServlet`と旧`MutterList`の一覧取得は、いいね数・いいね済み・フォロー済みを含む1 SQLへ統合済み。
- `MutterDAO.findPage`/`search`のLIKE検索がキーワード中の`%`/`_`をエスケープしていない問題への対応要否を判断。
- `MUTTERS.USER_ID`等に外部キー制約が無い問題への対応要否を判断（PostgreSQL側は対応済み、H2側は未対応）。

**6. 認証/CSRFのSpring Security移行（未着手）**

**7. 旧UI（JSP + vanilla JS）のReact統合、旧コードの撤去（未着手）**
- `Profile`/`FollowerList`/`FollowingList`のReact化と、対応するJSP・`webapp/js/*.js`の削除。

**8. その他の細かい未修正項目**
- DAOのエラーハンドリングの不統一（`e.printStackTrace()`で握りつぶすものと`RuntimeException`を投げるものが混在）。
- `Mutter`/`User`のテレスコーピングコンストラクタの整理。
- Mockitoの「self-attaching」警告（将来のJDKで動作しなくなる可能性）への対応（ビルド設定でjavaagentとして明示的に追加する）。

---

## 補足

このプロジェクトは、JSP / Servlet ベースの構成を土台にしながら、React + REST API への分離や責務整理を学べるようにしています。
画面、API、Logic、DAO、DB の流れを追いやすい構成なので、段階的なリファクタリングや機能追加の教材としても使いやすい状態です。
