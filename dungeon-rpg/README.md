# Dungeon RPG

Java 21とSpring Bootで作った、ブラウザで遊べる簡易ダンジョン探索RPGです。
5×5マスのダンジョンを移動し、ランダムに遭遇する敵と戦いながらプレイヤーを育てます。

このプロジェクトは、Spring MVCの基本的な役割分担を学びやすいように、
Controller・Service・Model・Viewを分けて実装しています。

## アプリ概要

現在、次の機能を実装しています。

- プレイヤー名を入力してゲーム開始
- セッションを使ったゲーム状態の保存
- 5×5マスのダンジョン移動と方向転換
- 前進時に30%の確率で敵と遭遇
- 攻撃、反撃、勝利、敗北を含むターン制戦闘
- 経験値獲得とレベルアップ
- 街への帰還、宿屋での全回復、ダンジョンへの再入場
- レトロRPG風のレスポンシブ画面

データベースは使用していません。ゲーム状態はブラウザごとのHTTPセッションに保存されるため、
アプリを再起動したりセッションが切れたりするとゲーム状態は失われます。

## 使用技術

| 分類 | 技術 |
|---|---|
| 言語 | Java 21 |
| フレームワーク | Spring Boot 4.1.0 / Spring MVC |
| テンプレート | Thymeleaf |
| 画面 | HTML / CSS |
| ビルド | Maven |
| テスト | JUnit 5 / AssertJ / Spring Boot Test |
| データ保存 | HttpSession（DBなし） |

## 起動方法

### 必要なもの

- JDK 21
- Maven 3.6.3以上

### 起動手順

このREADMEがある`dungeon-rpg`ディレクトリへ移動し、次を実行します。

```shell
mvn spring-boot:run
```

起動ログに`Started DungeonRpgApplication`と表示されたら、ブラウザで以下を開きます。

```text
http://localhost:8080/
```

終了するときは、起動したターミナルで`Ctrl + C`を押します。

8080番ポートが使用中の場合は、別のポートを指定できます。

```shell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

### テスト

```shell
mvn test
```

## 画面一覧

| URL | 画面 | 主な操作 |
|---|---|---|
| `/` | トップ | プレイヤー名を入力してゲームを開始 |
| `/game` | ダンジョン | 前進、左・右を向く、街へ戻る |
| `/battle` | 戦闘 | 敵を攻撃し、戦闘ログを確認 |
| `/town` | 街 | 宿屋で休む、ダンジョンへ入る |
| `/game-over` | ゲームオーバー | 戦闘ログを確認して再挑戦 |

通常、画面のボタンから移動するため、URLを直接入力する必要はありません。
Controllerが現在のゲーム状態を確認し、不適切な画面へアクセスした場合は正しい画面へ戻します。

## プロジェクト構成

```text
dungeon-rpg/
├─ pom.xml
├─ README.md
└─ src/
   ├─ main/
   │  ├─ java/com/example/dungeonrpg/
   │  │  ├─ controller/  リクエスト受付と画面遷移
   │  │  ├─ service/     移動・遭遇・戦闘のルール
   │  │  └─ model/       プレイヤーやゲーム状態のデータ
   │  └─ resources/
   │     ├─ templates/   Thymeleafの画面
   │     └─ static/css/  共通スタイル
   └─ test/              Serviceや初期状態のテスト
```

### 処理の流れ

例として「前進」を押した場合、処理は次の順番で進みます。

1. `DungeonController`がHTTPリクエストを受け取る
2. `DungeonMovementService`が移動可能か判定する
3. 移動できた場合、`EncounterService`が敵との遭遇を判定する
4. `GameState`の座標や敵情報が更新される
5. Controllerがダンジョン画面または戦闘画面へ遷移させる

Controllerは画面遷移、Serviceはゲームルール、Modelは状態の保持を担当します。

## 主要クラスの説明

### Controller

| クラス | 責務 |
|---|---|
| `GameController` | トップ画面、ゲーム開始、ゲームオーバー表示 |
| `DungeonController` | ダンジョン表示、前進、方向転換、遭遇後の遷移 |
| `BattleController` | 戦闘表示、攻撃、勝敗後の遷移 |
| `TownController` | 街への帰還、宿屋、ダンジョンへの再入場 |

### Service

| クラス | 責務 |
|---|---|
| `DungeonMovementService` | 5×5マスの境界判定、前進、左右の方向転換 |
| `EncounterService` | 30%の遭遇判定と敵のランダム生成 |
| `BattleService` | ダメージ計算、反撃、勝敗、経験値付与 |
| `MovementResult` | 移動できたかどうかと表示メッセージ |
| `BattleResult` / `BattleOutcome` | 戦闘結果とレベルアップ数、勝敗状態 |

### Model

| クラス | 責務 |
|---|---|
| `GameState` | セッションに保存するゲーム全体の状態 |
| `Player` | 能力値、HP、経験値、レベルアップ、回復 |
| `Enemy` | 敵の名前、HP、能力値、獲得経験値 |
| `Direction` | NORTH・EAST・SOUTH・WESTと日本語表示名 |

### View

`src/main/resources/templates`のHTMLがThymeleafテンプレートです。
Controllerから渡された`GameState`などを`${gameState.player.hp}`のように表示します。
見た目は`src/main/resources/static/css/style.css`にまとめています。

## 今後追加できる機能案

- 逃げる、防御、スキル、魔法などの戦闘コマンド
- 回復薬、武器、防具、所持品
- 宝箱、罠、階段、ボス部屋
- ダンジョンマップのランダム生成
- 複数の階層と階層ごとの敵
- ゴールド、武器屋、防具屋
- レベルアップ時の能力選択
- Spring Data JPAとDBを使ったセーブ・ロード
- Spring Securityを使ったユーザー登録とログイン
- 効果音、画像、アニメーション

機能を追加するときも、「画面遷移はController」「ゲームルールはService」
「状態はModel」という分担を保つと、コードを整理しやすくなります。
