package servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.UploadUtil;

public class ImageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String relativePath = pathInfo.substring(1);
        Path basePath = UploadUtil.getUploadBasePath(getServletContext());
        Path imagePath = basePath.resolve(relativePath).normalize();
        if (!imagePath.startsWith(basePath) || !Files.exists(imagePath) || Files.isDirectory(imagePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = getServletContext().getMimeType(imagePath.getFileName().toString());
        response.setContentType(contentType == null ? "application/octet-stream" : contentType);
        response.setContentLengthLong(Files.size(imagePath));
        try (OutputStream output = response.getOutputStream()) {
            Files.copy(imagePath, output);
        }
    }
}
