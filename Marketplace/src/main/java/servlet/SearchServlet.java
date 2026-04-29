package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Item;
import service.ItemService;
import util.JsonUtil;

public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ItemService itemService = new ItemService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String keyword = request.getParameter("q");
            Integer categoryID = parseOptionalInteger(request.getParameter("categoryID"));
            List<Item> items = itemService.searchItems(keyword, categoryID);
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                "{\"success\":true,\"data\":" + itemsToJson(items) + "}");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private Integer parseOptionalInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private String itemsToJson(List<Item> items) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(JsonUtil.itemToJson(items.get(i)));
        }
        json.append(']');
        return json.toString();
    }
}