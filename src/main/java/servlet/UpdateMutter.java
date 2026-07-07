package servlet;

import java.io.IOException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.GetMutterLogic;
import model.Mutter;
import model.UpdateMutterLogic;
import model.User;
import security.CsrfTokenManager;
import validation.MutterInputValidator;
import validation.ValidationResult;

@WebServlet("/UpdateMutter")
public class UpdateMutter extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * 編集画面を表示する。
	 * 対象のつぶやきが自分のものか確認してから表示する。
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 1. ログイン済みか確認する。
		HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}

		// 2. 編集対象のつぶやきIDを取得する。
		Integer mutterId = parseMutterId(request.getParameter("mutterId"));
		if (mutterId == null) {
			redirectWithError(request, response, "編集するつぶやきを指定してください");
			return;
		}

		// 3. 該当つぶやきを取得し、自分のものか確認する。
		Mutter targetMutter = new GetMutterLogic().execute(mutterId);
		if (targetMutter == null || targetMutter.getUserId() != loginUser.getId()) {
			redirectWithError(request, response, "編集できるつぶやきが見つかりません");
			return;
		}

		// 4. 編集画面で使う情報をリクエストにセットする。
		request.setAttribute("mutter", targetMutter);
		request.setAttribute("csrfToken", CsrfTokenManager.getOrCreate(session));
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/updateMutter.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * 編集内容を保存する。
	 * 入力内容とバージョンを確認してから更新する。
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 1. 文字コードを設定する。
		request.setCharacterEncoding("UTF-8");

		// 2. ログイン済みか確認する。
		User loginUser = (User) request.getSession().getAttribute("loginUser");
		if (loginUser == null) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}

		// 3. フォームから送られた値を取得する。
		Integer mutterId = parseMutterId(request.getParameter("mutterId"));
		Integer version = parseVersion(request.getParameter("version"));
		ValidationResult textResult = MutterInputValidator.validateText(request.getParameter("text"));
		if (mutterId == null || version == null || !textResult.valid()) {
			redirectWithError(request, response,
					textResult.valid() ? "更新情報が不正です" : textResult.message());
			return;
		}

		// 4. 更新用のオブジェクトを作成して保存を試みる。
		Mutter mutter = new Mutter(mutterId, loginUser.getId(), loginUser.getName(),
				textResult.value(), version);
		if (!new UpdateMutterLogic().execute(mutter)) {
			redirectWithError(request, response, "他の操作により更新できませんでした。最新の内容を確認してください");
			return;
		}

		// 5. 成功したらメイン画面へ戻る。
		response.sendRedirect(request.getContextPath() + "/Main");
	}

	// つぶやきIDを整数に変換する。
	private Integer parseMutterId(String value) {
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// version を安全な整数として扱う。
	private Integer parseVersion(String value) {
		try {
			int version = Integer.parseInt(value);
			return version >= 0 ? version : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// エラー時はメイン画面へ戻す。
	private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String message)
			throws IOException {
		request.getSession().setAttribute("errorMsg", message);
		response.sendRedirect(request.getContextPath() + "/Main");
	}
}
