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

import model.GetMutterListLogic;
import model.Mutter;
import model.PostMutterLogic;
import model.User;

@WebServlet("/Main")

public class Main extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		//ServletContext application = this.getServletContext();

		HttpSession session = request.getSession();

		User loginUser = (User) session.getAttribute("loginUser");

		if (loginUser == null) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}
		request.setAttribute("loginUser", loginUser);
		
		
		GetMutterListLogic getMutterListLogic = new GetMutterListLogic();
		List<Mutter> mutterList = getMutterListLogic.execute();
		//直接DAOを呼ばず、logicクラスを経由してデータを取得するように変更
		//MutterDAO dao = new MutterDAO();
		//List<Mutter> mutterList = dao.findAll();

		request.setAttribute("mutterList", mutterList);

		//追加
		String errorMsg = (String) session.getAttribute("errorMsg");
		session.removeAttribute("errorMsg");
		request.setAttribute("errorMsg", errorMsg);
		//追加ここまで

		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/main.jsp");
		dispatcher.forward(request, response);
	}

	
	//doPostは最後にredirectするので、requestスコープに保存しても意味がない。セッションスコープに保存する必要がある。
	

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {


		request.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");

		//ログイン中のユーザIDとつぶやき情報でmutterインスタンスを生成し、DBに登録		

		if (loginUser == null) {
			response.sendRedirect(request.getContextPath() + "/");

			return;
		}


		
		String text = request.getParameter("text");

		if (text != null && !text.isBlank()) {

			Mutter mutter = new Mutter(loginUser.getId(), text);

			PostMutterLogic postMutterLogic = new PostMutterLogic();

			boolean result = postMutterLogic.execute(mutter);

			if (!result) {
				session.setAttribute("errorMsg", "つぶやきの投稿に失敗しました");
			}

			//postMutterLogic.execute(mutter);

		} else {
			session.setAttribute("errorMsg", "つぶやきが入力されていません");
		}

		response.sendRedirect(request.getContextPath() + "/Main");
	}
}