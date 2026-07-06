package com.example.dungeonrpg.service;

/** 戦闘結果と、その攻撃で上がったレベル数をまとめます。 */
public record BattleResult(BattleOutcome outcome, int levelUpCount) {
}
