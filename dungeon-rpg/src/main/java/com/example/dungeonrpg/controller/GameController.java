package com.example.dungeonrpg.controller;

import com.example.dungeonrpg.model.GameState;
import com.example.dungeonrpg.model.Enemy;
import com.example.dungeonrpg.service.EncounterService;
import com.example.dungeonrpg.service.BattleOutcome;
import com.example.dungeonrpg.service.BattleService;
import com.example.dungeonrpg.service.GameService;
import com.example.dungeonrpg.service.MoveResult;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GameController {
    private static final String GAME_STATE_KEY = "gameState";
    private final GameService gameService;
    private final EncounterService encounterService;
    private final BattleService battleService;

    public GameController(GameService gameService, EncounterService encounterService,
                          BattleService battleService) {
        this.gameService = gameService;
        this.encounterService = encounterService;
        this.battleService = battleService;
    }

    /** トップページを表示します。 */
    @GetMapping("/")
    public String showTopPage() {
        return "index";
    }

    /** 入力された名前で新しいゲーム状態を作り、セッションへ保存します。 */
    @PostMapping("/game/start")
    public String startGame(@RequestParam String playerName,
                            HttpSession session,
                            Model model) {
        String normalizedName = playerName.trim();
        if (normalizedName.isEmpty()) {
            model.addAttribute("errorMessage", "プレイヤー名を入力してください。");
            return "index";
        }

        GameState gameState = new GameState(normalizedName);
        session.setAttribute(GAME_STATE_KEY, gameState);

        // 二重送信を防ぐため、保存後はゲーム画面へリダイレクトします。
        return "redirect:/game";
    }

    /** セッションから現在の状態を読み、ゲーム画面へ渡します。 */
    @GetMapping("/game")
    public String showGame(HttpSession session, Model model) {
        GameState gameState = (GameState) session.getAttribute(GAME_STATE_KEY);
        if (gameState == null) {
            return "redirect:/";
        }
        if (gameState.getPlayer().isDefeated()) {
            return "redirect:/game-over";
        }
        if (gameState.getCurrentEnemy() != null) {
            return "redirect:/battle";
        }

        model.addAttribute("gameState", gameState);
        return "game";
    }

    @PostMapping("/game/move")
    public String moveForward(HttpSession session, RedirectAttributes redirectAttributes) {
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

        MoveResult moveResult = gameService.moveForward(gameState);
        redirectAttributes.addFlashAttribute("actionMessage", moveResult.message());

        // 壁にぶつかった場合は前進していないため、遭遇判定をしません。
        if (moveResult.moved()) {
            Optional<Enemy> encounteredEnemy = encounterService.findEncounter();
            if (encounteredEnemy.isPresent()) {
                gameState.startBattle(encounteredEnemy.get());
                return "redirect:/battle";
            }
        }
        return "redirect:/game";
    }

    @PostMapping("/game/turn-left")
    public String turnLeft(HttpSession session, RedirectAttributes redirectAttributes) {
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

        String message = gameService.turnLeft(gameState);
        redirectAttributes.addFlashAttribute("actionMessage", message);
        return "redirect:/game";
    }

    @PostMapping("/game/turn-right")
    public String turnRight(HttpSession session, RedirectAttributes redirectAttributes) {
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

        String message = gameService.turnRight(gameState);
        redirectAttributes.addFlashAttribute("actionMessage", message);
        return "redirect:/game";
    }

    /** 遭遇した敵の情報を戦闘画面に表示します。 */
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

    /** 攻撃後の勝敗に応じて、次に表示する画面を決めます。 */
    @PostMapping("/battle/attack")
    public String attack(HttpSession session, RedirectAttributes redirectAttributes) {
        GameState gameState = getGameState(session);
        if (gameState == null) {
            return "redirect:/";
        }
        if (gameState.getCurrentEnemy() == null) {
            return "redirect:/game";
        }

        BattleOutcome outcome = battleService.attack(gameState);
        if (outcome == BattleOutcome.VICTORY) {
            redirectAttributes.addFlashAttribute(
                    "actionMessage", "敵に勝利し、経験値を獲得しました！");
            return "redirect:/game";
        }
        if (outcome == BattleOutcome.DEFEAT) {
            return "redirect:/game-over";
        }
        return "redirect:/battle";
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
        return (GameState) session.getAttribute(GAME_STATE_KEY);
    }
}
