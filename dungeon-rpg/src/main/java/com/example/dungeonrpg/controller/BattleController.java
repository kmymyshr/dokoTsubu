package com.example.dungeonrpg.controller;

import com.example.dungeonrpg.model.GameState;
import com.example.dungeonrpg.service.BattleOutcome;
import com.example.dungeonrpg.service.BattleResult;
import com.example.dungeonrpg.service.BattleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 戦闘画面と攻撃後の画面遷移を担当します。 */
@Controller
public class BattleController {
    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @GetMapping("/battle")
    public String showBattle(HttpSession session, Model model) {
        GameState gameState = getGameState(session);
        if (gameState == null) {
            return "redirect:/";
        }
        if (gameState.getPlayer().isDefeated()) {
            return "redirect:/game-over";
        }
        if (gameState.getCurrentEnemy() == null) {
            return "redirect:/game";
        }

        model.addAttribute("gameState", gameState);
        model.addAttribute("enemy", gameState.getCurrentEnemy());
        return "battle";
    }

    @PostMapping("/battle/attack")
    public String attack(HttpSession session, RedirectAttributes redirectAttributes) {
        GameState gameState = getGameState(session);
        if (gameState == null) {
            return "redirect:/";
        }
        if (gameState.getCurrentEnemy() == null) {
            return "redirect:/game";
        }

        BattleResult result = battleService.attack(gameState);
        if (result.outcome() == BattleOutcome.DEFEAT) {
            return "redirect:/game-over";
        }
        if (result.outcome() == BattleOutcome.VICTORY) {
            redirectAttributes.addFlashAttribute(
                    "actionMessage", createVictoryMessage(gameState, result));
            return "redirect:/game";
        }
        return "redirect:/battle";
    }

    private String createVictoryMessage(GameState gameState, BattleResult result) {
        if (result.levelUpCount() == 0) {
            return "敵に勝利し、経験値を獲得しました！";
        }
        return "敵に勝利！ レベルが" + gameState.getPlayer().getLevel()
                + "になり、HPが全回復しました！";
    }

    private GameState getGameState(HttpSession session) {
        return (GameState) session.getAttribute(SessionAttributeNames.GAME_STATE);
    }
}
