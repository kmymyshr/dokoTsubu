package com.example.dungeonrpg.service;

import com.example.dungeonrpg.model.Direction;
import com.example.dungeonrpg.model.GameState;
import org.springframework.stereotype.Service;

/** ダンジョン内の移動ルールを担当するクラスです。 */
@Service
public class GameService {
    public static final int DUNGEON_SIZE = 5;

    /** 現在向いている方向へ1マス進みます。 */
    public MoveResult moveForward(GameState gameState) {
        int nextX = gameState.getDungeonX();
        int nextY = gameState.getDungeonY();

        switch (gameState.getDirection()) {
            case NORTH -> nextY--;
            case EAST -> nextX++;
            case SOUTH -> nextY++;
            case WEST -> nextX--;
        }

        // 座標は0～4です。範囲外なら状態を変更しません。
        if (nextX < 0 || nextX >= DUNGEON_SIZE
                || nextY < 0 || nextY >= DUNGEON_SIZE) {
            return new MoveResult(false, "壁があり、これ以上進めません。");
        }

        gameState.moveTo(nextX, nextY);
        return new MoveResult(true, "前へ1マス進みました。");
    }

    /** プレイヤーを左へ90度回転させます。 */
    public String turnLeft(GameState gameState) {
        Direction nextDirection = switch (gameState.getDirection()) {
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
        };
        gameState.changeDirection(nextDirection);
        return "左を向きました。";
    }

    /** プレイヤーを右へ90度回転させます。 */
    public String turnRight(GameState gameState) {
        Direction nextDirection = switch (gameState.getDirection()) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
        };
        gameState.changeDirection(nextDirection);
        return "右を向きました。";
    }
}
