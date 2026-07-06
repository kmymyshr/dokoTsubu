package com.example.dungeonrpg.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * プレイヤーごとのゲーム状態です。
 * 今はDBを使わず、HttpSessionにこのオブジェクトを保存します。
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Player player;
    private int dungeonX;
    private int dungeonY;
    private Direction direction;
    private Enemy currentEnemy;
    private final List<String> battleLog;
    private boolean inTown;

    public GameState(String playerName) {
        this.player = new Player(playerName);
        this.dungeonX = 0;
        this.dungeonY = 0;
        this.direction = Direction.NORTH;
        this.battleLog = new ArrayList<>();
        this.inTown = false;
    }

    public Player getPlayer() {
        return player;
    }

    public int getDungeonX() {
        return dungeonX;
    }

    public int getDungeonY() {
        return dungeonY;
    }

    public Direction getDirection() {
        return direction;
    }

    public Enemy getCurrentEnemy() {
        return currentEnemy;
    }

    public List<String> getBattleLog() {
        return Collections.unmodifiableList(battleLog);
    }

    public boolean isInTown() {
        return inTown;
    }

    /** Serviceから移動結果を反映するためのメソッドです。 */
    public void moveTo(int dungeonX, int dungeonY) {
        this.dungeonX = dungeonX;
        this.dungeonY = dungeonY;
    }

    /** Serviceから方向転換の結果を反映するためのメソッドです。 */
    public void changeDirection(Direction direction) {
        this.direction = direction;
    }

    /** 遭遇した敵を、戦闘が終わるまでゲーム状態に保存します。 */
    public void startBattle(Enemy enemy) {
        this.currentEnemy = enemy;
        battleLog.clear();
        battleLog.add(enemy.getName() + "が現れた！");
    }

    public void addBattleLog(String message) {
        battleLog.add(message);
    }

    public void finishBattle() {
        this.currentEnemy = null;
    }

    public void returnToTown() {
        this.inTown = true;
    }

    /** ダンジョンへ入るたび、入口の座標と北向きに戻します。 */
    public void enterDungeon() {
        this.inTown = false;
        this.dungeonX = 0;
        this.dungeonY = 0;
        this.direction = Direction.NORTH;
    }
}
