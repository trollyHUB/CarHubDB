package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Утилита для логирования действий пользователей и системных событий
 */
public class LoggerUtil {

    private static final String LOG_FILE = "carhub_logs.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static boolean sessionActive = false;
    private static String currentSessionUser = null;

    // Статический блок для логирования завершения приложения
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (sessionActive && currentSessionUser != null) {
                logSessionEnd(currentSessionUser, "Приложение закрыто без выхода");
            }
            logSystemEvent("Приложение завершено");
            writeSeparator();
        }));
    }

    /**
     * Типы логов
     */
    public enum LogLevel {
        INFO("ℹ️ INFO"),
        WARNING("⚠️ WARNING"),
        ERROR("❌ ERROR"),
        SUCCESS("✅ SUCCESS"),
        AUTH("🔐 AUTH"),
        ACTION("🎯 ACTION");

        private final String label;

        LogLevel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Основной метод логирования
     */
    private static void log(LogLevel level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] %s: %s%n", timestamp, level.getLabel(), message);

        // Вывод в консоль
        System.out.print(logEntry);

        // Запись в файл
        writeToFile(logEntry);
    }

    /**
     * Запись в файл с правильной кодировкой UTF-8
     */
    private static void writeToFile(String logEntry) {
        try (FileWriter fw = new FileWriter(LOG_FILE, java.nio.charset.StandardCharsets.UTF_8, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.print(logEntry);
        } catch (IOException e) {
            System.err.println("Ошибка записи в лог-файл: " + e.getMessage());
        }
    }

    // ========== ПУБЛИЧНЫЕ МЕТОДЫ ==========

    /**
     * Информационное сообщение
     */
    public static void info(String message) {
        log(LogLevel.INFO, message);
    }

    /**
     * Предупреждение
     */
    public static void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    /**
     * Предупреждение (короткий алиас)
     */
    public static void warn(String message) {
        warning(message);
    }

    /**
     * Ошибка
     */
    public static void error(String message, Exception e) {
        String fullMessage = message + " | Причина: " + (e != null ? e.getMessage() : "Неизвестная ошибка");
        log(LogLevel.ERROR, fullMessage);
    }

    /**
     * Успешная операция
     */
    public static void success(String message) {
        log(LogLevel.SUCCESS, message);
    }

    /**
     * Аутентификация (вход/выход)
     */
    public static void auth(String message) {
        log(LogLevel.AUTH, message);
    }

    /**
     * Действие пользователя
     */
    public static void action(String message) {
        log(LogLevel.ACTION, message);
    }

    // ========== СПЕЦИАЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Логирование входа пользователя
     */
    public static void logLogin(String username, String role) {
        if (!sessionActive) {
            writeSeparator();
            logSystemEvent("╔═══════════════════════════════════════════════════════════╗");
            logSystemEvent("║          НОВАЯ СЕССИЯ ПОЛЬЗОВАТЕЛЯ                       ║");
            logSystemEvent("╚═══════════════════════════════════════════════════════════╝");
            sessionActive = true;
        }
        currentSessionUser = username;
        auth(String.format("👤 Вход: пользователь '%s' (роль: %s)", username, role));
    }

    /**
     * Логирование выхода пользователя
     */
    public static void logLogout(String username) {
        auth(String.format("👋 Выход: пользователь '%s'", username));
        logSessionEnd(username, "Нормальный выход");
    }

    /**
     * Завершение сессии
     */
    private static void logSessionEnd(String username, String reason) {
        if (sessionActive) {
            info("📊 Сессия завершена: " + reason);
            logSystemEvent("╔═══════════════════════════════════════════════════════════╗");
            logSystemEvent("║          КОНЕЦ СЕССИИ: " + username);
            logSystemEvent("╚═══════════════════════════════════════════════════════════╝");
            sessionActive = false;
            currentSessionUser = null;
        }
    }

    /**
     * Системное событие (без типа лога)
     */
    private static void logSystemEvent(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] %s%n", timestamp, message);
        System.out.print(logEntry);
        writeToFile(logEntry);
    }

    /**
     * Разделитель между сессиями
     */
    private static void writeSeparator() {
        String separator = "\n" + "=".repeat(80) + "\n";
        writeToFile(separator);
    }

    /**
     * Логирование регистрации
     */
    public static void logRegistration(String username) {
        auth(String.format("Регистрация нового пользователя: '%s'", username));
    }

    /**
     * Логирование добавления авто
     */
    public static void logCarAdded(String carName, String username) {
        action(String.format("Добавлен автомобиль '%s' пользователем '%s'", carName, username));
    }

    /**
     * Логирование редактирования авто
     */
    public static void logCarEdited(String carName, String username) {
        action(String.format("Изменён автомобиль '%s' пользователем '%s'", carName, username));
    }

    /**
     * Логирование удаления авто
     */
    public static void logCarDeleted(String carName, String username) {
        action(String.format("Удалён автомобиль '%s' пользователем '%s'", carName, username));
    }

    /**
     * Логирование экспорта
     */
    public static void logExport(String username, int recordCount) {
        action(String.format("Экспорт данных: %d записей, пользователь '%s'", recordCount, username));
    }

    /**
     * Очистка старых логов (опционально)
     */
    public static void clearLogs() {
        File logFile = new File(LOG_FILE);
        if (logFile.exists()) {
            logFile.delete();
        }
        info("Лог-файл очищен");
    }

    /**
     * Получить путь к лог-файлу
     */
    public static String getLogFilePath() {
        return new File(LOG_FILE).getAbsolutePath();
    }
}

