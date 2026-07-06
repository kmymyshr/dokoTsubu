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

    public void gainExperience(int gainedExperience) {
        experience += gainedExperience;
    }

    public boolean isDefeated() {
        return hp <= 0;
    }
}
