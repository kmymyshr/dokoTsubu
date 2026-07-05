package servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import model.GetMutterListLogic;
import model.Mutter;

@WebServlet("/MutterList")
public class MutterList extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        GetMutterListLogic logic = new GetMutterListLogic();
        List<Mutter> mutterList = logic.execute();

        response.setContentType("application/json; charset=UTF-8");

        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < mutterList.size(); i++) {
            Mutter mutter = mutterList.get(i);

            json.append("{");
            json.append("\"id\":").append(mutter.getId()).append(",");
            json.append("\"userId\":").append(mutter.getUserId()).append(",");
            json.append("\"userName\":\"")
                    .append(escapeJson(mutter.getUserName()))
                    .append("\",");

            json.append("\"text\":\"")
                    .append(escapeJson(mutter.getText()))
                    .append("\"");
            json.append("}");

            if (i < mutterList.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        response.getWriter().write(json.toString());
    }

    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }

        return str
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

}