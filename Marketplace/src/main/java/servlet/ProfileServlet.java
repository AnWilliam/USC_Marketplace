package servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Locale;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.User;
import service.UserService;
import util.JsonUtil;
import util.SessionUtil;

@MultipartConfig(maxFileSize = 5242880L, maxRequestSize = 6291456L)
public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userID = SessionUtil.requireLogin(request, response);
        if (userID == -1) {
            return;
        }

        try {
            User user = userService.getProfile(userID);
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                "{\"success\":true,\"data\":" + JsonUtil.userToJson(user) + "}");
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
            User current = userService.getProfile(userID);
            String bio = request.getParameter("bio");

            Part photoPart = request.getPart("photo");
            String pictureParam = request.getParameter("profilePicture");

            String pictureValue = current.getProfilePicture();
            if (photoPart != null && photoPart.getSize() > 0) {
                pictureValue = saveProfilePhoto(request, userID, photoPart);
            } else if (pictureParam != null) {
                String trimmed = pictureParam.trim();
                pictureValue = trimmed.isEmpty() ? null : trimmed;
            }

            User user = userService.updateProfile(userID, bio, pictureValue);
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                "{\"success\":true,\"message\":\"Profile updated.\",\"data\":" + JsonUtil.userToJson(user) + "}");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not save profile photo.");
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private static void prepareMultipart(HttpServletRequest request) throws IOException, ServletException {
        String ct = request.getContentType();
        if (ct != null && ct.toLowerCase(Locale.ROOT).startsWith("multipart/form-data")) {
            request.getParts();
        }
    }

    private static String saveProfilePhoto(HttpServletRequest request, int userID, Part part) throws IOException {
        String ctype = part.getContentType();
        if (ctype == null || !ctype.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Profile photo must be an image file.");
        }

        String ext = extensionFromFilename(part.getSubmittedFileName());
        String relative = "uploads/profile/" + userID + ext;

        String basePath = request.getServletContext().getRealPath("/");
        if (basePath == null) {
            throw new IOException("Cannot resolve application directory for uploads.");
        }

        Path uploadDir = Paths.get(basePath, "uploads", "profile");
        Files.createDirectories(uploadDir);

        Path target = uploadDir.resolve(userID + ext);
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
