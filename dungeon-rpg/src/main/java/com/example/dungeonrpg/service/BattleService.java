package com.example.dungeonrpg.service;

import com.example.dungeonrpg.model.Enemy;
import com.example.dungeonrpg.model.GameState;
import com.example.dungeonrpg.model.Player;
import org.springframework.stereotype.Service;

/** プレイヤーと敵の攻撃、ダメージ計算、勝敗判定を担当します。 */
@Service
public class BattleService {

    public BattleResult attack(GameState gameState) {
        Player player = gameState.getPlayer();
        Enemy enemy = gameState.getCurrentEnemy();
        if (enemy == null) {
            throw new IllegalStateException("戦闘中の敵がいません。");
        }

        int damageToEnemy = calculateDamage(player.getAttack(), enemy.getDefense());
        enemy.takeDamage(damageToEnemy);
        gameState.addBattleLog(player.getName() + "の攻撃！ "
                + enemy.getName() + "に" + damageToEnemy + "ダメージ。");

        // 敵を倒した場合、敵は反撃しません。
        if (enemy.isDefeated()) {
            int levelUpCount = player.gainExperience(enemy.getExperience());
            gameState.addBattleLog(enemy.getName() + "を倒した！ 経験値"
                    + enemy.getExperience() + "を獲得。");
            if (levelUpCount > 0) {
                gameState.addBattleLog("レベルが" + player.getLevel()
                        + "になった！ HPが全回復し、能力が上昇した。");
            }
            gameState.finishBattle();
            return new BattleResult(BattleOutcome.VICTORY, levelUpCount);
        }

        int damageToPlayer = calculateDamage(enemy.getAttack(), player.getDefense());
        player.takeDamage(damageToPlayer);
        gameState.addBattleLog(enemy.getName() + "の反撃！ "
                + player.getName() + "は" + damageToPlayer + "ダメージを受けた。");

        if (player.isDefeated()) {
            gameState.addBattleLog(player.getName() + "は力尽きた……。");
            return new BattleResult(BattleOutcome.DEFEAT, 0);
        }
        return new BattleResult(BattleOutcome.ONGOING, 0);
    }

    private int calculateDamage(int attack, int defense) {
        return Math.max(1, attack - defense);
    }
}
