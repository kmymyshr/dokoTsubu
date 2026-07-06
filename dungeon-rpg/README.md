# Dungeon RPG

Java 21、Spring Boot、Thymeleafで作る簡易ダンジョン探索RPGです。
現在はプレイヤー名を入力し、ゲーム画面を開始するところまで実装しています。

## 起動方法

Java 21とMaven 3.6.3以上を用意し、`dungeon-rpg`ディレクトリで実行します。

```shell
mvn spring-boot:run
```

起動後、ブラウザで <http://localhost:8080/> を開いてください。

## 主な構成

```text
src/main/java/.../controller  画面からのリクエストを処理
src/main/java/.../model       セッションに保存するゲーム状態
src/main/resources/templates  ThymeleafのHTML
src/main/resources/static     CSSなどの静的ファイル
```
