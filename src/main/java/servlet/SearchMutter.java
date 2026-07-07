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

/** 旧検索画面との互換用。新しいメイン画面はGET /api/mutters?keyword=...を使用します。 */
@WebServlet("/SearchMutter")
public class SearchMutter extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        ValidationResult keywordResult =
                MutterInputValidator.validateKeyword(request.getParameter("keyword"));
        if (keywordResult.valid() && keywordResult.value() != null) {
            List<Mutter> mutterList = new SearchMutterLogic().execute(keywordResult.value());
            request.setAttribute("mutterList", mutterList);
            if (mutterList == null || mutterList.isEmpty()) {
                request.setAttribute("errorMsg", "検索結果がありません");
            }
        } else {
            request.setAttribute("errorMsg", keywordResult.valid()
                    ? "キーワードを入力してください" : keywordResult.message());
        }
        request.setAttribute("searchMode", true);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/main.jsp");
        dispatcher.forward(request, response);
    }
}
