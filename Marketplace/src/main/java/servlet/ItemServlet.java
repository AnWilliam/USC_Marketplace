package servlet;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
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
            prepareMultipart(request);
        } catch (IOException | ServletException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Could not read form data.");
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

            Part photoPart = request.getPart("photo");
            if (photoPart != null && photoPart.getSize() > 0) {
                String savedRelative = saveItemPhoto(request, item.getItemID(), photoPart);
                itemService.updateItemPhoto(item.getItemID(), userID, savedRelative);
                item = itemService.getItemById(item.getItemID());
            }

            JsonUtil.writeJson(response, HttpServletResponse.SC_CREATED,
                "{\"success\":true,\"message\":\"Item created.\",\"data\":" + JsonUtil.itemToJson(item) + "}");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not save photo.");
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    /** Ensures multipart fields are parsed so {@link HttpServletRequest#getParameter(String)} works (Tomcat-safe). */
    private static void prepareMultipart(HttpServletRequest request) throws IOException, ServletException {
        String ct = request.getContentType();
        if (ct != null && ct.toLowerCase(Locale.ROOT).startsWith("multipart/form-data")) {
            request.getParts();
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

    private static String saveItemPhoto(HttpServletRequest request, int itemID, Part part) throws IOException {
        String ctype = part.getContentType();
        if (ctype == null || !ctype.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Photo must be an image file.");
        }

        String ext = extensionFromFilename(part.getSubmittedFileName());
        String relative = "uploads/items/" + itemID + ext;

        String basePath = request.getServletContext().getRealPath("/");
        if (basePath == null) {
            throw new IOException("Cannot resolve application directory for uploads.");
        }

        Path uploadDir = Paths.get(basePath, "uploads", "items");
        Files.createDirectories(uploadDir);

        Path target = uploadDir.resolve(itemID + ext);
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return relative;
    }

    private static String extensionFromFilename(String submittedName) {
        if (submittedName == null || submittedName.isEmpty()) {
            return ".jpg";
        }
        String name = submittedName.toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        String ext = dot >= 0 ? name.substring(dot) : "";
        switch (ext) {
            case ".png":
            case ".gif":
            case ".webp":
            case ".jpeg":
            case ".jpg":
                return ext;
            default:
                return ".jpg";
        }
    }
}
