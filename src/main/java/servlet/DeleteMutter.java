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

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendRedirect(request.getContextPath() + "/Main");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}

		Integer mutterId = parseMutterId(request.getParameter("mutterId"));
		if (mutterId == null) {
			session.setAttribute("errorMsg", "削除するつぶやきを指定してください");
			response.sendRedirect(request.getContextPath() + "/Main");
			return;
		}

		if (!new DeleteMutterLogic().execute(mutterId, loginUser.getId())) {
			session.setAttribute("errorMsg", "つぶやきの削除に失敗しました");
		}
		response.sendRedirect(request.getContextPath() + "/Main");
	}

	private Integer parseMutterId(String value) {
		try {
			int mutterId = Integer.parseInt(value);
			return mutterId > 0 ? mutterId : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
