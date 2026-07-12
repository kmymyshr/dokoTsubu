# dokoTsubu

Java Servlet / JSP と React を組み合わせた、ひとこと投稿アプリです。  
ログイン・投稿・検索・編集・削除に加えて、いいね、フォロー、REST API、CSRF 対策までを一通り含んだ学習用アプリとして構成しています。

現在は、JSP ベースの画面を残しつつ、メイン画面は React から REST API を呼び出す構成に移行しています。

## 主な機能

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
- 更新競合チェック

## 技術スタック

| 分類 | 内容 |
|---|---|
| 言語 | Java 17 |
| Web | Jakarta Servlet 6.0 / JSP |
| フロントエンド | React 19 / Vite 8 |
| ビルド | Maven / frontend-maven-plugin |
| DB | PostgreSQL（公開環境）/ H2（移行元・テスト） |
| JSON | Jackson |
| パスワード | jBCrypt |
| テスト | JUnit 5 / Mockito / Vitest |

## アーキテクチャ

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
    PostgreSQL
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

## ディレクトリ構成

```text
dokoTsubu/
├─ db/
│  └─ migration/
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
├─ src/main/webapp/
│  ├─ index.jsp
│  └─ WEB-INF/jsp/
├─ src/test/java/
├─ .gitignore
├─ pom.xml
└─ README.md
```

## 画面と API

### 主な画面 URL

| URL | 説明 |
|---|---|
| `/` | ログイン画面 |
| `/Register` | ユーザー登録 |
| `/Main` | メイン画面 |

### 主な API / サーブレット

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

## React メイン画面の実装状況

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

一覧 API の各投稿には、本文だけでなく以下も含めています。

- `likeCount`
- `likedByMe`
- `followedByMe`
- `version`
- `createdAt`

これにより React 側で、ボタン状態や編集時の競合制御を扱えるようにしています。

## いいね・フォローの現在の挙動

現在は、`like` と `follow` のトグル後に「処理成功かどうか」ではなく、「実際の最新状態」をサーバーが返すように修正済みです。

- `LikeMutter` は `liked` の最新状態を返す
- `FollowUser` は `following` の最新状態を返す
- フロントはその返却値を使ってボタン表示を更新する

これにより、次のような不整合は解消済みです。

- いいね解除したのに `♥` のままになる
- フォロー解除したのに「フォローしました」と表示される
- フォロー解除後もボタンが「フォロー解除」のまま残る

## CSRF 対策

状態変更系リクエストには CSRF 対策を入れています。

流れは次の通りです。

1. `GET /api/session` で CSRF トークンを取得
2. `frontend/src/api.js` がトークンを保持
3. `POST` / `PUT` / `DELETE` 時に `X-CSRF-Token` を付与
4. `CsrfProtectionFilter` で検証

JSP 側の従来フォーム送信では hidden の `_csrf` も利用しています。

## 更新競合チェック

つぶやき編集は楽観ロック方式です。  
`MUTTERS.VERSION` を使って、他の更新が先に入った場合は競合として弾きます。

更新 SQL の考え方は次の通りです。

```sql
UPDATE MUTTERS
SET TEXT = ?, VERSION = VERSION + 1
WHERE ID = ? AND USER_ID = ? AND VERSION = ?
```

## DB テーブル

主なテーブルは以下です。

- `USERS`
- `MUTTERS`
- `MUTTER_LIKES`
- `FOLLOWS`

`MUTTER_LIKES` と `FOLLOWS` は一意制約で重複登録を防いでいます。

### PostgreSQL移行時の堅牢化

公開環境では、アプリの入力チェックだけに依存せず、データベースでも参照整合性を保証します。

- `MUTTERS.USER_ID` は存在する `USERS.ID` のみ参照可能
- `MUTTER_LIKES` は存在する投稿・ユーザーのみ参照可能
- `FOLLOWS` は存在するフォロワー・フォロー対象のみ参照可能
- いいね・フォローの組み合わせは複合UNIQUE制約で重複不可
- 全テーブルの `CREATED_AT` を `NOT NULL` とし、作成日時の欠落を防止
- 投稿削除時は関連するいいねを `ON DELETE CASCADE` で削除
- ユーザー削除時は関連するフォロー関係を `ON DELETE CASCADE` で削除

