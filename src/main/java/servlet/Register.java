package servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ユーザー登録画面を表示するための入口Servlet。
 *
 * <p>Phase10で登録画面はReact化され、登録処理そのものは
 * {@code /api/register} に集約された。Phase15では旧JSPフォーム互換のPOST処理を
 * 撤去し、このServletはReactホストJSPへforwardする責務だけに絞る。</p>
 */
@WebServlet("/Register")
public class Register extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * React登録画面を読み込むJSPホストへforwardする。
     *
     * <p>画面表示後の入力、送信、登録完了メッセージ表示はReactが担当する。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/WEB-INF/jsp/registerView.jsp");
        dispatcher.forward(request, response);
    }
}
