package servlet;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
import util.UploadUtil;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 5 * 1024 * 1024,
    maxRequestSize = 32 * 1024 * 1024
)
public class ItemServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_IMAGES = 6;

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
            BigDecimal price = new BigDecimal(request.getParameter("price"));

            validateImageUploads(request);
            Item item = itemService.createItem(userID, categoryID, title, description, price);
            List<String> imageUrls = saveUploadedImages(request, item.getItemID());
            item = itemService.addItemImages(item.getItemID(), userID, imageUrls);
            JsonUtil.writeJson(response, HttpServletResponse.SC_CREATED,
                "{\"success\":true,\"message\":\"Item created.\",\"data\":" + JsonUtil.itemToJson(item) + "}");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void validateImageUploads(HttpServletRequest request) throws IOException, ServletException {
        if (!isMultipart(request)) {
            return;
        }

        int imageCount = 0;
        for (Part part : request.getParts()) {
            if (!"images".equals(part.getName()) || part.getSize() == 0) {
                continue;
            }
            imageCount++;
            if (imageCount > MAX_IMAGES) {
                throw new IllegalArgumentException("You can upload up to 6 images.");
            }
            if (part.getContentType() == null || !part.getContentType().toLowerCase().startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed.");
            }
        }
    }

    private List<String> saveUploadedImages(HttpServletRequest request, int itemID) throws IOException, ServletException {
        List<String> imageUrls = new ArrayList<>();
        if (!isMultipart(request)) {
            return imageUrls;
        }

        Collection<Part> parts = request.getParts();

        for (Part part : parts) {
            if (!"images".equals(part.getName()) || part.getSize() == 0) {
                continue;
            }
            if (imageUrls.size() >= MAX_IMAGES) {
                throw new IllegalArgumentException("You can upload up to 6 images.");
            }
            if (part.getContentType() == null || !part.getContentType().toLowerCase().startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed.");
            }

            String extension = extensionFor(part);
            String fileName = UUID.randomUUID().toString() + extension;
            String relativeFolder = "items/" + itemID;

            Path uploadPath = UploadUtil.resolve(getServletContext(), relativeFolder);
            Files.createDirectories(uploadPath);
            Path target = uploadPath.resolve(fileName);
            try (InputStream input = part.getInputStream()) {
                Files.copy(input, target);
            }
            imageUrls.add("uploads/" + relativeFolder + "/" + fileName);
        }

        return imageUrls;
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    private String extensionFor(Part part) {
        String submitted = part.getSubmittedFileName();
        if (submitted != null) {
            int dot = submitted.lastIndexOf('.');
            if (dot >= 0 && dot < submitted.length() - 1) {
                String extension = submitted.substring(dot).toLowerCase();
                if (extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                    return extension;
                }
            }
        }

        String contentType = part.getContentType() == null ? "" : part.getContentType().toLowerCase();
        if (contentType.contains("png")) {
            return ".png";
        }
        if (contentType.contains("gif")) {
            return ".gif";
        }
        if (contentType.contains("webp")) {
            return ".webp";
        }
        return ".jpg";
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
