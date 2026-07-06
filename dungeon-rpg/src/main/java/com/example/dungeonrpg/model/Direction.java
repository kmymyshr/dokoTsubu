package com.example.dungeonrpg.model;

/** ダンジョン内でプレイヤーが向いている方向です。 */
public enum Direction {
    NORTH("北"),
    EAST("東"),
    SOUTH("南"),
    WEST("西");

    private final String displayName;

    Direction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
