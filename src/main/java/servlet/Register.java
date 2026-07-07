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

@WebServlet("/Register")
public class Register extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * ユーザー登録画面を表示する。
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. 登録画面の JSP を表示する。
        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/WEB-INF/jsp/registerView.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * フォームから送られた内容で新規登録を行う。
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
