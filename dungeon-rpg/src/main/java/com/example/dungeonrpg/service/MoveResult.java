package com.example.dungeonrpg.service;

/** 前進できたかどうかと、画面に表示するメッセージをまとめた結果です。 */
public record MoveResult(boolean moved, String message) {
}
