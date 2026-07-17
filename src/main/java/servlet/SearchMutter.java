package servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Mutter;
import model.SearchMutterLogic;
import model.User;
import validation.MutterInputValidator;
import validation.ValidationResult;

/**
 * 検索機能を担当するサーブレットです。
 * ユーザーが入力したキーワードでつぶやきを検索し、結果を画面に渡します。
 */
@WebServlet("/SearchMutter")
public class SearchMutter extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. ログインしているかを確認する。未ログインならトップページへ戻す。
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // 2. リクエストに含まれた検索キーワードを検証する。
        ValidationResult keywordResult =
                MutterInputValidator.validateKeyword(request.getParameter("keyword"));

        // 3. キーワードが正しければ、検索処理を実行して結果をセットする。
        if (keywordResult.valid() && keywordResult.value() != null) {
            List<Mutter> mutterList = new SearchMutterLogic().execute(keywordResult.value());
            request.setAttribute("mutterList", mutterList);
            if (mutterList == null || mutterList.isEmpty()) {
                request.setAttribute("errorMsg", "検索結果がありません");
            }
        } else {
            // 4. キーワードが空や不正なら、エラーメッセージを画面に渡す。
            request.setAttribute("errorMsg", keywordResult.valid()
                    ? "キーワードを入力してください" : keywordResult.message());
        }

        // 5. 検索モードであることを画面に伝え、表示用JSPへ転送する。
        request.setAttribute("searchMode", true);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/main.jsp");
        dispatcher.forward(request, response);
    }
}
