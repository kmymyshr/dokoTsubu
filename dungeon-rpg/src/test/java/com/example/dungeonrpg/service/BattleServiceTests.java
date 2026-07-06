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

        BattleResult result = battleService.attack(gameState);

        assertThat(result.outcome()).isEqualTo(BattleOutcome.ONGOING);
        assertThat(enemy.getHp()).isEqualTo(12); // 8 - 2 = 6ダメージ
        assertThat(gameState.getPlayer().getHp()).isEqualTo(28); // 6 - 4 = 2ダメージ
        assertThat(gameState.getBattleLog()).hasSize(3);
    }

    @Test
    void victoryAwardsExperienceAndEndsBattle() {
        GameState gameState = new GameState("アレン");
        gameState.startBattle(new Enemy("Slime", 1, 100, 100, 5));

        BattleResult result = battleService.attack(gameState);

        assertThat(result.outcome()).isEqualTo(BattleOutcome.VICTORY);
        assertThat(result.levelUpCount()).isZero();
        assertThat(gameState.getPlayer().getExperience()).isEqualTo(5);
        assertThat(gameState.getPlayer().getHp()).isEqualTo(30); // 倒した敵は反撃しない
        assertThat(gameState.getCurrentEnemy()).isNull();
    }

    @Test
    void defeatOccursWhenPlayerHpReachesZero() {
        GameState gameState = new GameState("アレン");
        gameState.startBattle(new Enemy("Dragon", 100, 100, 100, 999));

        BattleResult result = battleService.attack(gameState);

        assertThat(result.outcome()).isEqualTo(BattleOutcome.DEFEAT);
        assertThat(gameState.getPlayer().getHp()).isZero();
        assertThat(gameState.getBattleLog().getLast()).contains("力尽きた");
    }

    @Test
    void victoryCanLevelUpPlayerAndRestoreHp() {
        GameState gameState = new GameState("アレン");
        gameState.getPlayer().takeDamage(10);
        gameState.startBattle(new Enemy("Skeleton", 1, 1, 1, 12));

        BattleResult result = battleService.attack(gameState);

        assertThat(result.outcome()).isEqualTo(BattleOutcome.VICTORY);
        assertThat(result.levelUpCount()).isEqualTo(1);
        assertThat(gameState.getPlayer().getLevel()).isEqualTo(2);
        assertThat(gameState.getPlayer().getExperience()).isEqualTo(2);
        assertThat(gameState.getPlayer().getMaxHp()).isEqualTo(35);
        assertThat(gameState.getPlayer().getHp()).isEqualTo(35);
        assertThat(gameState.getPlayer().getAttack()).isEqualTo(10);
        assertThat(gameState.getPlayer().getDefense()).isEqualTo(5);
        assertThat(gameState.getBattleLog().getLast()).contains("レベルが2");
    }
}
