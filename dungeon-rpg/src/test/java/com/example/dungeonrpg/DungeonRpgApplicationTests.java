package com.example.dungeonrpg;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dungeonrpg.model.Direction;
import com.example.dungeonrpg.model.GameState;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DungeonRpgApplicationTests {

    @Test
    void contextLoads() {
        // Springの設定を読み込み、アプリが起動できることを確認します。
    }

    @Test
    void newGameHasExpectedInitialState() {
        GameState gameState = new GameState("アレン");

        assertThat(gameState.getPlayer().getName()).isEqualTo("アレン");
        assertThat(gameState.getPlayer().getLevel()).isEqualTo(1);
        assertThat(gameState.getPlayer().getHp()).isEqualTo(30);
        assertThat(gameState.getPlayer().getMaxHp()).isEqualTo(30);
        assertThat(gameState.getPlayer().getAttack()).isEqualTo(8);
        assertThat(gameState.getPlayer().getDefense()).isEqualTo(4);
        assertThat(gameState.getPlayer().getExperience()).isZero();
        assertThat(gameState.getDungeonX()).isZero();
        assertThat(gameState.getDungeonY()).isZero();
        assertThat(gameState.getDirection()).isEqualTo(Direction.NORTH);
    }

    @Test
    void townRestoresHpAndDungeonEntryResetsPosition() {
        GameState gameState = new GameState("アレン");
        gameState.getPlayer().takeDamage(10);
        gameState.moveTo(3, 4);
        gameState.changeDirection(Direction.WEST);

        gameState.returnToTown();
        gameState.getPlayer().healFully();

        assertThat(gameState.isInTown()).isTrue();
        assertThat(gameState.getPlayer().getHp()).isEqualTo(30);

        gameState.enterDungeon();

        assertThat(gameState.isInTown()).isFalse();
        assertThat(gameState.getDungeonX()).isZero();
        assertThat(gameState.getDungeonY()).isZero();
        assertThat(gameState.getDirection()).isEqualTo(Direction.NORTH);
    }
}
