package servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Mutter;
import model.PostMutterLogic;
import model.User;
import validation.MutterInputValidator;
import validation.ValidationResult;

/**
 * ログイン後のメイン画面を表示するServlet。
 *
 * <p>Phase6では、投稿一覧・投稿作成・検索・編集・削除の画面操作をReact側へ寄せている。
 * このServletは認証済みユーザーだけを通し、Reactアプリを読み込むJSPへforwardする
 * 「画面ホスト」としての役割を持つ。</p>
 *
 * <p>POST処理は旧JSPフォーム互換のために残している。React移行が完了した画面操作は
 * `/api/mutters` を使うが、古いフォームから送信されても動作が壊れないようにしている。</p>
 */
@WebServlet("/Main")
public class Main extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /** 認証済みユーザーにReactホストJSPを表示する。 */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // main.jspはJSPでHTMLを作り込まず、Reactのroot要素とバンドルを読み込むだけにする。
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/main.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * 旧JSPフォームから投稿された場合の互換処理。
     *
     * <p>React画面では投稿作成APIを使うため通常は通らないが、段階移行中に旧導線が残っても
     * 既存仕様を維持できるようにしている。</p>
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        ValidationResult textResult = MutterInputValidator.validateText(request.getParameter("text"));
        if (textResult.valid()) {
            if (!new PostMutterLogic().execute(new Mutter(loginUser.getId(), textResult.value()))) {
                session.setAttribute("errorMsg", "投稿に失敗しました");
            }
        } else {
            session.setAttribute("errorMsg", textResult.message());
        }

        response.sendRedirect(request.getContextPath() + "/Main");
    }
}
