package com.example.dungeonrpg.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dungeonrpg.model.Enemy;
import com.example.dungeonrpg.model.GameState;
import org.junit.jupiter.api.Test;

class BattleServiceTests {
    private final BattleService battleService = new BattleService();

    @Test
    void playerAttacksAndEnemyCounterattacks() {
        GameState gameState = new GameState("アレン");
        Enemy enemy = new Enemy("Goblin", 18, 6, 2, 8);
        gameState.startBattle(enemy);

        BattleOutcome outcome = battleService.attack(gameState);

        assertThat(outcome).isEqualTo(BattleOutcome.ONGOING);
        assertThat(enemy.getHp()).isEqualTo(12); // 8 - 2 = 6ダメージ
        assertThat(gameState.getPlayer().getHp()).isEqualTo(28); // 6 - 4 = 2ダメージ
        assertThat(gameState.getBattleLog()).hasSize(3);
    }

    @Test
    void victoryAwardsExperienceAndEndsBattle() {
        GameState gameState = new GameState("アレン");
        gameState.startBattle(new Enemy("Slime", 1, 100, 100, 5));

        BattleOutcome outcome = battleService.attack(gameState);

        assertThat(outcome).isEqualTo(BattleOutcome.VICTORY);
        assertThat(gameState.getPlayer().getExperience()).isEqualTo(5);
        assertThat(gameState.getPlayer().getHp()).isEqualTo(30); // 倒した敵は反撃しない
        assertThat(gameState.getCurrentEnemy()).isNull();
    }

    @Test
    void defeatOccursWhenPlayerHpReachesZero() {
        GameState gameState = new GameState("アレン");
        gameState.startBattle(new Enemy("Dragon", 100, 100, 100, 999));

        BattleOutcome outcome = battleService.attack(gameState);

        assertThat(outcome).isEqualTo(BattleOutcome.DEFEAT);
        assertThat(gameState.getPlayer().getHp()).isZero();
        assertThat(gameState.getBattleLog().getLast()).contains("力尽きた");
    }
}
