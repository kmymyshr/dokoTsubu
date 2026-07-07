package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.DeleteMutterLogic;
import model.User;

@WebServlet("/DeleteMutter")
public class DeleteMutter extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * GET ではメイン画面へ戻す。
	 * 実際の削除処理は POST で行う。
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendRedirect(request.getContextPath() + "/Main");
	}

	/**
	 * 指定されたつぶやきを削除する。
	 * ログイン中のユーザー本人のものだけを削除できる。
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 1. 文字コードを設定する。
		request.setCharacterEncoding("UTF-8");

		// 2. ログイン済みか確認する。
		HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}

		// 3. 削除対象のつぶやきIDを取得する。
		Integer mutterId = parseMutterId(request.getParameter("mutterId"));
		if (mutterId == null) {
			session.setAttribute("errorMsg", "削除するつぶやきを指定してください");
			response.sendRedirect(request.getContextPath() + "/Main");
			return;
		}

		// 4. 削除ロジックを呼び出して、結果に応じてメッセージを設定する。
		if (!new DeleteMutterLogic().execute(mutterId, loginUser.getId())) {
			session.setAttribute("errorMsg", "つぶやきの削除に失敗しました");
		}

		// 5. メイン画面へ戻る。
		response.sendRedirect(request.getContextPath() + "/Main");
	}

	// 文字列からつぶやきIDを整数に変換する。
	private Integer parseMutterId(String value) {
		try {
			int mutterId = Integer.parseInt(value);
			return mutterId > 0 ? mutterId : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
