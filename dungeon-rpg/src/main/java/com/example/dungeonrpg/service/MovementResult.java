package com.example.dungeonrpg.service;

/** 前進できたかどうかと画面表示用メッセージをまとめた結果です。 */
public record MovementResult(boolean moved, String message) {
}
