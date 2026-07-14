package api;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dao.MutterDAO;
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
import model.GetMutterListLogic;
import model.Mutter;
import model.MutterFeedPage;
import model.User;
import util.ObjectMapperFactory;
import validation.MutterInputValidator;
import validation.ValidationResult;
import dao.LikeDAO;

/**
 * つぶやきを扱う REST API です。
 * 一覧取得・作成・更新・削除の4つの操作を受け付けます。
 */
@WebServlet("/api/mutters/*")
public class MutterApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    /**
     * つぶやきの一覧取得または個別取得を行う。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. ログインしているか確認する。
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }

        // 2. URL に ID が含まれているか確認する。
        boolean individualResource = hasResourceId(request);
        Integer id = parseResourceId(request, response);
        if (individualResource && id == null) {
            return;
        }
        if (id != null) {
            // 3. 個別のつぶやきを取得して JSON で返す。
            Mutter mutter = new MutterDAO().findById(id);
            if (mutter == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND,
                        "MUTTER_NOT_FOUND", "指定されたつぶやきは存在しません");
                return;
            }
            int likeCount = new LikeDAO().countLikes(id);
            boolean likedByMe = new LikeDAO().hasLiked(id, loginUser.getId());
            boolean followedByMe = new dao.FollowDAO().isFollowing(loginUser.getId(), mutter.getUserId());
            writeJson(response, HttpServletResponse.SC_OK, MutterResponse.from(mutter, likeCount, likedByMe, followedByMe));
            return;
        }

        // 4. 一覧検索用の条件を読み取り、つぶやき一覧を返す。
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
        MutterFeedPage page = new GetMutterListLogic()
                .executeFeed(keyword, cursor, limit, loginUser.getId());
        java.util.List<dto.MutterResponse> responseList = page.items().stream()
                .map(dto.MutterResponse::from)
                .toList();
        writeJson(response, HttpServletResponse.SC_OK, MutterListResponse.from(responseList, page));
    }

    /**
     * 新しいつぶやきを作成する。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. ログインしているか確認する。
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }
        if (hasResourceId(request)) {
            writeError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "METHOD_NOT_ALLOWED", "個別リソースにPOSTは使用できません");
            return;
        }

        // 2. リクエストボディから投稿内容を読み取る。
        MutterWriteRequest body = readBody(request, response);
        if (body == null) return;
        ValidationResult textResult = validateText(body.text(), response);
        if (!textResult.valid()) return;

        // 3. データベースに保存し、作成結果を返す。
        Mutter created = new MutterDAO().createAndReturn(
                new Mutter(loginUser.getId(), textResult.value()));
        if (created == null) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "CREATE_FAILED", "つぶやきの作成に失敗しました");
            return;
        }

        response.setHeader("Location", request.getContextPath() + "/api/mutters/" + created.getId());
        // 新規作成直後はいいね・フォローはないため false を返す
        writeJson(response, HttpServletResponse.SC_CREATED, MutterResponse.from(created, 0, false, false));
    }

    /**
     * 既存のつぶやきを更新する。
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. ログインしているか確認する。
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }
        Integer id = requireResourceId(request, response);
        if (id == null) {
            return;
        }

        // 2. 更新内容を読み取り、入力内容を確認する。
        MutterWriteRequest body = readBody(request, response);
        if (body == null) return;
        ValidationResult textResult = validateText(body.text(), response);
        if (!textResult.valid()) return;
        if (body.version() == null || body.version() < 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_VERSION", "更新時は0以上のversionが必要です");
            return;
        }

        // 3. 対象つぶやきが存在し、自分のものか確認する。
        Mutter current = new MutterDAO().findById(id);
        if (current == null) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "MUTTER_NOT_FOUND", "指定されたつぶやきは存在しません");
            return;
        }
        if (current.getUserId() != loginUser.getId()) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "FORBIDDEN", "他のユーザーのつぶやきは更新できません");
            return;
        }

        // 4. 更新を実行して結果を返す。
        Mutter updated = new Mutter(id, loginUser.getId(), loginUser.getName(),
                textResult.value(), body.version());
        if (!new MutterDAO().update(updated)) {
            writeError(response, HttpServletResponse.SC_CONFLICT,
                    "UPDATE_CONFLICT", "他の操作で更新されています。最新データを取得してください");
            return;
        }
        Mutter refreshed = new MutterDAO().findById(id);
        int likeCount = new LikeDAO().countLikes(id);
        boolean likedByMe = new LikeDAO().hasLiked(id, loginUser.getId());
        boolean followedByMe = new dao.FollowDAO().isFollowing(loginUser.getId(), refreshed.getUserId());
        writeJson(response, HttpServletResponse.SC_OK,
            MutterResponse.from(refreshed, likeCount, likedByMe, followedByMe));
    }

    /**
     * つぶやきを削除する。
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. ログインしているか確認する。
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }
        Integer id = requireResourceId(request, response);
        if (id == null) {
            return;
        }

        // 2. 対象つぶやきが存在し、自分のものか確認する。
        Mutter current = new MutterDAO().findById(id);
        if (current == null) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "MUTTER_NOT_FOUND", "指定されたつぶやきは存在しません");
            return;
        }
        if (current.getUserId() != loginUser.getId()) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "FORBIDDEN", "他のユーザーのつぶやきは削除できません");
            return;
        }

        // 3. 削除を実行する。
        if (!new MutterDAO().delete(id, loginUser.getId())) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "DELETE_FAILED", "つぶやきの削除に失敗しました");
            return;
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

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

    private ValidationResult validateText(String text, HttpServletResponse response)
            throws IOException {
        ValidationResult result = MutterInputValidator.validateText(text);
        if (!result.valid()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    result.code(), result.message());
        }
        return result;
    }

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

    private Integer requireResourceId(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!hasResourceId(request)) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "ID_REQUIRED", "URLに対象のつぶやきIDを指定してください");
            return null;
        }
        return parseResourceId(request, response);
    }

    private boolean hasResourceId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path != null && !path.equals("/") && !path.isBlank();
    }

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

    private void writeJson(HttpServletResponse response, int status, Object body)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }

    private void writeError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        writeJson(response, status, new ApiErrorResponse(status, code, message));
    }
}
