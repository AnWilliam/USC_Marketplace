package dto;

public class ErrorResponse {
    private boolean success;
    private String message;

    public ErrorResponse(String message) {
        this.success = false;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}