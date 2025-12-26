package services;

import database.DatabaseConnection;
import models.Car;
import models.Favorite;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FavoritesService {

    // Добавить авто в избранное
    public static boolean addToFavorites(int userId, int carId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ addToFavorites: Connection is null");
                return false;
            }

            String sql = "INSERT INTO favorites (user_id, car_id) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, carId);
            int rows = stmt.executeUpdate();
            System.out.println("✅ addToFavorites: userId=" + userId + ", carId=" + carId + ", rows=" + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ addToFavorites ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Удалить авто из избранного
    public static boolean removeFromFavorites(int userId, int carId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return false;

            String sql = "DELETE FROM favorites WHERE user_id = ? AND car_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, carId);
            int rows = stmt.executeUpdate();
            System.out.println("✅ removeFromFavorites: userId=" + userId + ", carId=" + carId + ", rows=" + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ removeFromFavorites ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Проверить, находится ли авто в избранном
    public static boolean isFavorite(int userId, int carId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return false;

            String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND car_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, carId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean result = rs.getInt(1) > 0;
                System.out.println("✅ isFavorite: userId=" + userId + ", carId=" + carId + ", result=" + result);
                return result;
            }
        } catch (SQLException e) {
            System.err.println("❌ isFavorite ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Получить все избранные авто пользователя
    public static List<Car> getFavoritesCars(int userId) {
        List<Car> favorites = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return favorites;

            String sql = "SELECT c.* FROM Cars c " +
                        "INNER JOIN favorites f ON c.id = f.car_id " +
                        "WHERE f.user_id = ? " +
                        "ORDER BY f.created_at DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            // Проверяем наличие дополнительных колонок
            ResultSetMetaData meta = rs.getMetaData();
            boolean hasBrand = hasColumn(meta, "brand");
            boolean hasYear = hasColumn(meta, "year");
            boolean hasMileage = hasColumn(meta, "mileage");
            boolean hasDesc = hasColumn(meta, "description");
            boolean hasImage = hasColumn(meta, "imageUrl") || hasColumn(meta, "image_url");

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String model = rs.getString("model");
                double price = rs.getDouble("price");

                if (hasBrand || hasYear || hasMileage || hasDesc || hasImage) {
                    String brand = hasBrand ? rs.getString("brand") : null;
                    Integer year = hasYear ? (Integer) rs.getObject("year") : null;
                    Integer mileage = hasMileage ? (Integer) rs.getObject("mileage") : null;
                    String description = hasDesc ? rs.getString("description") : null;
                    String imageUrl = null;
                    if (hasImage) {
                        if (hasColumn(meta, "imageUrl")) imageUrl = rs.getString("imageUrl");
                        else if (hasColumn(meta, "image_url")) imageUrl = rs.getString("image_url");
                    }

                    // ✅ ЗАГРУЖАЕМ ГЛАВНОЕ ФОТО ИЗ ТАБЛИЦЫ CarImages
                    String mainImageUrl = getMainImageUrl(conn, id);
                    if (mainImageUrl != null && !mainImageUrl.isEmpty()) {
                        imageUrl = mainImageUrl;
                    }

                    favorites.add(new Car(id, name, model, price, brand, year, mileage, description, imageUrl));
                } else {
                    favorites.add(new Car(id, name, model, price));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favorites;
    }

    // Получить количество избранных у пользователя
    public static int getFavoritesCount(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            String sql = "SELECT COUNT(*) FROM Favorites WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static boolean hasColumn(ResultSetMetaData meta, String column) throws SQLException {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (meta.getColumnLabel(i).equalsIgnoreCase(column)) return true;
        }
        return false;
    }

    /**
     * Получает URL главной фотографии автомобиля из таблицы CarImages
     */
    private static String getMainImageUrl(Connection conn, int carId) {
        try {
            String sql = "SELECT image_url FROM CarImages WHERE car_id = ? AND is_main = 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, carId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("image_url");
            }
        } catch (SQLException e) {
            // Если таблицы CarImages нет - пропускаем
        }
        return null;
    }
}

