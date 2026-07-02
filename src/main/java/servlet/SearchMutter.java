
package servlet;

import java.io.IOException;
import java.util.List;

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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");

		if (loginUser == null) {
		    response.sendRedirect("index.jsp");
		    return;
		}
		
		
	String keyword = request.getParameter("keyword");
	
	if(keyword != null && !keyword.isBlank()){
		SearchMutterLogic searchMutterLogic = new SearchMutterLogic();
		List<Mutter> mutterList = searchMutterLogic.execute(keyword);
		
		request.setAttribute("mutterList", mutterList);
		
		if(mutterList == null || mutterList.isEmpty()) {
			request.setAttribute("errorMsg", "検索結果がありません");
			} 
	}
		 else {
		request.setAttribute("errorMsg", "キーワードを入力してください");
	}
    request.getRequestDispatcher("WEB-INF/jsp/main.jsp")
    .forward(request, response);
	
	}	
}
