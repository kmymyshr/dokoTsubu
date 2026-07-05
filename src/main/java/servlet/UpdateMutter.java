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
import model.UpdateMutterLogic;
import model.User;

@WebServlet("/UpdateMutter")
public class UpdateMutter extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}

		Integer mutterId = parseMutterId(request.getParameter("mutterId"));
		if (mutterId == null) {
			redirectWithError(request, response, "編集するつぶやきを指定してください");
			return;
		}

		List<Mutter> mutterList = new GetMutterListLogic().execute();
		Mutter targetMutter = null;
		if (mutterList != null) {
			for (Mutter mutter : mutterList) {
				if (mutter.getId() == mutterId && mutter.getUserId() == loginUser.getId()) {
					targetMutter = mutter;
					break;
				}
			}
		}

		if (targetMutter == null) {
			redirectWithError(request, response, "編集できるつぶやきが見つかりません");
			return;
		}

		request.setAttribute("mutter", targetMutter);
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/updateMutter.jsp");
		dispatcher.forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		User loginUser = (User) request.getSession().getAttribute("loginUser");
		if (loginUser == null) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}

		Integer mutterId = parseMutterId(request.getParameter("mutterId"));
		String text = request.getParameter("text");
		if (mutterId == null || text == null || text.isBlank()) {
			redirectWithError(request, response, "つぶやきを入力してください");
			return;
		}

		Mutter mutter = new Mutter(mutterId, loginUser.getId(), loginUser.getName(), text);
		if (!new UpdateMutterLogic().execute(mutter)) {
			redirectWithError(request, response, "つぶやきの編集に失敗しました");
			return;
		}
		response.sendRedirect(request.getContextPath() + "/Main");
	}

	private Integer parseMutterId(String value) {
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String message)
			throws IOException {
		request.getSession().setAttribute("errorMsg", message);
		response.sendRedirect(request.getContextPath() + "/Main");
	}
}
