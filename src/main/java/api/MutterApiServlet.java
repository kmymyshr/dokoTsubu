package api;

import java.io.IOException;
import java.util.List;

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
import model.MutterPage;
import model.User;
import util.ObjectMapperFactory;

/** つぶやきをリソースとして扱うREST APIです。 */
@WebServlet("/api/mutters/*")
public class MutterApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final int MAX_TEXT_LENGTH = 255;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }

        boolean individualResource = hasResourceId(request);
        Integer id = parseResourceId(request, response);
        if (individualResource && id == null) {
            return;
        }
        if (id != null) {
            Mutter mutter = new MutterDAO().findById(id);
            if (mutter == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND,
                        "MUTTER_NOT_FOUND", "指定されたつぶやきは存在しません");
                return;
            }
            writeJson(response, HttpServletResponse.SC_OK, MutterResponse.from(mutter));
            return;
        }

        String keyword = normalizeKeyword(request.getParameter("keyword"));
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
        MutterPage page = new GetMutterListLogic().execute(keyword, cursor, limit);
        writeJson(response, HttpServletResponse.SC_OK, MutterListResponse.from(page));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }
        if (hasResourceId(request)) {
            writeError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "METHOD_NOT_ALLOWED", "個別リソースにPOSTは使用できません");
            return;
        }

        MutterWriteRequest body = readBody(request, response);
        if (body == null || !validateText(body.text(), response)) {
            return;
        }
        Mutter created = new MutterDAO().createAndReturn(
                new Mutter(loginUser.getId(), body.text().trim()));
        if (created == null) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "CREATE_FAILED", "つぶやきの作成に失敗しました");
            return;
        }

        response.setHeader("Location", request.getContextPath() + "/api/mutters/" + created.getId());
        writeJson(response, HttpServletResponse.SC_CREATED, MutterResponse.from(created));
    }

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
        if (body == null || !validateText(body.text(), response)) {
            return;
        }
        if (body.version() == null || body.version() < 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_VERSION", "更新時は0以上のversionが必要です");
            return;
        }

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

        Mutter updated = new Mutter(id, loginUser.getId(), loginUser.getName(),
                body.text().trim(), body.version());
        if (!new MutterDAO().update(updated)) {
            writeError(response, HttpServletResponse.SC_CONFLICT,
                    "UPDATE_CONFLICT", "他の操作で更新されています。最新データを取得してください");
            return;
        }
        writeJson(response, HttpServletResponse.SC_OK,
                MutterResponse.from(new MutterDAO().findById(id)));
    }

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

    private boolean validateText(String text, HttpServletResponse response) throws IOException {
        if (text == null || text.isBlank()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "TEXT_REQUIRED", "つぶやき本文を入力してください");
            return false;
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "TEXT_TOO_LONG", "つぶやき本文は255文字以内で入力してください");
            return false;
        }
        return true;
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

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
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