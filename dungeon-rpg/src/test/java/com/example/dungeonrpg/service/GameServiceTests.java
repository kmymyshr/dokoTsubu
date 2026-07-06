package com.example.dungeonrpg.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dungeonrpg.model.Direction;
import com.example.dungeonrpg.model.GameState;
import org.junit.jupiter.api.Test;

class GameServiceTests {
    private final GameService gameService = new GameService();

    @Test
    void cannotMoveOutsideDungeon() {
        GameState gameState = new GameState("アレン");

        MoveResult result = gameService.moveForward(gameState);

        assertThat(result.moved()).isFalse();
        assertThat(result.message()).contains("壁");
        assertThat(gameState.getDungeonX()).isZero();
        assertThat(gameState.getDungeonY()).isZero();
    }

    @Test
    void canTurnAndMoveForward() {
        GameState gameState = new GameState("アレン");

        gameService.turnRight(gameState);
        MoveResult result = gameService.moveForward(gameState);

        assertThat(result.moved()).isTrue();
        assertThat(gameState.getDirection()).isEqualTo(Direction.EAST);
        assertThat(gameState.getDungeonX()).isEqualTo(1);
        assertThat(gameState.getDungeonY()).isZero();
    }

    @Test
    void leftAndRightTurnsFollowCompassOrder() {
        GameState gameState = new GameState("アレン");

        gameService.turnLeft(gameState);
        assertThat(gameState.getDirection()).isEqualTo(Direction.WEST);

        gameService.turnRight(gameState);
        assertThat(gameState.getDirection()).isEqualTo(Direction.NORTH);
    }
}
