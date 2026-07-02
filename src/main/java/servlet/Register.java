//ユーザ登録に関するリクエストを処理するコントローラ  コミット確認修正

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

    // 登録画面を表示
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/WEB-INF/jsp/registerView.jsp");
        dispatcher.forward(request, response);
    }

    // 登録処理
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String pass = request.getParameter("pass");

        if(name != null && name.length() != 0 && pass != null && pass.length() != 0) {
			// 入力値が有効な場合の処理
        	   User user = new User(name, pass);

               RegisterUserLogic logic = new RegisterUserLogic();
               boolean isRegister = logic.execute(user);
        	
        	if (isRegister) {
        	//登録完了画面にフォワード
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
    	//どちらかでも入力されてなければエラーメッセージ出力
    	request.setAttribute("errorMsg", "必須項目が未入力ですYO");
       
         RequestDispatcher dispatcher =
                request.getRequestDispatcher("/WEB-INF/jsp/registerView.jsp");
        dispatcher.forward(request, response);
    }
}}