外部キーは、バグや管理操作によって「存在しないユーザーの投稿」のような孤児データが作られることを最後の砦として防ぎます。`NOT NULL` は日時を前提とする並び替えや監査処理を単純かつ確実にします。制約付きの初期スキーマは `src/main/resources/db/migration/V1__initial_postgresql_schema.sql` に置いています。

DAOがリクエストごとにDDLを実行する方式は廃止しました。スキーマ変更をバージョン管理されたSQLへ集約することで、ローカル・検証・公開環境で同じ定義を再現できます。このファイル名はFlywayのバージョン付きマイグレーション規則にも対応しています。

## H2からPostgreSQLへのデータ移行

GitHubはソースコードの保管・共有先であり、JavaアプリやPostgreSQLを常時実行するデプロイ先ではありません。現時点ではデプロイ先に依存しないSQLと接続設定を用意し、実行環境が決まったら同じ手順を適用します。DBパスワードやCSVはGitHubへ登録しないでください。

### 1. H2データを検査する

H2コンソールで `db/migration/h2_pre_migration_checks.sql` を実行します。全ての `INVALID_COUNT` が `0` であることを確認してください。

NULLの作成日時だけが見つかった場合は、日時が不明になることを了承したうえで次のように補完できます。

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

移行元で確認した目安は `USERS=32`、`MUTTERS=52`、`MUTTER_LIKES=28`、`FOLLOWS=16` です。`MUTTERS=52` は、外部キー追加前の検査で見つかった孤児投稿3件を削除した後の件数です。移行直前にH2で再集計した件数を正として照合してください。

## セットアップ

前提:

- JDK 17
- Maven
- Tomcat 10.1 / 11 系など Jakarta Servlet 対応コンテナ
- PostgreSQL（公開環境）またはH2 Databaseサーバー（従来のローカル環境）

H2 接続設定:

```text
jdbc:h2:tcp://localhost/~/dokoTsubu
user: sa
password: なし
```

PostgreSQL接続設定は、公開環境のSecretまたはローカルの環境変数へ設定します。

```text
DB_URL=jdbc:postgresql://localhost:5432/dokotsubu
DB_USER=dokotsubu_app
DB_PASSWORD=推測困難なパスワード
```

Javaのシステムプロパティ `db.url` / `db.user` / `db.password` はテスト用途として引き続き環境変数より優先されます。未設定時のみ従来のローカルH2へ接続します。

PostgreSQL接続時は、Tomcat上でJDBCドライバの自動検出が効かない場合に備えて、`DBUtil` がURLに応じてドライバを明示ロードします。

- `jdbc:postgresql:` → `org.postgresql.Driver`
- `jdbc:h2:` → `org.h2.Driver`

## ビルド

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

### ローカルTomcat 11での起動例

Pleiades同梱Tomcat 11を使う場合の例です。環境に合わせてパスは読み替えてください。

```powershell
$env:JAVA_HOME="C:\pleiades\2025-12\java\21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

$env:DB_URL="jdbc:postgresql://localhost:5432/dokotsubu"
$env:DB_USER="dokotsubu_app"
$env:DB_PASSWORD="your_password"
```

WARをビルドします。

```powershell
mvn clean package
```

Tomcatを停止したうえで、WARを `webapps` 直下へ配置します。展開済みフォルダが残っている場合は削除してから置き換えると確実です。

```powershell
Remove-Item "C:\pleiades\2025-12\tomcat\11\webapps\dokoTsubu" -Recurse -Force

Copy-Item `
  "C:\Users\kmymy\VScode\Workspace\dokoTsubu\target\dokoTsubu.war" `
  "C:\pleiades\2025-12\tomcat\11\webapps\dokoTsubu.war" `
  -Force
