package servlet;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.MutterListResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.GetMutterListLogic;
import model.MutterPage;
import model.User;
import util.ObjectMapperFactory;

@WebServlet("/MutterList")
public class MutterList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int MAX_LIMIT = 100;
	private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

	/**
	 * つぶやき一覧を JSON で返す。
	 * ログイン済みユーザー向けに、ページング用の cursor と limit を受け取る。
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 1. ログイン済みか確認する。
		HttpSession session = request.getSession(false);
		User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		// 2. cursor の形式を確認する。
		Integer cursor = parsePositiveInteger(request.getParameter("cursor"));
		String cursorParameter = request.getParameter("cursor");
		if (cursorParameter != null && cursor == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "cursorが不正です");
			return;
		}

		// 3. limit を整えて、つぶやき一覧を取得する。
		int limit = parseLimit(request.getParameter("limit"));
		MutterPage page = new GetMutterListLogic().execute(cursor, limit);
		// つぶやきごとにいいね情報を付与してから返す
		java.util.List<dto.MutterResponse> responseList = page.getMutters().stream()
				.map(m -> {
					int likeCount = new dao.LikeDAO().countLikes(m.getId());
					boolean likedByMe = new dao.LikeDAO().hasLiked(m.getId(), loginUser.getId());
					return dto.MutterResponse.from(m, likeCount, likedByMe);
				})
				.toList();
		MutterListResponse responseBody = MutterListResponse.from(responseList, page);

		// 4. JSON としてレスポンスを返す。
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		OBJECT_MAPPER.writeValue(response.getWriter(), responseBody);
	}

	// limit の値を安全な範囲に整える。
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

	// cursor を正の整数として扱えるように変換する。
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
}
