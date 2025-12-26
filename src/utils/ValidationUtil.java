package utils;

import java.util.regex.Pattern;

/**
 * Утилита для валидации вводимых данных
 */
public class ValidationUtil {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Проверка: является ли строка допустимым URL
     */
    public static boolean isValidUrl(String url) {
        return url != null && !url.isBlank() && URL_PATTERN.matcher(url).matches();
    }

    /**
     * Проверка: является ли строка допустимым числом (цена, пробег)
     */
    public static boolean isValidNumber(String text) {
        if (text == null || text.isBlank()) return false;
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Проверка: является ли строка допустимым годом (1900-2030)
     */
    public static boolean isValidYear(String text) {
        if (text == null || text.isBlank()) return true; // год опционален
        try {
            int year = Integer.parseInt(text);
            return year >= 1900 && year <= 2030;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Проверка: является ли строка допустимым пробегом (0-999999)
     */
    public static boolean isValidMileage(String text) {
        if (text == null || text.isBlank()) return true; // пробег опционален
        try {
            int mileage = Integer.parseInt(text);
            return mileage >= 0 && mileage <= 999999;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Проверка: является ли строка допустимой ценой (> 0)
     */
    public static boolean isValidPrice(String text) {
        if (!isValidNumber(text)) return false;
        try {
            double price = Double.parseDouble(text);
            return price > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Проверка: не пустая ли строка
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Проверка: длина строки в допустимых пределах
     */
    public static boolean isValidLength(String text, int minLen, int maxLen) {
        if (text == null) return false;
        int len = text.trim().length();
        return len >= minLen && len <= maxLen;
    }

    /**
     * Форматирование сообщения об ошибке валидации
     */
    public static String formatValidationError(String fieldName, String issue) {
        return "❌ Поле \"" + fieldName + "\": " + issue;
    }
}

