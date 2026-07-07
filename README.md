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
| DB | H2 Database |
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
    H2 Database
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
firstSampleRepository/
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

## セットアップ

前提:

- JDK 17
- Maven
- Tomcat 10.1 系など Servlet 6.0 対応コンテナ
- H2 Database サーバー

H2 接続設定:

```text
jdbc:h2:tcp://localhost/~/dokoTsubu
user: sa
password: なし
```

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
