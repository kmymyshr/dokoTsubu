# どこつぶ（dokoTsubu）

Java ServletとJSPで作成した、簡易つぶやき投稿Webアプリです。

ユーザー登録・ログイン後、つぶやきの投稿、検索、編集、削除ができます。
データはH2 Databaseへ保存し、ログイン状態はHTTPセッションで管理します。

## 目次

- [主な機能](#主な機能)
- [アプリの特徴](#アプリの特徴)
- [使用技術](#使用技術)
- [アプリの基本構成](#アプリの基本構成)
- [ディレクトリ構成](#ディレクトリ構成)
- [画面とURL一覧](#画面とurl一覧)
- [主要ファイルの関連](#主要ファイルの関連)
- [ModelとDAO](#modelとdao)
- [スコープの使い分け](#スコープの使い分け)
- [データベースの準備](#データベースの準備)
- [ビルドと実行](#ビルドと実行)
- [初学者向け：コードを読む順番](#初学者向けコードを読む順番)
- [今後改善できる点](#今後改善できる点)

## 主な機能

- ユーザーの新規登録
- BCryptによるパスワードのハッシュ化
- ログイン・ログアウト
- つぶやきの一覧表示と投稿
- キーワードによるつぶやき検索
- 自分のつぶやきの編集・削除
- 編集時の楽観的ロック
- JavaScriptによる5秒ごとの一覧自動更新
- IDカーソル方式による20件ずつのページネーション

## アプリの特徴

初心者向けの学習用アプリですが、次のような実務でも意識される作りを取り入れています。

- **認可の二重化**：編集・削除ボタンは自分の投稿にだけ表示するだけでなく、DAOのSQL自体もユーザーIDを条件に含めています。画面表示を迂回してリクエストを直接送っても、他人の投稿は変更できません。
- **楽観的ロック**：つぶやき編集時に`VERSION`列を照合し、他の操作が先に更新していた場合は上書きを防ぎます。
- **セッション固定攻撃への対策**：ログイン成功時に`request.changeSessionId()`でセッションIDを再発行します。
- **内部モデルとAPI用DTOの分離**：`Mutter`（内部モデル）と`MutterResponse`（JSON専用）を分けることで、内部用フィールドを増やしても意図せずAPIへ公開されにくい構成にしています。
- **カーソル方式のページネーション**：オフセット方式ではなく、直前に表示したIDを基準に次の20件を取得する方式を採用し、一覧の途中に新しい投稿が増えても表示がずれにくいようにしています。
- **検索中は自動更新を止める**：一覧の5秒ごと自動更新は、検索結果表示中や過去ページ読み込み後には停止し、表示中の内容が意図せず最新一覧へ戻らないようにしています。

## 使用技術

| 分類 | 技術 |
|---|---|
| 言語 | Java 17 |
| Web | Jakarta Servlet 6.0 / JSP / JSTL |
| DB | H2 Database 2.4.240 |
| DB接続 | JDBC |
| パスワード | jBCrypt 0.4 |
| JSON | Jackson 2.21.3 |
| ビルド | Maven |
| 実行環境 | Tomcat 10.1系など、Servlet 6.0対応コンテナ |
| フロントエンド | HTML / JavaScript |

## アプリの基本構成

このアプリは、MVCを意識して次のように役割を分けています。

```text
ブラウザ
  │ リクエスト
  ▼
Servlet（Controller）
  │ Logicを呼ぶ
  ▼
Logic（Modelの処理）
  │ DAOを呼ぶ
  ▼
DAO ── JDBC ── H2 Database
  │
  └─ 結果をServletへ返す
         │
         ▼
       JSP（View）── HTMLをブラウザへ返す
```

- **Controller（`servlet`）**：URLを受け取り、入力確認や画面遷移を行う
- **Logic（`model`の`～Logic`）**：アプリの処理をDAOへつなぐ
- **DAO（`dao`）**：SQLを実行し、DBを読み書きする
- **Model（`User`、`Mutter`）**：ユーザーやつぶやきのデータを保持する
- **View（JSP）**：Servletから受け取ったデータをHTMLとして表示する

## ディレクトリ構成

```text
firstSampleRepository/
├─ pom.xml                         Maven設定
├─ README.md                       この説明書
└─ src/main/
   ├─ java/
   │  ├─ servlet/                  Controller
   │  ├─ dto/                      JSONレスポンス専用DTO
   │  ├─ model/                    データクラスとLogic
   │  ├─ dao/                      DBアクセス
   │  └─ util/                     DB・パスワード関連の共通処理
   └─ webapp/
      ├─ index.jsp                 ログイン画面
      └─ WEB-INF/
         ├─ jsp/                   各画面のJSP
         └─ lib/                   実行時に利用するライブラリ
```

`WEB-INF/jsp`内のJSPはブラウザから直接開けません。
Servletからフォワードすることで表示します。これにより、必要な処理を通さずに画面だけ開かれることを防げます。

## 画面とURL一覧

先に全体のURLを把握しておくと、後述の関連図が読みやすくなります。

| URL | HTTP | 担当 | 内容 |
|---|---|---|---|
| `/` | GET | `index.jsp` | ログイン画面 |
| `/Login` | POST | `Login` | ログイン処理 |
| `/Logout` | GET | `Logout` | ログアウト処理 |
| `/Register` | GET / POST | `Register` | 登録画面・登録処理 |
| `/Main` | GET | `Main` | 一覧表示 |
| `/Main` | POST | `Main` | つぶやき投稿 |
| `/SearchMutter` | GET | `SearchMutter` | キーワード検索 |
| `/UpdateMutter` | GET / POST | `UpdateMutter` | 編集画面・更新処理 |
| `/DeleteMutter` | POST | `DeleteMutter` | 削除処理 |
| `/MutterList` | GET | `MutterList` | カーソルページネーション用JSON取得 |

表中のURLには、実行時にコンテキストパス`/dokoTsubu`が付きます。

## 主要ファイルの関連

### ユーザー登録

```text
index.jsp
  └─ GET /Register
       └─ Register.java
            └─ registerView.jsp
                 └─ POST /Register
                      └─ RegisterUserLogic.java
                           └─ UserDAO.create()
                                └─ USERSテーブル
```

| ファイル | 役割 |
|---|---|
| `Register.java` | 登録画面の表示、入力値の確認、登録結果の画面遷移 |
| `RegisterUserLogic.java` | BCryptでパスワードをハッシュ化してDAOへ渡す |
| `UserDAO.java` | USERSテーブルへユーザーをINSERTする |
| `registerView.jsp` | ユーザー名とパスワードの入力画面 |
| `registerResult.jsp` | 登録結果の表示画面 |

### ログイン・ログアウト

```text
index.jsp
  └─ POST /Login
       └─ Login.java
            └─ LoginLogic.java
                 └─ UserDAO.findByName()
                      └─ BCryptでパスワード照合
                           └─ loginUserをセッションへ保存
```

| ファイル | 役割 |
|---|---|
| `Login.java` | 入力値を受け取り、ログイン成功時に`loginUser`をセッションへ保存 |
| `LoginLogic.java` | DB上のハッシュ済みパスワードと入力パスワードを照合 |
| `Logout.java` | セッションを破棄してログアウト |
| `loginResult.jsp` | ログイン結果を表示 |
| `logout.jsp` | ログアウト完了を表示 |

ログイン成功時には`request.changeSessionId()`を実行し、セッション固定攻撃への対策をしています。

### 一覧表示・投稿

```text
GET /Main
  └─ GetMutterListLogic
       └─ MutterDAO.findLatest(20)
            └─ mutterListをmain.jspへ渡す

POST /Main
  └─ PostMutterLogic
       └─ MutterDAO.create()
            └─ MUTTERSテーブルへINSERT
```

`Main.java`のGETは一覧表示、POSTは新しいつぶやきの投稿を担当します。
投稿後は`/Main`へリダイレクトするため、ブラウザを再読み込みしても同じ内容が再投稿されにくくなっています。

### 検索

```text
main.jsp
  └─ GET /SearchMutter?keyword=...
       └─ SearchMutter.java
            └─ SearchMutterLogic.java
                 └─ MutterDAO.search()
                      └─ main.jspへ検索結果を渡す
```

検索結果を表示している間は`searchMode`を設定し、JavaScriptの自動更新を停止します。
これにより、検索結果が5秒後に全件表示へ戻ってしまうことを防いでいます。

### 編集と楽観的ロック

```text
main.jspの編集ボタン
  └─ GET /UpdateMutter?mutterId=...
       └─ updateMutter.jsp
            └─ POST /UpdateMutter（ID・本文・VERSION）
                 └─ UpdateMutterLogic
                      └─ MutterDAO.update()
```

編集画面を開いた時点の`VERSION`をhidden項目に保存します。
更新時は、DB上のVERSIONが変わっていない場合だけUPDATEします。

```sql
UPDATE MUTTERS
SET TEXT = ?, VERSION = VERSION + 1
WHERE ID = ? AND USER_ID = ? AND VERSION = ?
```

別の操作が先に同じつぶやきを更新しているとVERSIONが一致しないため、更新件数は0件になります。
これが**楽観的ロック**です。後から更新した内容で、先の変更を誤って上書きすることを防ぎます。

### 削除

```text
main.jspの削除ボタン
  └─ POST /DeleteMutter
       └─ DeleteMutterLogic
            └─ MutterDAO.delete()
```

削除SQLは投稿IDだけでなく、ログイン中のユーザーIDも条件にしています。

```sql
DELETE FROM MUTTERS WHERE ID = ? AND USER_ID = ?
```

### 一覧の自動更新とカーソルページネーション

`main.jsp`は画面の枠だけを返し、外部ファイル`/js/main.js`が5秒ごとに最新20件を取得します。
「さらに読み込む」を押すと、最後に表示したIDをカーソルとして次の20件を取得します。

```text
main.js
  └─ GET /api/mutters?limit=20
       └─ MutterApiServlet
            └─ GetMutterListLogic
                 └─ MutterDAO.findPage(null, null, 21)
                      └─ MutterPageを返す
                           └─ MutterListResponseへ変換
                                └─ JacksonでJSONを出力

「さらに読み込む」
  └─ GET /api/mutters?cursor=最後のID&limit=20
       └─ MutterDAO.findPage(null, cursor, 21)
```

21件取得する理由は、画面に20件を表示し、残りの1件で次ページの有無を判定するためです。
APIは次の形式で返します。

```json
{
  "mutters": [],
  "nextCursor": 980,
  "hasNext": true
}
```

受け取ったJSONから一覧のHTMLを作ります。
検索画面では検索結果を維持するため、この定期実行を開始しません。
また、古いページを追加表示した後は、5秒更新で最新20件だけに戻らないよう自動更新を停止します。

JSONは文字列を手作業で組み立てず、Jacksonの`ObjectMapper`で生成します。

| クラス | 役割 |
|---|---|
| `MutterPage` | Logic層で使用するページ取得結果 |
| `MutterResponse` | つぶやき1件分のAPI専用DTO |
| `MutterListResponse` | 一覧・次カーソル・次ページ有無を持つAPI専用DTO |
| `ObjectMapperFactory` | `LocalDateTime`対応済みObjectMapperを提供 |

内部モデルとAPI用DTOを分けることで、`Mutter`へ内部用フィールドを追加しても、
意図せずJSONへ公開されることを防ぎやすくしています。

## JavaScript中心のメイン画面

`main.jsp`は見出し、フォーム、一覧の配置先、編集ダイアログという画面の枠だけを持ちます。
つぶやきのHTMLはJSPの`<c:forEach>`では生成せず、`/js/main.js`がREST APIのJSONから組み立てます。

```text
GET /Main
  └─ Main Servlet
       └─ main.jsp（画面の枠）
            └─ main.js
                 ├─ GET    /api/session
                 ├─ GET    /api/mutters
                 ├─ POST   /api/mutters
                 ├─ PUT    /api/mutters/{id}
                 └─ DELETE /api/mutters/{id}
```

一覧・投稿・検索・編集・削除はいずれもページ遷移せず、必要な部分だけを更新します。
本文の表示には`textContent`を使用し、投稿内容をHTMLとして解釈しないようにしています。
従来のServletとJSPは互換性と学習履歴のため残していますが、通常のメイン画面からは使用しません。
## REST API

ログイン済みセッションを使用して、つぶやきを`/api/mutters`リソースとして操作できます。
従来の`/UpdateMutter`、`/DeleteMutter`、`/MutterList`も互換用として残しています。`/Main`はJavaScript画面の入口になりました。

| HTTPメソッド | URL | 内容 |
|---|---|---|
| `GET` | `/api/session` | ログイン中ユーザーの公開情報を取得 |
| `GET` | `/api/mutters` | 一覧取得 |
| `GET` | `/api/mutters?keyword=Java&cursor=100&limit=20` | 検索・ページネーション |
| `POST` | `/api/mutters` | 新規作成 |
| `GET` | `/api/mutters/{id}` | 1件取得 |
| `PUT` | `/api/mutters/{id}` | 本文更新 |
| `DELETE` | `/api/mutters/{id}` | 削除 |

作成リクエストはJSONで送信します。

```json
{
  "text": "REST APIからの投稿"
}
```

作成成功時は`201 Created`を返し、`Location`ヘッダーに作成したリソースのURLを設定します。
更新では、取得時の`version`を一緒に送信します。

```json
{
  "text": "更新後の本文",
  "version": 0
}
```

別の操作で先に更新されていた場合は`409 Conflict`になります。
削除成功時は本文なしの`204 No Content`を返します。

エラーもHTMLではなく一定形式のJSONで返します。

```json
{
  "status": 404,
  "code": "MUTTER_NOT_FOUND",
  "message": "指定されたつぶやきは存在しません"
}
```

| ステータス | 主な意味 |
|---|---|
| `400 Bad Request` | ID、本文、JSON、versionなどが不正 |
| `401 Unauthorized` | 未ログイン |
| `403 Forbidden` | 他のユーザーの投稿を更新・削除しようとした |
| `404 Not Found` | 投稿またはAPIリソースが存在しない |
| `409 Conflict` | 楽観的ロックによる更新競合 |

実装の中心は`MutterApiServlet`です。URLは名詞で統一し、操作はHTTPメソッドで表します。
## ModelとDAO

### User

| フィールド | 内容 |
|---|---|
| `id` | ユーザーID |
| `name` | ユーザー名 |
| `pass` | BCryptでハッシュ化されたパスワード |

### Mutter

| フィールド | 内容 |
|---|---|
| `id` | つぶやきID |
| `userId` | 投稿者のユーザーID |
| `userName` | 一覧表示用のユーザー名 |
| `text` | つぶやき本文 |
| `version` | 楽観的ロック用のバージョン番号 |
| `createdAt` | 投稿日時 |

### DAOの主なメソッド

| クラス | メソッド | SQLの役割 |
|---|---|---|
| `UserDAO` | `findByName()` | ユーザー名からユーザーを検索 |
| `UserDAO` | `create()` | ユーザー登録 |
| `MutterDAO` | `findLatest()` | 最新のつぶやきを指定件数取得 |
| `MutterDAO` | `findByCursor()` | 指定IDより古いつぶやきを取得 |
| `MutterDAO` | `findById()` | つぶやきをIDで1件取得 |
| `MutterDAO` | `findPage()` | 検索条件とカーソルを使った一覧取得 |
| `MutterDAO` | `create()` | 画面からのつぶやき投稿 |
| `MutterDAO` | `createAndReturn()` | APIから作成し、採番済みデータを取得 |
| `MutterDAO` | `search()` | 本文の部分一致検索 |
| `MutterDAO` | `update()` | VERSIONを確認して本文更新 |
| `MutterDAO` | `delete()` | 投稿者本人のつぶやきを削除 |

## スコープの使い分け

### リクエストスコープ

1回のリクエストからJSPへ値を渡すために使います。

```java
request.setAttribute("mutterList", mutterList);
```

一覧、検索結果、編集対象などはリクエストスコープへ保存します。

### セッションスコープ

複数の画面にまたがって保持する値に使います。

```java
session.setAttribute("loginUser", dBUser);
```

このアプリではログインユーザーと、リダイレクト後に表示するエラーメッセージを保存します。

同じブラウザの複数タブは同じセッションCookieを共有します。
同じブラウザで別ユーザーを同時に試す場合は、通常ウィンドウとシークレットウィンドウ、または別ブラウザを使ってください。

## データベースの準備

接続先は次のH2 Databaseです。

```text
jdbc:h2:tcp://localhost/~/dokoTsubu
ユーザー: sa
パスワード: なし
```

H2をTCPサーバーモードで起動し、`dokoTsubu`データベースに次のテーブルを作成します。

```sql
CREATE TABLE USERS (
    ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME VARCHAR(100) NOT NULL UNIQUE,
    PASS VARCHAR(100) NOT NULL
);

CREATE TABLE MUTTERS (
    ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    USER_ID INT NOT NULL,
    TEXT VARCHAR(255) NOT NULL,
    VERSION INT DEFAULT 0 NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES USERS(ID)
);

CREATE INDEX IDX_MUTTERS_ID_DESC ON MUTTERS(ID DESC);
```

すでにテーブルが存在する場合は、同じSQLを再実行する必要はありません。
`VERSION`列を後から追加する場合は、既存行にも0が入っていることを確認してください。
`IDX_MUTTERS_ID_DESC`は、IDの降順で最新投稿を少数件取得する処理を補助します。

## ビルドと実行

### 必要な環境

- JDK 17
- Maven
- Servlet 6.0対応のTomcat（Tomcat 10.1系など）
- H2 DatabaseのTCPサーバー

### WARファイルの作成

プロジェクトルートで実行します。

```shell
mvn clean package
```

成功すると、次のWARファイルが作成されます。

```text
target/dokoTsubu.war
```

このWARをTomcatの`webapps`へ配置してTomcatを起動します。

```text
http://localhost:8080/dokoTsubu/
```

EclipseなどからTomcatへ追加して実行する場合も、アクセス先のコンテキストパスは通常`/dokoTsubu`です。

## 初学者向け：コードを読む順番

最初は次の順番で追うと、ファイル間の関係を理解しやすくなります。

1. `src/main/webapp/index.jsp`でログインフォームの送信先を見る
2. `Login.java`でフォームの値をどう受け取るかを見る
3. `LoginLogic.java`でログイン判定の流れを見る
4. `UserDAO.java`でSQLとPreparedStatementを見る
5. `Main.java`から`main.jsp`へ一覧を渡す流れを見る
6. `main.jsp`の投稿・検索・編集・削除フォームの送信先を見る
7. 各Logicから`MutterDAO`の対応メソッドを追う

フォームの`action`、Servletの`@WebServlet`、JSPへの`RequestDispatcher`を順に探すのがコツです。

## 今後改善できる点

- DAOの接続処理をすべて`DBUtil`へ統一する
- H2やJSTLを`WEB-INF/lib`への手動配置ではなくMaven依存関係で管理する
- Servlet名を`MainServlet`など、役割がより明確な名前へ統一する
- 入力文字数の上限やユーザー名形式のバリデーションを追加する
- CSRF対策を追加する
- APIエラー時もJSON形式でメッセージを返す
- DAO・Logic・Servletの自動テストを追加する
- CSSを追加して画面を見やすくする
- DB接続情報を環境変数や設定ファイルへ移す

このプロジェクトは学習用のため、まずは「画面 → Servlet → Logic → DAO → DB」という流れを理解し、
慣れてきたら上記を少しずつ改善していくと学びやすくなります。
