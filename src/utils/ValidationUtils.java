package utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Shared validation helpers used by UI and services. */
public final class ValidationUtils {
    private ValidationUtils() {}

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static int requireInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }

    public static double requirePositiveDouble(String value, String fieldName) {
        try {
            double number = Double.parseDouble(value.trim());
            if (number <= 0) {
                throw new IllegalArgumentException(fieldName + " must be greater than zero.");
            }
            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid amount.");
        }
    }

    public static LocalDate requireDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " must be in YYYY-MM-DD format.");
        }
    }
}
