package util;

import java.math.BigDecimal;
import java.util.Set;

public final class ValidationUtil {
    private static final Set<String> VALID_STATUSES = Set.of("AVAILABLE", "SOLD", "PENDING");

    private ValidationUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email
            .replace('\uFF20', '@')
            .replace('\uFF0E', '.')
            .replace('\u3002', '.')
            .replaceAll("[\\s\\u200B\\u200C\\u200D\\uFEFF]+", "")
            .toLowerCase();
    }

    public static boolean isUSCEmail(String email) {
        return normalizeEmail(email).endsWith("@usc.edu");
    }

    public static boolean isValidPrice(BigDecimal price) {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isValidStatus(String status) {
        return status != null && VALID_STATUSES.contains(status.toUpperCase());
    }
}
