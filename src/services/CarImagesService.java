package services;

import database.DatabaseConnection;
import models.CarImage;
import utils.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с фото автомобилей
 */
public class CarImagesService {

    /**
     * Получить все фото автомобиля
     */
    public static List<CarImage> getCarImages(int carId) {
        List<CarImage> images = new ArrayList<>();
        String sql = "SELECT * FROM CarImages WHERE car_id = ? ORDER BY is_main DESC, display_order ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CarImage image = new CarImage(
                    rs.getInt("id"),
                    rs.getInt("car_id"),
                    rs.getString("image_url"),
                    rs.getBoolean("is_main"),
                    rs.getInt("display_order"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
                images.add(image);
            }

            LoggerUtil.info("Загружено " + images.size() + " фото для автомобиля ID=" + carId);

        } catch (Exception e) {
            LoggerUtil.error("Ошибка загрузки фото автомобиля ID=" + carId, e);
        }

        return images;
    }

    /**
     * Получить главное фото автомобиля
     */
    public static String getMainImage(int carId) {
        String sql = "SELECT image_url FROM CarImages WHERE car_id = ? AND is_main = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("image_url");
            }

        } catch (Exception e) {
            LoggerUtil.error("Ошибка получения главного фото для автомобиля ID=" + carId, e);
        }

        // Если нет главного фото, вернуть первое доступное
        sql = "SELECT TOP 1 image_url FROM CarImages WHERE car_id = ? ORDER BY display_order";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("image_url");
            }

        } catch (Exception e) {
            LoggerUtil.error("Ошибка получения первого фото для автомобиля ID=" + carId, e);
        }

        return null;
    }

    /**
     * Добавить фото
     */
    public static boolean addImage(int carId, String imageUrl, boolean isMain) {
        // Если это главное фото, сбросить флаг у других
        if (isMain) {
            resetMainFlag(carId);
        }

        String sql = "INSERT INTO CarImages (car_id, image_url, is_main, display_order) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Получить следующий display_order
            int nextOrder = getNextDisplayOrder(carId);

            stmt.setInt(1, carId);
            stmt.setString(2, imageUrl);
            stmt.setBoolean(3, isMain);
            stmt.setInt(4, nextOrder);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                LoggerUtil.action("Добавлено фото для автомобиля ID=" + carId + " (main=" + isMain + ")");
                return true;
            }

        } catch (Exception e) {
            LoggerUtil.error("Ошибка добавления фото для автомобиля ID=" + carId, e);
        }

        return false;
    }

    /**
     * Установить главное фото
     */
    public static boolean setMainImage(int imageId) {
        // Сначала получим car_id этого фото
        int carId = getCarIdByImageId(imageId);
        if (carId == -1) return false;

        // Сбросить флаг у всех фото этого автомобиля
        resetMainFlag(carId);

        // Установить флаг для выбранного фото
        String sql = "UPDATE CarImages SET is_main = 1 WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, imageId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                LoggerUtil.action("Установлено главное фото ID=" + imageId);
                return true;
            }

        } catch (Exception e) {
            LoggerUtil.error("Ошибка установки главного фото ID=" + imageId, e);
        }

        return false;
    }

    /**
     * Удалить фото
     */
    public static boolean deleteImage(int imageId) {
        String sql = "DELETE FROM CarImages WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, imageId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                LoggerUtil.action("Удалено фото ID=" + imageId);
                return true;
            }

        } catch (Exception e) {
            LoggerUtil.error("Ошибка удаления фото ID=" + imageId, e);
        }

        return false;
    }

    /**
     * Сбросить флаг главного фото у всех фото автомобиля
     */
    private static void resetMainFlag(int carId) {
        String sql = "UPDATE CarImages SET is_main = 0 WHERE car_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            stmt.executeUpdate();

        } catch (Exception e) {
            LoggerUtil.error("Ошибка сброса флага главного фото для автомобиля ID=" + carId, e);
        }
    }

    /**
     * Получить следующий порядок отображения
     */
    private static int getNextDisplayOrder(int carId) {
        String sql = "SELECT ISNULL(MAX(display_order), 0) + 1 AS next_order FROM CarImages WHERE car_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("next_order");
            }

        } catch (Exception e) {
            LoggerUtil.error("Ошибка получения следующего display_order", e);
        }

        return 1;
    }

    /**
     * Получить car_id по image_id
     */
    private static int getCarIdByImageId(int imageId) {
        String sql = "SELECT car_id FROM CarImages WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, imageId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("car_id");
            }

        } catch (Exception e) {
            LoggerUtil.error("Ошибка получения car_id для фото ID=" + imageId, e);
        }

        return -1;
    }

    /**
     * Получить количество фото автомобиля
     */
    public static int getImagesCount(int carId) {
        String sql = "SELECT COUNT(*) AS cnt FROM CarImages WHERE car_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("cnt");
            }

        } catch (Exception e) {
            LoggerUtil.error("Ошибка подсчёта фото для автомобиля ID=" + carId, e);
        }

        return 0;
    }
}

