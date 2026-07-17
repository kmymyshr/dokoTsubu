//パスワードチェック処理をユーザ情報がテーブルに存在するかチェックするように変更

package servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.LoginLogic;
import model.User;

@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * ログイン処理を行う。
	 * フォームから送られたユーザー名とパスワードを確認し、成功したらセッションに保存する。
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 1. 文字コードをUTF-8に設定する。
		request.setCharacterEncoding("UTF-8");

		// 2. フォームから送られた値を取得する。
		String name = request.getParameter("name");
		String pass = request.getParameter("pass");

		// 3. 入力値が両方とも空でなければ、ログイン処理を実行する。
		if ((name != null && name.length() != 0) && (pass != null && pass.length() != 0)) {
			User user = new User(name, pass);
			LoginLogic loginLogic = new LoginLogic();
			User dBUser = loginLogic.login(user);

			// 4. ユーザー情報が見つかれば、セッションに保存する。
			if (dBUser != null) {
				HttpSession session = request.getSession();
				request.changeSessionId();
				session.setAttribute("loginUser", dBUser);
			} else {
				// 5. 認証に失敗した場合はエラーメッセージをセットする。
				request.setAttribute("errorMsg", "パスワードが間違っているか、ユーザが未登録です");
			}
		} else {
			// 6. 入力が不足している場合はエラーメッセージをセットする。
			request.setAttribute("errorMsg", "必須項目が未入力です");
		}

		// 7. ログイン結果画面へ転送する。
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/loginResult.jsp");
		dispatcher.forward(request, response);
	}
}
