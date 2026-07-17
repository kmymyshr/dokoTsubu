package servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import model.RegisterUserLogic;
import model.User;

/**
 * ユーザー登録画面の入口Servlet。
 *
 * <p>Phase10で登録画面をReact化したため、GETはReactホストJSPを表示するだけに縮小する。
 * POSTは旧JSPフォーム互換として残すが、通常の登録処理はReactから `/api/register` を呼ぶ。</p>
 */
@WebServlet("/Register")
public class Register extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /** React登録画面を読み込むJSPホストへforwardする。 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/WEB-INF/jsp/registerView.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * 旧JSPフォーム互換の登録処理。
     *
     * <p>Phase10以降の通常導線では使わないが、段階移行中に古いフォームからPOSTされても
     * 既存仕様で登録できるよう残している。</p>
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. 文字コードをUTF-8に設定する。
        request.setCharacterEncoding("UTF-8");

        // 2. フォームの入力値を取得する。
        String name = request.getParameter("name");
        String pass = request.getParameter("pass");

        // 3. 入力値が両方とも空でなければ登録処理を行う。
        if (name != null && name.length() != 0 && pass != null && pass.length() != 0) {
            User user = new User(name, pass);
            RegisterUserLogic logic = new RegisterUserLogic();
            boolean isRegister = logic.execute(user);

            // 4. 登録成功なら完了画面へ、失敗なら入力画面へ戻す。
            if (isRegister) {
                RequestDispatcher dispatcher =
                        request.getRequestDispatcher("/WEB-INF/jsp/registerResult.jsp");
                dispatcher.forward(request, response);
            } else {
                request.setAttribute("errorMsg", "登録できませんでした、最初からやり直してね");

                RequestDispatcher dispatcher =
                        request.getRequestDispatcher("/WEB-INF/jsp/registerView.jsp");
                dispatcher.forward(request, response);
            }
        } else {
            // 5. 入力不足ならエラーメッセージを表示する。
            request.setAttribute("errorMsg", "必須項目が未入力です");

            RequestDispatcher dispatcher =
                    request.getRequestDispatcher("/WEB-INF/jsp/registerView.jsp");
            dispatcher.forward(request, response);
        }
    }
}
