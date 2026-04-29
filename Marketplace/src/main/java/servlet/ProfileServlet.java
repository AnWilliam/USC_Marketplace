package servlet;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.UserService;
import util.JsonUtil;
import util.SessionUtil;

@MultipartConfig
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
            String bio = request.getParameter("bio");
            String profilePicture = request.getParameter("profilePicture");
            User user = userService.updateProfile(userID, bio, profilePicture);
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                "{\"success\":true,\"message\":\"Profile updated.\",\"data\":" + JsonUtil.userToJson(user) + "}");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}