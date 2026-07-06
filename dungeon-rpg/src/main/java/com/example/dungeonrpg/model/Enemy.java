package com.example.dungeonrpg.model;

import java.io.Serializable;

/** 戦闘で使用する敵の情報です。 */
public class Enemy implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private int hp;
    private final int attack;
    private final int defense;
    private final int experience;

    public Enemy(String name, int hp, int attack, int defense, int experience) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.experience = experience;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
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

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    public boolean isDefeated() {
        return hp <= 0;
    }
}
