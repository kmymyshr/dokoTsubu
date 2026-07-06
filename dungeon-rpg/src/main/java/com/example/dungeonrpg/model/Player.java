package com.example.dungeonrpg.model;

import java.io.Serializable;

/** プレイヤーの能力値をまとめて管理するクラスです。 */
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private int level;
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private int experience;

    public Player(String name) {
        this.name = name;
        this.level = 1;
        this.maxHp = 30;
        this.hp = maxHp;
        this.attack = 8;
        this.defense = 4;
        this.experience = 0;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getExperience() {
        return experience;
    }

    /** ダメージを受けます。HPは0未満にならないようにします。 */
    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    /**
     * 経験値を加算してレベルアップを判定します。
     * 戻り値は、一度に上がったレベル数です。
     */
    public int gainExperience(int gainedExperience) {
        experience += gainedExperience;

        int levelUpCount = 0;
        int requiredExperience = level * 10;
        while (experience >= requiredExperience) {
            experience -= requiredExperience;
            level++;
            maxHp += 5;
            attack += 2;
            defense += 1;
            hp = maxHp;
            levelUpCount++;

            // レベルが上がるたび、次に必要な経験値も増えます。
            requiredExperience = level * 10;
        }
        return levelUpCount;
    }

    public boolean isDefeated() {
        return hp <= 0;
    }

    /** 宿屋などでHPを最大値まで回復します。 */
    public void healFully() {
        hp = maxHp;
    }
}
