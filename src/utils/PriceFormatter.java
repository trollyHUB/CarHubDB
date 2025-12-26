package utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Утилита для форматирования цен в приложении CarHub
 * Решает проблему отображения больших чисел в научной нотации (8.2E7)
 */
public class PriceFormatter {

    private static final DecimalFormat PRICE_FORMAT;
    private static final DecimalFormat PRICE_FORMAT_WITH_DECIMALS;

    static {
        // Создаем форматтер для русской локали (пробелы между разрядами)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("ru", "KZ"));
        symbols.setGroupingSeparator(' '); // Пробел между разрядами
        symbols.setDecimalSeparator(',');   // Запятая для дробной части

        // Форматтер без копеек (для отображения в карточках и таблицах)
        PRICE_FORMAT = new DecimalFormat("#,##0", symbols);

        // Форматтер с копейками (для детального просмотра)
        PRICE_FORMAT_WITH_DECIMALS = new DecimalFormat("#,##0.00", symbols);
    }

    /**
     * Форматирует цену БЕЗ копеек (для карточек, таблиц, списков)
     * Пример: 82000000 → "82 000 000 ₸"
     */
    public static String format(double price) {
        return PRICE_FORMAT.format(price) + " ₸";
    }

    /**
     * Форматирует цену С копейками (для детального просмотра)
     * Пример: 82000000 → "82 000 000,00 ₸"
     */
    public static String formatWithDecimals(double price) {
        return PRICE_FORMAT_WITH_DECIMALS.format(price) + " ₸";
    }

    /**
     * Форматирует цену с символом тенге ВПЕРЕДИ
     * Пример: 82000000 → "₸ 82 000 000"
     */
    public static String formatWithPrefix(double price) {
        return "₸ " + PRICE_FORMAT.format(price);
    }

    /**
     * Форматирует цену в миллионах для компактного отображения
     * Пример: 82000000 → "82 млн ₸"
     */
    public static String formatInMillions(double price) {
        if (price >= 1_000_000) {
            double millions = price / 1_000_000;
            DecimalFormat df = new DecimalFormat("#,##0.#");
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("ru", "KZ"));
            symbols.setGroupingSeparator(' ');
            symbols.setDecimalSeparator(',');
            df.setDecimalFormatSymbols(symbols);
            return df.format(millions) + " млн ₸";
        }
        return format(price);
    }

    /**
     * Форматирует диапазон цен
     * Пример: "от 14 500 000 ₸ до 98 000 000 ₸"
     */
    public static String formatRange(double minPrice, double maxPrice) {
        return "от " + format(minPrice) + " до " + format(maxPrice);
    }

    /**
     * Парсит строку цены обратно в число (убирает пробелы и символы)
     */
    public static double parse(String priceString) throws NumberFormatException {
        String cleaned = priceString.replaceAll("[^0-9,.]", "");
        cleaned = cleaned.replace(',', '.');
        return Double.parseDouble(cleaned);
    }
}