```

起動ログを同じPowerShellに表示したい場合は、`startup.bat` ではなく `catalina.bat run` を使います。環境変数も同じPowerShellから渡せるため、接続確認時に便利です。

```powershell
cd "C:\pleiades\2025-12\tomcat\11\bin"
.\catalina.bat run
```

## テスト

```sh
mvn test
```

現在のテスト対象には次が含まれます。

- `MutterInputValidatorTest`
- `CsrfProtectionFilterTest`
- `FollowUserLogicTest`
- `LikeMutterLogicTest`
- `frontend/src/components/__tests__/MutterList.test.jsx`

`mvn test` で Java 側テストと React 側 Vitest の両方をまとめて実行します。

## 補足

このプロジェクトは、JSP / Servlet ベースの構成を土台にしながら、React + REST API への分離や責務整理を学べるようにしています。  
画面、API、Logic、DAO、DB の流れを追いやすい構成なので、段階的なリファクタリングや機能追加の教材としても使いやすい状態です。

## 変更履歴（モダナイゼーション対応）

既存の挙動を100%維持したまま、クリーンアーキテクチャ化・テスト整備を段階的に進めています。各変更は「なぜそうしたか」が分かるよう、コード側にもコメントを残しています。

### Phase 0: 安全網構築（DB接続の一本化 + MutterDAOの特性テスト）

- **変更日**: 2026-07-07
- **変更理由**: リファクタリングを安全に進めるための土台として、(1) DB接続情報が`DBUtil`と`MutterDAO`の2箇所に重複していた問題を解消し、(2) 現状のDAOの挙動を固定する特性テスト（Characterization Test）を追加した。DAO層はこれまでテストが1本も無く、リファクタリングの安全網が無い状態だったため。
- **変更箇所**:
  - `pom.xml`: 特性テストで埋め込みH2（`jdbc:h2:mem:...`）を使うため、H2をtestスコープの依存として追加（本番で使用しているバージョン2.4.240に合わせた）。
  - `src/main/java/util/DBUtil.java`: 接続先(URL/USER/PASSWORD)をシステムプロパティで上書き可能にした。プロパティ未設定時は従来と全く同じ値になるため、本番の挙動は変わらない。
  - `src/main/java/dao/MutterDAO.java`: 独自に持っていた接続先情報と`Class.forName`呼び出しを削除し、`DBUtil.getConnection()`に一本化。SQLやロジックには一切手を加えていない。
  - `src/test/java/support/TestDatabaseSupport.java`（新規）: テスト用の埋め込みH2にスキーマを用意するヘルパー。**`MUTTERS`テーブルの作成スクリプトが既存コードのどこにも見当たらなかった**ため、`MutterDAO`/`MutterInputValidator`の使われ方から逆算して定義した。実際の本番DB定義と差異がないか、今後のFlywayベースライン化の際に必ず確認が必要（未検証のリスクとして記録）。
  - `src/test/java/dao/MutterDAOCharacterizationTest.java`（新規）: `findLatest`/`findByCursor`/`findPage`/`findById`/`createAndReturn`/`create`/`update`(楽観ロック競合・他人の投稿への更新拒否を含む)/`delete`/`search`の現状の挙動をテストとして固定。キーワード検索が`%`や`_`をエスケープしていない点、`MUTTERS.USER_ID`に外部キー制約が無く存在しないユーザーIDでもINSERTが成立してしまう点など、既知の問題点はあえて「今の挙動」として記録し、修正はしていない。
- **潜在的リスクと対応**:
  - 推測した`MUTTERS`スキーマが本番と異なる可能性がある → Phase2（Flywayによるスキーマのコード管理化）の際に実DBの定義と突き合わせて確認する。
  - 埋め込みH2と本番のH2（TCPサーバーモード）でSQL方言の細かな差異が出る可能性がある → バージョンを本番と同じ2.4.240に固定することで最小化している。

### Phase 0続き: UserDAO / LikeDAO / FollowDAO の特性テスト

- **変更日**: 2026-07-07
- **変更理由**: `UserDAO`/`LikeDAO`/`FollowDAO`は元々`DBUtil`を利用しており接続情報の重複は無かったため、本番コードは変更せず、現状の挙動を固定する特性テストのみを追加した。DAO層全体のテストカバレッジをPhase0で揃えることが目的。
- **変更箇所**:
  - `src/test/java/dao/UserDAOCharacterizationTest.java`（新規）: `findById`/`findByName`/`create`(NAME重複時にfalseを返す挙動、BIOが`null`のとき`""`に正規化される挙動を含む)/`updateBio`の現状の挙動を固定。
  - `src/test/java/dao/LikeDAOCharacterizationTest.java`（新規）: `addLike`/`removeLike`/`hasLiked`/`countLikes`/`toggleLike`の現状の挙動を固定。`MUTTER_ID`+`USER_ID`のUNIQUE制約により二重いいねが失敗する挙動も含む。
  - `src/test/java/dao/FollowDAOCharacterizationTest.java`（新規）: `follow`/`unfollow`/`isFollowing`/`toggleFollow`/`countFollowers`/`countFollowing`/`findFollowingUsers`/`findFollowerUsers`(NAME昇順ソート)の現状の挙動を固定。
- **確認結果**: `dao`パッケージの特性テスト計33件、および既存のJavaテスト（`FollowUserLogicTest`, `LikeMutterLogicTest`, `CsrfProtectionFilterTest`, `MutterInputValidatorTest`）を含む全45件が回帰なく成功。
- **潜在的リスクと対応**:
  - `LikeDAO`/`FollowDAO`の一意制約違反時、DAO内部で`SQLException`を`e.printStackTrace()`してから`false`を返しており、テスト実行時にスタックトレースがコンソールへ出力される（テスト失敗ではなく想定内の出力）。将来的にロギングを整備する際の対象として記録。
  - `MUTTER_LIKES.MUTTER_ID`/`FOLLOWS.FOLLOWER_ID`等にも外部キー制約が無く、存在しないIDに対しても操作できてしまう可能性がある点は`MUTTERS.USER_ID`と同様の設計上の課題として記録（Phase3以降のドメイン層設計で検討）。

### Phase 0続き: MutterApiServlet / SessionApiServlet の特性テスト

- **変更日**: 2026-07-07
- **変更理由**: `/api/mutters`一覧取得はStep1分析で指摘したN+1クエリ（1件ごとに`likeCount`/`likedByMe`/`followedByMe`を追加クエリで問い合わせる）を将来最適化する予定の最重要箇所。最適化の前にレスポンス形状・ステータスコード・エラーコードを固定するテストを用意した。本番コードは変更していない。
- **変更箇所**:
  - `src/test/java/support/ServletTestSupport.java`（新規）: Servletの特性テストで繰り返し必要になる、ログイン中ユーザー付きリクエストのモック生成、JSONリクエストボディの差し込み、レスポンスに書き込まれるJSON文字列のキャプチャをまとめた共通ヘルパー。
  - `src/test/java/api/SessionApiServletCharacterizationTest.java`（新規）: 未ログイン時の401、ログイン時のユーザー情報+CSRFトークン返却を固定（2件）。
  - `src/test/java/api/MutterApiServletCharacterizationTest.java`（新規）: `doGet`(単体取得の成功/404/IDの形式違いによる404と400の使い分け、一覧のカーソル・キーワード・limitのバリデーション)/`doPost`(401/405/バリデーション/201+Locationヘッダー)/`doPut`(バリデーション/404/403/楽観ロック競合409/200)/`doDelete`(404/403/204)の現状の挙動を固定（21件）。
- **確認結果**: 新規23件を含む全68件のJavaテストが回帰なく成功。
- **潜在的リスクと対応**:
  - `doGet`の単体取得は、パスが数字でない場合は404「RESOURCE_NOT_FOUND」、`0`や負の数の場合は400「INVALID_ID」という、一見直感に反する使い分けになっている。これは既存の挙動としてテストで明示的に固定した（Phase4のAPI移行時に仕様として維持するか見直すかの判断材料にする）。
  - N+1クエリ自体はこの段階ではまだ修正していない。Phase4（Spring MVCへの移行、1クエリJOINへの最適化）で、このテストがレスポンス形状の回帰検知に使われる想定。

### 現状のまとめ（2026-07-07時点）

Phase0「安全網構築」の範囲で、DAO層4クラス（`MutterDAO`/`UserDAO`/`LikeDAO`/`FollowDAO`、計33件）とAPI層2クラス（`MutterApiServlet`/`SessionApiServlet`、計23件）の特性テストを整備した。既存テスト12件と合わせて**Javaテスト計68件**が全て成功しており、`mvn clean package`でのビルド（React部分含む）も成功することを確認済み。本番コードへの変更は「DB接続情報の重複解消」（`DBUtil`/`MutterDAO`、挙動は無変更）のみで、それ以外は全てテスト追加である。

### 今後の課題（未着手・次回以降のタスク）

以下は、ステップ2のモダナイゼーション計画で洗い出した項目のうち、今回のセッションではまだ着手していないもの。優先順に記載する。

**1. 特性テストの範囲拡大（Phase0の残り）**
- 今回はREST API（`MutterApiServlet`, `SessionApiServlet`）のみ特性テストを整備した。JSP/フォーム経由の旧Servlet（`Main`, `Login`, `Register`, `Profile`, `UpdateMutter`, `DeleteMutter`, `SearchMutter`, `LikeMutter`, `FollowUser`, `FollowerList`, `FollowingList`, `Logout`）にはまだ特性テストが無い。特にJSON POSTを受ける`LikeMutter`/`FollowUser`は次のCSRF修正の前提として先に固めておきたい。
- `model/*Logic`クラス（`GetMutterListLogic`, `PostMutterLogic`等）自体の単体テストも未整備（現状はDAO/Servlet経由の間接的な検証のみ）。

**2. セキュリティ修正（Step1で指摘した既知の脆弱性、未修正）**
- `/LikeMutter`, `/FollowUser`が`CsrfProtectionFilter`の対象URLに含まれておらず、CSRF保護が効いていない。修正時は上記の特性テスト整備後に着手する。

**3. Phase1: Branch by Abstraction（未着手）**
- `Repository`インターフェースの導入（`MutterRepository`, `UserRepository`, `LikeRepository`, `FollowRepository`）。既存DAOをラップし、レイヤー境界だけ先に導入する。
- `model/*Logic`のパススルークラスを意味のあるユースケース単位（`PostMutterUseCase`等）に再編。

**4. Phase2: 基盤の入れ替え（未着手）**
- HikariCPによるコネクションプール導入（現状は`DriverManager`で毎回新規接続）。
- Flywayによるスキーマのコード管理化。特に`MUTTERS`テーブルの実際の定義を本番DBで確認し、`TestDatabaseSupport`で推測したスキーマとの差異を解消する。
- 各DAOの`ensureSchema()`（毎リクエストでの`CREATE TABLE IF NOT EXISTS`実行）を廃止。

**5. Phase3〜4: 永続層・APIエンドポイントの移行（未着手）**
- DAOをSpring Data JDBC実装へ置き換え。
- `MutterApiServlet`一覧取得のN+1クエリ解消（1クエリJOIN化）。今回整備した特性テストで回帰を検知する。
- `MutterDAO.findPage`/`search`のLIKE検索がキーワード中の`%`/`_`をエスケープしていない問題への対応要否を判断。
- `MUTTERS.USER_ID`等に外部キー制約が無い問題への対応要否を判断。

**6. Phase5: 認証/CSRFのSpring Security移行（未着手）**

**7. Phase6: 旧UI（JSP + vanilla JS）のReact統合、Phase7: 旧コードの撤去（未着手）**
- `Profile`/`FollowerList`/`FollowingList`のReact化と、対応するJSP・`webapp/js/*.js`の削除。

**8. その他の細かい未修正項目**
- DAOのエラーハンドリングの不統一（`e.printStackTrace()`で握りつぶすものと`RuntimeException`を投げるものが混在）。
- `Mutter`/`User`のテレスコーピングコンストラクタの整理。
- Mockitoの「self-attaching」警告（将来のJDKで動作しなくなる可能性）への対応（ビルド設定でjavaagentとして明示的に追加する）。
