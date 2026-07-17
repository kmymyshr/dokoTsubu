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
 * メイン画面の表示と、旧フォーム経由の投稿処理を担当するサーブレットです。
 * 新しい画面では JavaScript から API を使いますが、古い画面との互換も残しています。
 */
@WebServlet("/Main")
public class Main extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * メイン画面を表示する。
     * ログイン済みでなければトップページへ戻す。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. セッションからログインユーザーを確認する。
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // 2. メイン画面を表示する。
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/main.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * 旧フォームからの投稿処理を行う。
     * 入力内容を検証し、問題なければつぶやきを保存する。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. 文字コードをUTF-8に設定する。
        request.setCharacterEncoding("UTF-8");

        // 2. ログイン済みか確認する。
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // 3. 投稿本文の入力内容を検証する。
        ValidationResult textResult = MutterInputValidator.validateText(request.getParameter("text"));
        if (textResult.valid()) {
            // 4. 問題なければ、つぶやき投稿のロジックを呼び出す。
            if (!new PostMutterLogic().execute(new Mutter(loginUser.getId(), textResult.value()))) {
                session.setAttribute("errorMsg", "つぶやきの投稿に失敗しました");
            }
        } else {
            // 5. 不正な入力ならエラーメッセージを設定する。
            session.setAttribute("errorMsg", textResult.message());
        }

        // 6. 処理後にメイン画面へ戻る。
        response.sendRedirect(request.getContextPath() + "/Main");
    }
}
