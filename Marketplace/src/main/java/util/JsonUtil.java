package util;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletResponse;
import model.Category;
import model.Conversation;
import model.Item;
import model.Message;
import model.User;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static void writeJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    public static void writeSuccess(HttpServletResponse response, String message) throws IOException {
        writeJson(response, HttpServletResponse.SC_OK, "{\"success\":true,\"message\":" + quote(message) + "}");
    }

    public static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        writeJson(response, status, "{\"success\":false,\"message\":" + quote(message) + "}");
    }

    public static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': escaped.append("\\\\"); break;
                case '"': escaped.append("\\\""); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default: escaped.append(c);
            }
        }
        escaped.append("\"");
        return escaped.toString();
    }

    public static String date(LocalDateTime value) {
        return value == null ? "null" : quote(value.toString());
    }

    public static String money(BigDecimal value) {
        return value == null ? "null" : value.toPlainString();
    }

    public static String userToJson(User user) {
        if (user == null) {
            return "null";
        }
        return "{"
            + "\"userID\":" + user.getUserID() + ","
            + "\"email\":" + quote(user.getEmail()) + ","
            + "\"name\":" + quote(user.getName()) + ","
            + "\"createdAt\":" + date(user.getCreatedAt()) + ","
            + "\"profilePicture\":" + quote(user.getProfilePicture()) + ","
            + "\"bio\":" + quote(user.getBio())
            + "}";
    }

    public static String itemToJson(Item item) {
        if (item == null) {
            return "null";
        }
        return "{"
            + "\"itemID\":" + item.getItemID() + ","
            + "\"sellerID\":" + item.getSellerID() + ","
            + "\"categoryID\":" + item.getCategoryID() + ","
            + "\"title\":" + quote(item.getTitle()) + ","
            + "\"description\":" + quote(item.getDescription()) + ","
            + "\"price\":" + money(item.getPrice()) + ","
            + "\"status\":" + quote(item.getStatus()) + ","
            + "\"dateListed\":" + date(item.getDateListed())
            + "}";
    }

    public static String categoryToJson(Category category) {
        if (category == null) {
            return "null";
        }
        return "{"
            + "\"categoryID\":" + category.getCategoryID() + ","
            + "\"categoryName\":" + quote(category.getCategoryName()) + ","
            + "\"description\":" + quote(category.getDescription())
            + "}";
    }

    public static String conversationToJson(Conversation conversation) {
        if (conversation == null) {
            return "null";
        }
        return "{"
            + "\"conversationID\":" + conversation.getConversationID() + ","
            + "\"itemID\":" + conversation.getItemID() + ","
            + "\"buyerID\":" + conversation.getBuyerID() + ","
            + "\"sellerID\":" + conversation.getSellerID() + ","
            + "\"createdAt\":" + date(conversation.getCreatedAt()) + ","
            + "\"lastMessageAt\":" + date(conversation.getLastMessageAt())
            + "}";
    }

    public static String messageToJson(Message message) {
        if (message == null) {
            return "null";
        }
        return "{"
            + "\"messageID\":" + message.getMessageID() + ","
            + "\"conversationID\":" + message.getConversationID() + ","
            + "\"senderID\":" + message.getSenderID() + ","
            + "\"content\":" + quote(message.getContent()) + ","
            + "\"timestamp\":" + date(message.getTimestamp()) + ","
            + "\"read\":" + message.isRead()
            + "}";
    }
}
