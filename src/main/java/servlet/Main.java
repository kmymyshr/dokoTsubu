package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.Mutter;
import model.PostMutterLogic;
import model.User;

@WebServlet("/Main")


public class Main extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ServletContext application = this.getServletContext();
		
		List<Mutter> mutterList = (List<Mutter>) application.getAttribute("mutterList");

		
		
		if (mutterList == null) {
			mutterList = new ArrayList<>();
			application.setAttribute("mutterList", mutterList);
		}
		
		
		HttpSession session = request.getSession();
		User loginUser = (User)session.getAttribute("loginUser");

		if (loginUser == null) {
			response.sendRedirect("index.jsp");
		} else {
			
			RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/main.jsp");
			dispatcher.forward(request, response);
					}
	}

	
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		String text = request.getParameter("text");

		if (text != null && text.length() != 0) {
			
			ServletContext application = this.getServletContext();
			List<Mutter> mutterList = (List<Mutter>)application.getAttribute("mutterList");

			
			HttpSession session = request.getSession();
			User loginUser = (User)session.getAttribute("loginUser");

			Mutter mutter = new Mutter(0, loginUser.getName(), text);

			PostMutterLogic postMutterLogic = new PostMutterLogic();
			postMutterLogic.execute(mutter, mutterList);
		} else {
			request.setAttribute("errorMsg", "つぶやきが入力されていません");
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/main.jsp");
		dispatcher.forward(request, response);
	}
}
