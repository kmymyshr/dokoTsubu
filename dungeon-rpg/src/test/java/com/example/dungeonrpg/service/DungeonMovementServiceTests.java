package com.example.dungeonrpg.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dungeonrpg.model.Direction;
import com.example.dungeonrpg.model.GameState;
import org.junit.jupiter.api.Test;

class DungeonMovementServiceTests {
    private final DungeonMovementService movementService = new DungeonMovementService();

    @Test
    void cannotMoveOutsideDungeon() {
        GameState gameState = new GameState("アレン");

        MovementResult result = movementService.moveForward(gameState);

        assertThat(result.moved()).isFalse();
        assertThat(result.message()).contains("壁");
        assertThat(gameState.getDungeonX()).isZero();
        assertThat(gameState.getDungeonY()).isZero();
    }

    @Test
    void canTurnAndMoveForward() {
        GameState gameState = new GameState("アレン");

        movementService.turnRight(gameState);
        MovementResult result = movementService.moveForward(gameState);

        assertThat(result.moved()).isTrue();
        assertThat(gameState.getDirection()).isEqualTo(Direction.EAST);
        assertThat(gameState.getDungeonX()).isEqualTo(1);
        assertThat(gameState.getDungeonY()).isZero();
    }

    @Test
    void leftAndRightTurnsFollowCompassOrder() {
        GameState gameState = new GameState("アレン");

        movementService.turnLeft(gameState);
        assertThat(gameState.getDirection()).isEqualTo(Direction.WEST);

        movementService.turnRight(gameState);
        assertThat(gameState.getDirection()).isEqualTo(Direction.NORTH);
    }
}
