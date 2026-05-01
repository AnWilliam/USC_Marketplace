package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import dao.WishlistDAO;
import model.Item;
import util.JsonUtil;
import util.SessionUtil;
import jakarta.servlet.annotation.WebServlet;

@WebServlet({"/wishlist", "/wishlist/check"})

public class Wishlistservlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userID = SessionUtil.requireLogin(request, response);
        if (userID == -1) return;

        try {
            String checkItemID = request.getParameter("itemID");

            // 🔹 CHECK IF ITEM IS IN WISHLIST
            if (request.getRequestURI().contains("check") && checkItemID != null) {
                boolean inWishlist = wishlistDAO.isInWishlist(userID, Integer.parseInt(checkItemID));
                JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                        "{\"success\":true,\"inWishlist\":" + inWishlist + "}");
                return;
            }

            // 🔹 GET FULL WISHLIST
            List<Item> items = wishlistDAO.getWishlistItems(userID);
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                    "{\"success\":true,\"data\":" + itemsToJson(items) + "}");

        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userID = SessionUtil.requireLogin(request, response);
        if (userID == -1) return;

        try {
            int itemID = Integer.parseInt(request.getParameter("itemID"));

            wishlistDAO.addToWishlist(userID, itemID);

            JsonUtil.writeSuccess(response, "Added to wishlist");

        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userID = SessionUtil.requireLogin(request, response);
        if (userID == -1) return;

        try {
            int itemID = Integer.parseInt(request.getParameter("itemID"));

            wishlistDAO.removeFromWishlist(userID, itemID);

            JsonUtil.writeSuccess(response, "Removed from wishlist");

        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error: " + e.getMessage());
        }
    }

    private String itemsToJson(List<Item> items) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) json.append(',');
            json.append(JsonUtil.itemToJson(items.get(i)));
        }
        json.append(']');
        return json.toString();
    }
}