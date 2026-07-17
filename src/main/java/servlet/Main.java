package servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

/**
 * ログイン後のメイン画面を表示するための入口Servlet。
 *
 * <p>投稿一覧、投稿作成、検索、編集、削除はReact + {@code /api/mutters} に移行済み。
 * Phase18では旧JSPフォーム互換のPOST処理を撤去し、このServletは認証済みユーザーを
 * ReactホストJSPへforwardする責務だけに絞る。</p>
 */
@WebServlet("/Main")
public class Main extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * 認証済みユーザーにReactメイン画面を表示する。
     *
     * <p>未ログインの場合はSpring Securityのログイン画面であるトップへ戻す。
     * 実際の投稿データ取得と画面操作は、読み込まれたReactアプリがAPI経由で行う。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/main.jsp");
        dispatcher.forward(request, response);
    }
}
