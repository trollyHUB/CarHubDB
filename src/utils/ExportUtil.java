package utils;

import models.Car;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Утилита для экспорта данных в различные форматы
 */
public class ExportUtil {

    /**
     * Экспорт списка автомобилей в CSV файл
     * @param cars Список автомобилей
     * @param file Файл для сохранения
     * @throws IOException при ошибке записи
     */
    public static void exportCarsToCSV(List<Car> cars, File file) throws IOException {
        // Используем OutputStreamWriter с явной кодировкой UTF-8
        try (OutputStreamWriter osw = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            // ✅ Добавляем UTF-8 BOM для корректного открытия в Excel
            writer.write('\ufeff');

            // Заголовок CSV
            writer.write("ID,Название,Модель,Цена,Бренд,Год,Пробег,Описание,Фото\n");

            // Данные
            for (Car car : cars) {
                String line = String.format("%d,\"%s\",\"%s\",%.2f,\"%s\",%s,%s,\"%s\",\"%s\"\n",
                    car.getId(),
                    escapeCSV(car.getName()),
                    escapeCSV(car.getModel()),
                    car.getPrice(),
                    escapeCSV(car.getBrand()),
                    car.getYear() != null ? car.getYear() : "",
                    car.getMileage() != null ? car.getMileage() : "",
                    escapeCSV(car.getDescription()),
                    escapeCSV(car.getImageUrl())
                );
                writer.write(line);
            }
        }
    }

    /**
     * Экранирование специальных символов для CSV
     */
    private static String escapeCSV(String value) {
        if (value == null) return "";
        // Заменяем кавычки на двойные кавычки и удаляем переносы строк
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", "");
    }

    /**
     * Экспорт статистики в текстовый файл
     */
    public static void exportStatisticsToText(String content, File file) throws IOException {
        // Используем OutputStreamWriter с явной кодировкой UTF-8
        try (OutputStreamWriter osw = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            // ✅ Добавляем UTF-8 BOM для корректного отображения в текстовых редакторах
            writer.write('\ufeff');

            writer.write("========================================\n");
            writer.write("   СТАТИСТИКА АВТОСАЛОНА CARHUB\n");
            writer.write("========================================\n\n");
            writer.write(content);
            writer.write("\n\n========================================\n");
            writer.write("Дата экспорта: " + java.time.LocalDateTime.now() + "\n");
            writer.write("========================================\n");
        }
    }
}

