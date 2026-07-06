package com.example.dungeonrpg.service;

import com.example.dungeonrpg.model.Direction;
import com.example.dungeonrpg.model.GameState;
import org.springframework.stereotype.Service;

/** ダンジョンの境界判定と方向転換を担当します。 */
@Service
public class DungeonMovementService {
    public static final int DUNGEON_SIZE = 5;

    public MovementResult moveForward(GameState gameState) {
        int nextX = gameState.getDungeonX();
        int nextY = gameState.getDungeonY();

        switch (gameState.getDirection()) {
            case NORTH -> nextY--;
            case EAST -> nextX++;
            case SOUTH -> nextY++;
            case WEST -> nextX--;
        }

        if (isOutsideDungeon(nextX, nextY)) {
            return new MovementResult(false, "壁があり、これ以上進めません。");
        }

        gameState.moveTo(nextX, nextY);
        return new MovementResult(true, "前へ1マス進みました。");
    }

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

    private boolean isOutsideDungeon(int x, int y) {
        return x < 0 || x >= DUNGEON_SIZE || y < 0 || y >= DUNGEON_SIZE;
    }
}
