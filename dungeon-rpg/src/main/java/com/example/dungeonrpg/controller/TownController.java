package com.example.dungeonrpg.controller;

import com.example.dungeonrpg.model.GameState;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 街への移動、宿屋、ダンジョンへの再入場を担当します。 */
@Controller
public class TownController {
    @PostMapping("/town/return")
    public String returnToTown(HttpSession session) {
        GameState gameState = getGameState(session);
        String unavailablePage = findUnavailablePage(gameState);
        if (unavailablePage != null) {
            return unavailablePage;
        }

        gameState.returnToTown();
        return "redirect:/town";
    }

    @GetMapping("/town")
    public String showTown(HttpSession session, Model model) {
        GameState gameState = getGameState(session);
        if (gameState == null) {
            return "redirect:/";
        }
        if (gameState.getPlayer().isDefeated()) {
            return "redirect:/game-over";
        }
        if (gameState.getCurrentEnemy() != null) {
            return "redirect:/battle";
        }
        if (!gameState.isInTown()) {
            return "redirect:/game";
        }

        model.addAttribute("gameState", gameState);
        return "town";
    }

    @PostMapping("/town/rest")
    public String restAtInn(HttpSession session, RedirectAttributes redirectAttributes) {
        GameState gameState = getGameState(session);
        if (gameState == null || !gameState.isInTown()) {
            return "redirect:/game";
        }

        gameState.getPlayer().healFully();
        redirectAttributes.addFlashAttribute("townMessage", "宿屋で休み、HPが全回復しました。");
        return "redirect:/town";
    }

    @PostMapping("/town/enter")
    public String enterDungeon(HttpSession session) {
        GameState gameState = getGameState(session);
        if (gameState == null || !gameState.isInTown()) {
            return "redirect:/game";
        }

        gameState.enterDungeon();
        return "redirect:/game";
    }

    private GameState getGameState(HttpSession session) {
        return (GameState) session.getAttribute(SessionAttributeNames.GAME_STATE);
    }

    /** 街へ戻れない状態なら、その状態に合う画面を返します。 */
    private String findUnavailablePage(GameState gameState) {
        if (gameState == null) {
            return "redirect:/";
        }
        if (gameState.getPlayer().isDefeated()) {
            return "redirect:/game-over";
        }
        if (gameState.getCurrentEnemy() != null) {
            return "redirect:/battle";
        }
        return null;
    }
}
