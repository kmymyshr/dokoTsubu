
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

@WebServlet("/SearchMutter")

public class SearchMutter extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		//ログインしているかを確認するため、セッションスコープからログインユーザを取得する
		HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			//未ログイン　デフォルトページに返す
			response.sendRedirect("/dokoTsubu/");
			return;
		}

		//リクエストパラメータの取得
		request.setCharacterEncoding("UTF-8");

		String keyword = request.getParameter("keyword");

		if (keyword != null && !keyword.isBlank()) {

			//SMLのメソッド→MutterDAOのメソッド からの戻り値(配列である名前mutterlistのmutterデータ)を受け取って、mutterListに格納する
			//mutterListをリクエストスコープに保存する
			SearchMutterLogic searchMutterLogic = new SearchMutterLogic();

			List<Mutter> mutterList = searchMutterLogic.execute(keyword);

			request.setAttribute("mutterList", mutterList);

			if (mutterList == null || mutterList.isEmpty()) {
				request.setAttribute("errorMsg", "検索結果がありません");
			}
		} else {
			request.setAttribute("errorMsg", "キーワードを入力してください");

		}
		//検索結果が自動更新で全件表示に上書きされないよう、検索中であることをJSPへ渡す
		request.setAttribute("searchMode", true);

		//フォワード先はmain.jspにする　特に画面は変えずに出す
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/main.jsp");
		dispatcher.forward(request, response);
	}
}
