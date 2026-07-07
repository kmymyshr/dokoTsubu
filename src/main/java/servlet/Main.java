package servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Mutter;
import model.PostMutterLogic;
import model.User;
import validation.MutterInputValidator;
import validation.ValidationResult;

/** メイン画面の枠を返します。画面データはJavaScriptがREST APIから取得します。 */
@WebServlet("/Main")
public class Main extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/main.jsp");
        dispatcher.forward(request, response);
    }

    /** 旧フォームとの互換用。新しいメイン画面はPOST /api/muttersを使用します。 */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        ValidationResult textResult = MutterInputValidator.validateText(request.getParameter("text"));
        if (textResult.valid()) {
            if (!new PostMutterLogic().execute(new Mutter(loginUser.getId(), textResult.value()))) {
                session.setAttribute("errorMsg", "つぶやきの投稿に失敗しました");
            }
        } else {
            session.setAttribute("errorMsg", textResult.message());
        }
        response.sendRedirect(request.getContextPath() + "/Main");
    }
}
