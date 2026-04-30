package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Conversation;
import service.ConversationService;
import util.JsonUtil;
import util.SessionUtil;
import dto.ConversationSummary;

@MultipartConfig
public class ConversationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ConversationService conversationService = new ConversationService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        int userID = SessionUtil.requireLogin(request, response);
        if (userID == -1) {
            return;
        }

        try {
            int itemID = Integer.parseInt(request.getParameter("itemID"));
            Conversation conversation = conversationService.startConversation(itemID, userID);
            JsonUtil.writeJson(response, HttpServletResponse.SC_CREATED,
                "{\"success\":true,\"message\":\"Conversation ready.\",\"data\":" + JsonUtil.conversationToJson(conversation) + "}");
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
        	List<ConversationSummary> conversations = conversationService.getConversationSummariesForUser(userID);
        	JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
        	    "{\"success\":true,\"data\":" + conversationSummariesToJson(conversations) + "}");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private String conversationSummariesToJson(List<ConversationSummary> conversations) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < conversations.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(JsonUtil.conversationSummaryToJson(conversations.get(i)));
        }
        json.append(']');
        return json.toString();
    }
}