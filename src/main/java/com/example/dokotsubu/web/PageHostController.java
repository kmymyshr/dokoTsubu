package com.example.dokotsubu.web;

import com.example.dokotsubu.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import model.User;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;
import org.springframework.security.web.csrf.CsrfToken;

/**
 * ReactホストHTMLを表示する画面入口Controller。
 *
 * <p>Phase21で画面表示専用ServletをSpring MVCへ移行し、Phase22でJSPホストを廃止した。
 * 各画面の実データ取得や更新処理はReact + JSON APIへ移行済みなので、このControllerはログイン確認、
 * URLパラメータ検証、Reactを起動する最小HTMLの生成だけを担当する。</p>
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
    ResponseEntity<String> main(HttpSession session, HttpServletRequest request) {
        if (currentUser(session) == null) {
            return redirectToRoot(request);
        }
        return reactHost("dokoTsubu", "画面を読み込み中です...", request, attrs());
    }

    /**
     * ユーザー登録画面を表示する。
     *
     * <p>登録処理は {@code /api/register} に集約済みなので、未ログインでも参照できるReactホストJSPだけを返す。</p>
     */
    @GetMapping("/Register")
    ResponseEntity<String> register(HttpServletRequest request) {
        return reactHost("ユーザー登録", "登録画面を読み込み中です...", request,
                attrs("data-react-page", "register"));
    }

    /**
     * ログイン画面を表示する。
     *
     * <p>Phase22で {@code index.jsp} を廃止したため、Spring SecurityのログインページURLは維持しつつ、
     * Reactログイン画面を起動するHTMLをControllerから直接返す。</p>
     */
    @GetMapping("/index.jsp")
    ResponseEntity<String> login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            HttpServletRequest request) {
        return reactHost("どこつぶ", "ログイン画面を読み込み中です...", request,
                attrs(
                        "data-react-page", "login",
                        "data-login-error", Boolean.toString("1".equals(error)),
                        "data-logged-out", Boolean.toString("1".equals(logout))));
    }

    /**
     * プロフィール画面を表示する。
     *
     * <p>対象ユーザーIDの妥当性と存在だけを確認し、詳細データはReactが {@code /api/profile} から取得する。</p>
     */
    @GetMapping("/Profile")
    ResponseEntity<String> profile(
            @RequestParam("userId") String userId,
            HttpSession session,
        HttpServletRequest request) {
        if (currentUser(session) == null) {
            return redirectToRoot(request);
        }

        User profileUser = requireExistingUser(userId);
        return reactHost("プロフィール", "プロフィールを読み込み中です...", request,
                attrs(
                        "data-react-page", "profile",
                        "data-target-user-id", String.valueOf(profileUser.getId())));
    }

    /**
     * フォロワー一覧画面を表示する。
     *
     * <p>一覧データそのものはReactが {@code /api/follows?type=followers} から取得するため、
     * JSPには対象ユーザーだけを渡す。</p>
     */
    @GetMapping("/FollowerList")
    ResponseEntity<String> followers(
            @RequestParam("userId") String userId,
            HttpSession session,
        HttpServletRequest request) {
        if (currentUser(session) == null) {
            return redirectToRoot(request);
        }

        User targetUser = requireExistingUser(userId);
        return reactHost("フォロワー一覧", "フォロワー一覧を読み込み中です...", request,
                attrs(
                        "data-react-page", "follow-list",
                        "data-follow-list-type", "followers",
                        "data-target-user-id", String.valueOf(targetUser.getId())));
    }

    /**
     * フォロー中一覧画面を表示する。
     *
     * <p>フォロワー一覧と同じく、ControllerはホストJSPへ対象ユーザーを渡すだけにして、
     * 一覧描画はReact + {@code /api/follows?type=following} に任せる。</p>
     */
    @GetMapping("/FollowingList")
    ResponseEntity<String> following(
            @RequestParam("userId") String userId,
            HttpSession session,
        HttpServletRequest request) {
        if (currentUser(session) == null) {
            return redirectToRoot(request);
        }

        User targetUser = requireExistingUser(userId);
        return reactHost("フォロー中一覧", "フォロー中一覧を読み込み中です...", request,
                attrs(
                        "data-react-page", "follow-list",
                        "data-follow-list-type", "following",
                        "data-target-user-id", String.valueOf(targetUser.getId())));
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

    /** 画面ControllerからのリダイレクトをResponseEntityで表現する。 */
    private ResponseEntity<String> redirectToRoot(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", request.getContextPath() + "/")
                .build();
    }

    /**
     * Reactを起動する共通HTMLを生成する。
     *
     * <p>Phase22でJSPホストをなくすため、全画面に共通だったCSS/JS読み込み、contextPath、CSRF属性を
     * Controller側で組み立てる。画面固有のdata属性は {@code extraAttributes} で追加する。</p>
     */
    private ResponseEntity<String> reactHost(
            String title,
            String loadingMessage,
            HttpServletRequest request,
            java.util.Map<String, String> extraAttributes) {
        String contextPath = request.getContextPath();
        java.util.Map<String, String> attributes = new java.util.LinkedHashMap<>();
        attributes.put("data-context-path", contextPath);
        CsrfToken token = csrfToken(request);
        if (token != null) {
            attributes.put("data-csrf-token", token.getToken());
            attributes.put("data-csrf-header", token.getHeaderName());
            attributes.put("data-csrf-param", token.getParameterName());
        }
        attributes.putAll(extraAttributes);

        String html = """
                <!DOCTYPE html>
                <html lang="ja">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>%s</title>
                    <link rel="stylesheet" href="%s/react/assets/main.css">
                </head>
                <body %s>
                    <div id="root"><p>%s</p></div>
                    <noscript>この画面を利用するにはJavaScriptを有効にしてください。</noscript>
                    <script type="module" src="%s/react/assets/main.js"></script>
                </body>
                </html>
                """.formatted(
                escape(title),
                escape(contextPath),
                renderAttributes(attributes),
                escape(loadingMessage),
                escape(contextPath));

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /** Spring Securityがrequest属性へ公開したCSRFトークンを取得する。 */
    private CsrfToken csrfToken(HttpServletRequest request) {
        Object token = request.getAttribute(CsrfToken.class.getName());
        if (token instanceof CsrfToken csrfToken) {
            return csrfToken;
        }
        token = request.getAttribute("_csrf");
        return token instanceof CsrfToken csrfToken ? csrfToken : null;
    }

    /** data属性を安全にHTMLへ出力する。 */
    private String renderAttributes(java.util.Map<String, String> attributes) {
        return attributes.entrySet().stream()
                .map(entry -> entry.getKey() + "=\"" + escape(entry.getValue()) + "\"")
                .collect(java.util.stream.Collectors.joining("\n      "));
    }

    /** 小さなHTML生成でも属性値を壊さないよう、SpringのHTMLエスケープを必ず通す。 */
    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }

    /** 可変長で画面固有のdata属性を組み立てる。 */
    private java.util.Map<String, String> attrs(String... pairs) {
        java.util.Map<String, String> result = new java.util.LinkedHashMap<>();
        for (int index = 0; index + 1 < pairs.length; index += 2) {
            result.put(pairs[index], pairs[index + 1]);
        }
        return result;
    }
}
