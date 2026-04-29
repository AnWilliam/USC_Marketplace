package servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Item;
import service.ItemService;
import util.JsonUtil;
import util.SessionUtil;

@MultipartConfig(maxFileSize = 10485760L, maxRequestSize = 12582912L)
public class ItemServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ItemService itemService = new ItemService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");
            if (id != null) {
                Item item = itemService.getItemById(Integer.parseInt(id));
                JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                    "{\"success\":true,\"data\":" + JsonUtil.itemToJson(item) + "}");
            } else {
                List<Item> items = itemService.getAvailableItems();
                JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                    "{\"success\":true,\"data\":" + itemsToJson(items) + "}");
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        int userID = SessionUtil.requireLogin(request, response);
        if (userID == -1) {
            return;
        }

        try {
            String action = request.getParameter("action");
            if ("updateStatus".equals(action)) {
                int itemID = Integer.parseInt(request.getParameter("itemID"));
                String status = request.getParameter("status");
                itemService.updateStatus(itemID, userID, status);
                JsonUtil.writeSuccess(response, "Item status updated.");
                return;
            }

            int categoryID = Integer.parseInt(request.getParameter("categoryID"));
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String itemCondition = request.getParameter("itemCondition");
            BigDecimal price = new BigDecimal(request.getParameter("price"));

            Item item = itemService.createItem(userID, categoryID, title, description, price, itemCondition);
            JsonUtil.writeJson(response, HttpServletResponse.SC_CREATED,
                "{\"success\":true,\"message\":\"Item created.\",\"data\":" + JsonUtil.itemToJson(item) + "}");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
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
