package com.example.dokotsubu.web;

import com.example.dokotsubu.service.UserService;
import jakarta.servlet.http.HttpSession;
import model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

/**
 * ReactホストJSPを表示する画面入口Controller。
 *
 * <p>Phase21で、旧Servletパッケージに残っていた画面表示専用ServletをSpring MVCへ移行するために追加した。
 * 各画面の実データ取得や更新処理はReact + JSON APIへ移行済みなので、このControllerはログイン確認、
 * URLパラメータ検証、JSPホストへのforwardだけを担当する。</p>
 */
@Controller
public class PageHostController {
    private final UserService users;

    public PageHostController(UserService users) {
        this.users = users;
    }

    /**
     * ログイン後のメイン画面を表示する。
     *
     * <p>投稿一覧、投稿作成、検索、編集、削除はReactと {@code /api/mutters} が担当するため、
     * ここではセッション上のログインユーザー確認後にReactホストJSPへforwardする。</p>
     */
    @GetMapping("/Main")
    String main(HttpSession session) {
        if (currentUser(session) == null) {
            return "redirect:/";
        }
        return "forward:/WEB-INF/jsp/main.jsp";
    }

    /**
     * ユーザー登録画面を表示する。
     *
     * <p>登録処理は {@code /api/register} に集約済みなので、未ログインでも参照できるReactホストJSPだけを返す。</p>
     */
    @GetMapping("/Register")
    String register() {
        return "forward:/WEB-INF/jsp/registerView.jsp";
    }

    /**
     * プロフィール画面を表示する。
     *
     * <p>対象ユーザーIDの妥当性と存在だけを確認し、詳細データはReactが {@code /api/profile} から取得する。</p>
     */
    @GetMapping("/Profile")
    String profile(
            @RequestParam("userId") String userId,
            HttpSession session,
            Model model) {
        if (currentUser(session) == null) {
            return "redirect:/";
        }

        User profileUser = requireExistingUser(userId);
        model.addAttribute("targetUserId", profileUser.getId());
        return "forward:/WEB-INF/jsp/profile.jsp";
    }

    /**
     * フォロワー一覧画面を表示する。
     *
     * <p>一覧データそのものはReactが {@code /api/follows?type=followers} から取得するため、
     * JSPには対象ユーザーだけを渡す。</p>
     */
    @GetMapping("/FollowerList")
    String followers(
            @RequestParam("userId") String userId,
            HttpSession session,
            Model model) {
        if (currentUser(session) == null) {
            return "redirect:/";
        }

        model.addAttribute("targetUser", requireExistingUser(userId));
        return "forward:/WEB-INF/jsp/followerList.jsp";
    }

    /**
     * フォロー中一覧画面を表示する。
     *
     * <p>フォロワー一覧と同じく、ControllerはホストJSPへ対象ユーザーを渡すだけにして、
     * 一覧描画はReact + {@code /api/follows?type=following} に任せる。</p>
     */
    @GetMapping("/FollowingList")
    String following(
            @RequestParam("userId") String userId,
            HttpSession session,
            Model model) {
        if (currentUser(session) == null) {
            return "redirect:/";
        }

        model.addAttribute("targetUser", requireExistingUser(userId));
        return "forward:/WEB-INF/jsp/followingList.jsp";
    }

    /** 旧Servletと同じく、段階移行中はセッション上のloginUserをログイン判定の軸にする。 */
    private User currentUser(HttpSession session) {
        return session == null ? null : (User) session.getAttribute("loginUser");
    }

    /** URLパラメータのユーザーIDを正の整数として検証し、存在するユーザーだけを返す。 */
    private User requireExistingUser(String userId) {
        Integer id = parsePositiveInteger(userId);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userIdが不正です");
        }

        User user = users.findById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定されたユーザーは存在しません");
        }
        return user;
    }

    /** URLパラメータのIDを安全に読むため、正の整数だけを受け入れる。 */
    private Integer parsePositiveInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int number = Integer.parseInt(value);
            return number > 0 ? number : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
