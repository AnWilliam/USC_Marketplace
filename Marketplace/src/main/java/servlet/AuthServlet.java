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
public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        try {
            if ("/register".equals(path)) {
                register(request, response);
            } else if ("/login".equals(path)) {
                login(request, response);
            } else if ("/logout".equals(path)) {
                logout(request, response);
            } else {
                JsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown auth endpoint.");
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String name = request.getParameter("name");

        User user = userService.register(email, password, name);
        SessionUtil.setCurrentUserID(request, user.getUserID());
        JsonUtil.writeJson(response, HttpServletResponse.SC_CREATED,
            "{\"success\":true,\"message\":\"Registered successfully.\",\"data\":" + JsonUtil.userToJson(user) + "}");
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        User user = userService.login(email, password);
        SessionUtil.setCurrentUserID(request, user.getUserID());
        JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
            "{\"success\":true,\"message\":\"Logged in successfully.\",\"data\":" + JsonUtil.userToJson(user) + "}");
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SessionUtil.logout(request);
        JsonUtil.writeSuccess(response, "Logged out successfully.");
    }
}