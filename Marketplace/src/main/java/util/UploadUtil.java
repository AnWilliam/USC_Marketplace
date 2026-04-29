package util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.ServletContext;

public final class UploadUtil {
    private UploadUtil() {
    }

    public static Path getUploadBasePath(ServletContext context) {
        String configured = context.getInitParameter("uploadDirectory");
        if (configured != null && !configured.trim().isEmpty()) {
            return Paths.get(configured.trim()).toAbsolutePath().normalize();
        }
        return Paths.get(System.getProperty("user.home") + File.separator + "usc-marketplace-uploads")
            .toAbsolutePath()
            .normalize();
    }

    public static Path resolve(ServletContext context, String relativePath) {
        return getUploadBasePath(context).resolve(relativePath).normalize();
    }
}
