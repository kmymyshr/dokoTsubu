package com.example.dungeonrpg.controller;

import com.example.dungeonrpg.model.Enemy;
import com.example.dungeonrpg.model.GameState;
import com.example.dungeonrpg.service.DungeonMovementService;
import com.example.dungeonrpg.service.EncounterService;
import com.example.dungeonrpg.service.MovementResult;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** ダンジョン画面と移動操作を担当します。 */
@Controller
public class DungeonController {
    private final DungeonMovementService movementService;
    private final EncounterService encounterService;

    public DungeonController(DungeonMovementService movementService,
                             EncounterService encounterService) {
        this.movementService = movementService;
        this.encounterService = encounterService;
    }

    @GetMapping("/game")
    public String showDungeon(HttpSession session, Model model) {
        GameState gameState = getGameState(session);
        String redirect = findRedirectForUnavailableState(gameState);
        if (redirect != null) {
            return redirect;
        }

        model.addAttribute("gameState", gameState);
        return "game";
    }

    @PostMapping("/game/move")
    public String moveForward(HttpSession session, RedirectAttributes redirectAttributes) {
        GameState gameState = getGameState(session);
        String redirect = findRedirectForUnavailableState(gameState);
        if (redirect != null) {
            return redirect;
        }

        MovementResult result = movementService.moveForward(gameState);
        redirectAttributes.addFlashAttribute("actionMessage", result.message());

        // 前進できた場合だけ、敵との遭遇を判定します。
        if (result.moved()) {
            Optional<Enemy> enemy = encounterService.findEncounter();
            if (enemy.isPresent()) {
                gameState.startBattle(enemy.get());
                return "redirect:/battle";
            }
        }
        return "redirect:/game";
    }

    @PostMapping("/game/turn-left")
    public String turnLeft(HttpSession session, RedirectAttributes redirectAttributes) {
        GameState gameState = getGameState(session);
        String redirect = findRedirectForUnavailableState(gameState);
        if (redirect != null) {
            return redirect;
        }

        redirectAttributes.addFlashAttribute(
                "actionMessage", movementService.turnLeft(gameState));
        return "redirect:/game";
    }

    @PostMapping("/game/turn-right")
    public String turnRight(HttpSession session, RedirectAttributes redirectAttributes) {
        GameState gameState = getGameState(session);
        String redirect = findRedirectForUnavailableState(gameState);
        if (redirect != null) {
            return redirect;
        }

        redirectAttributes.addFlashAttribute(
                "actionMessage", movementService.turnRight(gameState));
        return "redirect:/game";
    }

    private GameState getGameState(HttpSession session) {
        return (GameState) session.getAttribute(SessionAttributeNames.GAME_STATE);
    }

    private String findRedirectForUnavailableState(GameState gameState) {
        if (gameState == null) {
            return "redirect:/";
        }
        if (gameState.getPlayer().isDefeated()) {
            return "redirect:/game-over";
        }
        if (gameState.isInTown()) {
            return "redirect:/town";
        }
        if (gameState.getCurrentEnemy() != null) {
            return "redirect:/battle";
        }
        return null;
    }
}
