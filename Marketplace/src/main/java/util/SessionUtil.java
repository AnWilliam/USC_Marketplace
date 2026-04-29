package util;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public final class SessionUtil {
    public static final String USER_ID_KEY = "userID";

    private SessionUtil() {
    }

    public static void setCurrentUserID(HttpServletRequest request, int userID) {
        request.getSession(true).setAttribute(USER_ID_KEY, userID);
    }

    public static Integer getCurrentUserID(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(USER_ID_KEY);
        return value instanceof Integer ? (Integer) value : null;
    }

    public static int requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userID = getCurrentUserID(request);
        if (userID == null) {
            JsonUtil.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in first.");
            return -1;
        }
        return userID;
    }

    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}