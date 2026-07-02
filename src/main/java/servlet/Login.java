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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String name = request.getParameter("name");
		String pass = request.getParameter("pass");

		if((name != null && name.length() != 0) && (pass != null && pass.length() != 0)) {
			
		
		User user = new User(name, pass);

		LoginLogic loginLogic = new LoginLogic();
		
		
		User findUser = loginLogic.login(user);
		
		if (findUser != null) {
			HttpSession session = request.getSession();
			session.setAttribute("loginUser", findUser);
			
		} else {
			request.setAttribute("errorMsg", "パスワードが間違っているか、ユーザが未登録です");
		}
		
		} else {
			request.setAttribute("errorMsg", "必須項目が未入力です");
		}
		
		//boolean isLogin = loginLogic.login(user);
		
        //request.setAttribute("isLogin", isLogin);

		//if (isLogin) {
		//	HttpSession session = request.getSession();
		//	session.setAttribute("loginUser", user);
		//}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/loginResult.jsp");
		dispatcher.forward(request, response);
	}

}
