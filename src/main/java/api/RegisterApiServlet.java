package api;

import java.io.IOException;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ApiErrorResponse;
import dto.RegisterRequest;
import dto.RegisterResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.ObjectMapperFactory;

/**
 * ユーザー登録を扱うREST API。
 *
 * <p>Phase10では登録画面をReactへ移すため、登録処理を画面Servletから切り離してJSON API化する。
 * 実際の登録処理とパスワードハッシュ化は {@link UserService} に委譲し、このServletは
 * HTTP/JSONの変換と入力エラーのステータス整理に集中する。</p>
 */
@WebServlet("/api/register")
public class RegisterApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    /**
     * JSONのユーザー名/パスワードを受け取り、新規ユーザーを登録する。
     *
     * <p>登録ページは未ログイン状態で使うため、SecurityConfigでこのAPIだけ公開している。
     * ただしPOSTなので、Spring SecurityのCSRFトークンは通常どおり必要になる。</p>
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RegisterRequest body = readBody(request, response);
        if (body == null) {
            return;
        }

        String name = normalize(body.name());
        String pass = normalize(body.pass());
        if (name.isBlank() || pass.isBlank()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "REQUIRED_FIELD_MISSING", "ユーザー名とパスワードを入力してください");
            return;
        }

        UserService users = ApplicationServiceBridge.users();
        if (users.findByName(name) != null) {
            writeError(response, HttpServletResponse.SC_CONFLICT,
                    "USER_ALREADY_EXISTS", "同じユーザー名は既に登録されています");
            return;
        }

        boolean registered = users.register(new User(name, pass));
        if (!registered) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "REGISTER_FAILED", "登録に失敗しました。時間をおいて再度お試しください");
            return;
        }

        writeJson(response, HttpServletResponse.SC_CREATED,
                new RegisterResponse(name, "ユーザー登録が完了しました", request.getContextPath() + "/"));
    }

    /** 空白だけの入力を弾けるよう、nullを空文字に正規化して前後の空白を取り除く。 */
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    /** JSON本文をRegisterRequestへ変換し、壊れたJSONは400として返す。 */
    private RegisterRequest readBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            return OBJECT_MAPPER.readValue(request.getReader(), RegisterRequest.class);
        } catch (IOException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_JSON", "リクエストJSONが不正です");
            return null;
        }
    }

    /** 成功レスポンスは常にUTF-8のJSONで返す。 */
    private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }

    /** React側で扱いやすい共通エラー形式にそろえる。 */
    private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
        writeJson(response, status, new ApiErrorResponse(status, code, message));
    }
}
