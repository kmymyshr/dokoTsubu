package com.example.dungeonrpg.controller;

import com.example.dungeonrpg.model.GameState;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** ゲームの開始と終了に関する画面遷移を担当します。 */
@Controller
public class GameController {

    @GetMapping("/")
    public String showTopPage() {
        return "index";
    }

    @PostMapping("/game/start")
    public String startNewGame(@RequestParam String playerName,
                               HttpSession session,
                               Model model) {
        String normalizedName = playerName.trim();
        if (normalizedName.isEmpty()) {
            model.addAttribute("errorMessage", "プレイヤー名を入力してください。");
            return "index";
        }

        session.setAttribute(
                SessionAttributeNames.GAME_STATE,
                new GameState(normalizedName));
        return "redirect:/game";
    }

    @GetMapping("/game-over")
    public String showGameOver(HttpSession session, Model model) {
        GameState gameState = getGameState(session);
        if (gameState == null || !gameState.getPlayer().isDefeated()) {
            return "redirect:/";
        }

        model.addAttribute("gameState", gameState);
        return "game-over";
    }

    private GameState getGameState(HttpSession session) {
        return (GameState) session.getAttribute(SessionAttributeNames.GAME_STATE);
    }
}
