package servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.GetMutterListLogic;
import model.Mutter;
import model.MutterPage;
import model.User;

@WebServlet("/MutterList")
public class MutterList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int MAX_LIMIT = 100;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		Integer cursor = parsePositiveInteger(request.getParameter("cursor"));
		String cursorParameter = request.getParameter("cursor");
		if (cursorParameter != null && cursor == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "cursorが不正です");
			return;
		}

		int limit = parseLimit(request.getParameter("limit"));
		MutterPage page = new GetMutterListLogic().execute(cursor, limit);

		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().write(toJson(page));
	}

	private int parseLimit(String value) {
		if (value == null || value.isBlank()) {
			return GetMutterListLogic.DEFAULT_LIMIT;
		}
		try {
			int limit = Integer.parseInt(value);
			return limit > 0 ? Math.min(limit, MAX_LIMIT) : GetMutterListLogic.DEFAULT_LIMIT;
		} catch (NumberFormatException e) {
			return GetMutterListLogic.DEFAULT_LIMIT;
		}
	}

	private Integer parsePositiveInteger(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			int number = Integer.parseInt(value);
			return number > 0 ? number : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String toJson(MutterPage page) {
		StringBuilder json = new StringBuilder();
		json.append("{\"mutters\":[");

		List<Mutter> mutters = page.getMutters();
		for (int i = 0; i < mutters.size(); i++) {
			Mutter mutter = mutters.get(i);
			json.append("{");
			json.append("\"id\":").append(mutter.getId()).append(",");
			json.append("\"userId\":").append(mutter.getUserId()).append(",");
			json.append("\"userName\":\"").append(escapeJson(mutter.getUserName())).append("\",");
			json.append("\"text\":\"").append(escapeJson(mutter.getText())).append("\",");
			json.append("\"version\":").append(mutter.getVersion()).append(",");
			json.append("\"createdAt\":\"")
					.append(escapeJson(mutter.getCreatedAt().toString())).append("\"");
			json.append("}");

			if (i < mutters.size() - 1) {
				json.append(",");
			}
		}

		json.append("],\"nextCursor\":");
		if (page.getNextCursor() == null) {
			json.append("null");
		} else {
			json.append(page.getNextCursor());
		}
		json.append(",\"hasNext\":").append(page.hasNext()).append("}");
		return json.toString();
	}

	private String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\r", "\\r")
				.replace("\n", "\\n");
	}
}
