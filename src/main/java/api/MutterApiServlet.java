package api;

import java.io.IOException;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;
import com.example.dokotsubu.service.SocialService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ApiErrorResponse;
import dto.MutterListResponse;
import dto.MutterResponse;
import dto.MutterWriteRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Mutter;
import model.MutterFeedPage;
import model.User;
import util.ObjectMapperFactory;
import validation.MutterInputValidator;
import validation.ValidationResult;

/**
 * 投稿（つぶやき）を扱うREST API。
 *
 * <p>旧JSP画面からReactへ段階移行するために追加したAPIで、一覧取得、詳細取得、作成、
 * 更新、削除をJSONで提供する。Phase5では投稿取得や更新の実体を {@link MutterService} /
 * {@link SocialService} へ委譲し、ServletはHTTPリクエスト/レスポンスの変換に集中させている。</p>
 */
@WebServlet("/api/mutters/*")
public class MutterApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    /**
     * 投稿一覧または投稿詳細を取得する。
     *
     * <p>`/api/mutters` は一覧、`/api/mutters/{id}` は個別投稿として扱う。
     * 一覧ではReact側の追加読み込みに必要なカーソル情報も返す。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // APIはログインユーザーのいいね/フォロー状態も返すため、最初に認証状態を確認する。
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }

        // URLにIDがある場合は個別投稿取得として処理し、不正なパスは明示的なエラーにする。
        boolean individualResource = hasResourceId(request);
        Integer id = parseResourceId(request, response);
        if (individualResource && id == null) {
            return;
        }

        MutterService mutters = ApplicationServiceBridge.mutters();
        SocialService social = ApplicationServiceBridge.social();

        if (id != null) {
            // 詳細レスポンスには、投稿本体に加えて表示用のソーシャル状態を含める。
            Mutter mutter = mutters.findById(id);
            if (mutter == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND,
                        "MUTTER_NOT_FOUND", "指定された投稿は存在しません");
                return;
            }
            int likeCount = social.countLikes(id);
            boolean likedByMe = social.hasLiked(id, loginUser.getId());
            boolean followedByMe = social.isFollowing(loginUser.getId(), mutter.getUserId());
            writeJson(response, HttpServletResponse.SC_OK, MutterResponse.from(mutter, likeCount, likedByMe, followedByMe));
            return;
        }

        // 一覧取得では、検索キーワード、カーソル、取得件数を検証してからServiceへ渡す。
        ValidationResult keywordResult = MutterInputValidator.validateKeyword(request.getParameter("keyword"));
        if (!keywordResult.valid()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    keywordResult.code(), keywordResult.message());
            return;
        }
        String keyword = keywordResult.value();

        Integer cursor = parseOptionalPositiveInt(request.getParameter("cursor"));
        if (request.getParameter("cursor") != null && cursor == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_CURSOR", "cursorには正の整数を指定してください");
            return;
        }

        Integer requestedLimit = parseOptionalPositiveInt(request.getParameter("limit"));
        if (request.getParameter("limit") != null && requestedLimit == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_LIMIT", "limitには正の整数を指定してください");
            return;
        }
        int limit = requestedLimit == null ? DEFAULT_LIMIT : Math.min(requestedLimit, MAX_LIMIT);

        MutterFeedPage page = mutters.findFeedPage(keyword, cursor, limit, loginUser.getId());
        java.util.List<MutterResponse> responseList = page.items().stream()
                .map(MutterResponse::from)
                .toList();
        writeJson(response, HttpServletResponse.SC_OK, MutterListResponse.from(responseList, page));
    }

    /**
     * 新しい投稿を作成する。
     *
     * <p>入力検証はServletで行い、永続化はServiceへ委譲する。作成後はReact側が即座に
     * 画面へ反映できるよう、作成済み投稿をJSONで返す。</p>
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }
        // Phase20: 旧 /LikeMutter のいいね切り替えを、投稿APIのサブリソースへ統合する。
        if (isLikeResource(request)) {
            toggleLike(request, response, loginUser);
            return;
        }
        if (hasResourceId(request)) {
            writeError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "METHOD_NOT_ALLOWED", "個別リソースにPOSTは使用できません");
            return;
        }

        MutterWriteRequest body = readBody(request, response);
        if (body == null) return;
        ValidationResult textResult = validateText(body.text(), response);
        if (!textResult.valid()) return;

        Mutter created = ApplicationServiceBridge.mutters().createAndReturn(
                new Mutter(loginUser.getId(), textResult.value()));
        if (created == null) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "CREATE_FAILED", "投稿の作成に失敗しました");
            return;
        }

        response.setHeader("Location", request.getContextPath() + "/api/mutters/" + created.getId());
        // 新規作成直後は、いいね/フォロー状態がまだ付かないためfalse/0で返す。
        writeJson(response, HttpServletResponse.SC_CREATED, MutterResponse.from(created, 0, false, false));
    }

    /**
     * 投稿に対するいいね状態を切り替える。
     *
     * <p>Phase20で旧 {@code /LikeMutter} から移した処理。投稿に従属する操作を
     * {@code /api/mutters/{id}/like} に集約し、React側のAPI境界を分かりやすくする。</p>
     */
    private void toggleLike(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws IOException {
        Integer id = parseLikeResourceId(request, response);
        if (id == null) {
            return;
        }

        Mutter target = ApplicationServiceBridge.mutters().findById(id);
        if (target == null) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "MUTTER_NOT_FOUND", "指定された投稿は存在しません");
            return;
        }
        if (target.getUserId() == loginUser.getId()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "CANNOT_LIKE_OWN_MUTTER", "自分の投稿にはいいねできません");
            return;
        }

        SocialService social = ApplicationServiceBridge.social();
        boolean liked = social.toggleLike(id, loginUser.getId());
        int count = social.countLikes(id);
        writeJson(response, HttpServletResponse.SC_OK,
                java.util.Map.of("liked", liked, "count", count));
    }

    /**
     * 既存投稿を更新する。
     *
     * <p>投稿者本人のみ更新可能。versionを使った楽観ロックにより、別タブや別端末で
     * 先に更新された投稿を上書きしないようにする。</p>
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }
        Integer id = requireResourceId(request, response);
        if (id == null) {
            return;
        }

        MutterWriteRequest body = readBody(request, response);
        if (body == null) return;
        ValidationResult textResult = validateText(body.text(), response);
        if (!textResult.valid()) return;
        if (body.version() == null || body.version() < 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_VERSION", "更新時は0以上のversionが必要です");
            return;
        }

        MutterService mutters = ApplicationServiceBridge.mutters();
        SocialService social = ApplicationServiceBridge.social();

        // 更新前に存在確認と所有者確認を行い、Repository側の更新条件を分かりやすく保つ。
        Mutter current = mutters.findById(id);
        if (current == null) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "MUTTER_NOT_FOUND", "指定された投稿は存在しません");
            return;
        }
        if (current.getUserId() != loginUser.getId()) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "FORBIDDEN", "他のユーザーの投稿は更新できません");
            return;
        }

        Mutter updated = new Mutter(id, loginUser.getId(), loginUser.getName(),
                textResult.value(), body.version());
        if (!mutters.update(updated)) {
            writeError(response, HttpServletResponse.SC_CONFLICT,
                    "UPDATE_CONFLICT", "他の操作で更新されています。最新データを取得してください");
            return;
        }

        // 更新後のversionを含む最新状態を返すため、再取得してからレスポンスを組み立てる。
        Mutter refreshed = mutters.findById(id);
        int likeCount = social.countLikes(id);
        boolean likedByMe = social.hasLiked(id, loginUser.getId());
        boolean followedByMe = social.isFollowing(loginUser.getId(), refreshed.getUserId());
        writeJson(response, HttpServletResponse.SC_OK,
            MutterResponse.from(refreshed, likeCount, likedByMe, followedByMe));
    }

    /**
     * 既存投稿を削除する。
     *
     * <p>更新と同じく投稿者本人のみ削除可能。成功時はREST APIの慣例に合わせて204を返す。</p>
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }
        Integer id = requireResourceId(request, response);
        if (id == null) {
            return;
        }

        MutterService mutters = ApplicationServiceBridge.mutters();
        Mutter current = mutters.findById(id);
        if (current == null) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "MUTTER_NOT_FOUND", "指定された投稿は存在しません");
            return;
        }
        if (current.getUserId() != loginUser.getId()) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "FORBIDDEN", "他のユーザーの投稿は削除できません");
            return;
        }

        if (!mutters.delete(id, loginUser.getId())) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "DELETE_FAILED", "投稿の削除に失敗しました");
            return;
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /** API共通のログイン確認。未ログインならJSONエラーを返す。 */
    private User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED", "ログインが必要です");
        }
        return loginUser;
    }

    /** JSONリクエストボディをDTOへ変換し、壊れたJSONは400で返す。 */
    private MutterWriteRequest readBody(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            return OBJECT_MAPPER.readValue(request.getReader(), MutterWriteRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_JSON", "JSONの形式が不正です");
            return null;
        }
    }

    /** 投稿本文の入力検証を共通化し、エラー時はAPI形式で返す。 */
    private ValidationResult validateText(String text, HttpServletResponse response)
            throws IOException {
        ValidationResult result = MutterInputValidator.validateText(text);
        if (!result.valid()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    result.code(), result.message());
        }
        return result;
    }

    /** `/api/mutters/{id}` のID部分を解析する。 */
    private Integer parseResourceId(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String path = request.getPathInfo();
        if (path == null || path.equals("/") || path.isBlank()) {
            return null;
        }
        if (!path.matches("/\\d+/?")) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "RESOURCE_NOT_FOUND", "指定されたAPIリソースは存在しません");
            return null;
        }
        try {
            int id = Integer.parseInt(path.replace("/", ""));
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_ID", "IDには正の整数を指定してください");
            return null;
        }
    }

    /** `/api/mutters/{id}/like` の投稿ID部分を安全に取り出す。 */
    private Integer parseLikeResourceId(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String path = request.getPathInfo();
        if (path == null || !path.matches("/\\d+/like/?")) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "RESOURCE_NOT_FOUND", "指定されたAPIリソースは存在しません");
            return null;
        }
        String idPart = path.replaceFirst("^/", "").replaceFirst("/like/?$", "");
        try {
            int id = Integer.parseInt(idPart);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_ID", "IDには正の整数を指定してください");
            return null;
        }
    }

    /** 更新/削除のようにID必須のAPIで、ID未指定を分かりやすく400にする。 */
    private Integer requireResourceId(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!hasResourceId(request)) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "ID_REQUIRED", "URLに対象の投稿IDを指定してください");
            return null;
        }
        return parseResourceId(request, response);
    }

    /** パスに投稿IDらしき要素があるかを判定する。 */
    private boolean hasResourceId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path != null && !path.equals("/") && !path.isBlank();
    }

    /** Phase20で追加した、投稿いいね用サブリソースかどうかを判定する。 */
    private boolean isLikeResource(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path != null && path.matches("/\\d+/like/?");
    }

    /** cursor/limitのような任意の正整数パラメータを安全に読む。 */
    private Integer parseOptionalPositiveInt(String value) {
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

    /** APIレスポンスをJSONとして書き込む共通処理。 */
    private void writeJson(HttpServletResponse response, int status, Object body)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }

    /** エラー時もフロントエンドが扱いやすい共通JSON形式で返す。 */
    private void writeError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        writeJson(response, status, new ApiErrorResponse(status, code, message));
    }
}
