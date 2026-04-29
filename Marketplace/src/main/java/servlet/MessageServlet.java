package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Message;
import service.MessageService;
import util.JsonUtil;
import util.SessionUtil;

public class MessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final MessageService messageService = new MessageService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        int userID = SessionUtil.requireLogin(request, response);
        if (userID == -1) {
            return;
        }

        try {
            int conversationID = Integer.parseInt(request.getParameter("conversationID"));
            String action = request.getParameter("action");
            if ("markAsRead".equals(action)) {
                messageService.markAsRead(conversationID, userID);
                JsonUtil.writeSuccess(response, "Messages marked as read.");
                return;
            }

            String content = request.getParameter("content");
            Message message = messageService.sendMessage(conversationID, userID, content);
            JsonUtil.writeJson(response, HttpServletResponse.SC_CREATED,
                "{\"success\":true,\"message\":\"Message sent.\",\"data\":" + JsonUtil.messageToJson(message) + "}");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userID = SessionUtil.requireLogin(request, response);
        if (userID == -1) {
            return;
        }

        try {
            int conversationID = Integer.parseInt(request.getParameter("conversationID"));
            List<Message> messages = messageService.getMessages(conversationID, userID);
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                "{\"success\":true,\"data\":" + messagesToJson(messages) + "}");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private String messagesToJson(List<Message> messages) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < messages.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(JsonUtil.messageToJson(messages.get(i)));
        }
        json.append(']');
        return json.toString();
    }
}