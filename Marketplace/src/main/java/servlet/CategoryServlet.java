package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import dao.CategoryDAO;
import model.Category;
import util.JsonUtil;

public class CategoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Category> categories = categoryDAO.findAll();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < categories.size(); i++) {
                if (i > 0) {
                    json.append(',');
                }
                json.append(JsonUtil.categoryToJson(categories.get(i)));
            }
            json.append(']');
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                "{\"success\":true,\"data\":" + json + "}");
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}

	