package services;

import database.DatabaseConnection;
import models.Car;
import utils.LoggerUtil;

import java.sql.*;
import java.util.*;

public class StatisticsService {

    // Общее количество автомобилей
    public static int getTotalCars() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Cars");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Средняя цена автомобилей
    public static double getAveragePrice() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT AVG(price) FROM Cars");
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Самый дорогой автомобиль
    public static Car getMostExpensiveCar() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return null;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT TOP 1 * FROM Cars ORDER BY price DESC");
            if (rs.next()) {
                return extractCarFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Самый дешёвый автомобиль
    public static Car getCheapestCar() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return null;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT TOP 1 * FROM Cars ORDER BY price ASC");
            if (rs.next()) {
                return extractCarFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Топ-5 брендов по количеству автомобилей
    public static Map<String, Integer> getTopBrands(int limit) {
        Map<String, Integer> brands = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return brands;

            String sql = "SELECT TOP " + limit + " brand, COUNT(*) as count " +
                        "FROM Cars " +
                        "WHERE brand IS NOT NULL AND brand != '' " +
                        "GROUP BY brand " +
                        "ORDER BY count DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                brands.put(rs.getString("brand"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return brands;
    }

    // Количество пользователей
    public static int getTotalUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users_secure WHERE role = 'user'");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Количество админов
    public static int getTotalAdmins() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users_secure WHERE role = 'admin'");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Количество активных пользователей
    public static int getTotalActiveUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users_secure WHERE is_active = 1");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Количество неактивных пользователей
    public static int getTotalInactiveUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users_secure WHERE is_active = 0");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Количество заявок на бронирование
    public static int getTotalReservations() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Reservations", null);
            if (!tables.next()) {
                return 0;
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Reservations");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Количество заявок на покупку
    public static int getTotalPurchases() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Purchases", null);
            if (!tables.next()) {
                return 0;
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Purchases");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Количество комментариев
    public static int getTotalComments() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "comments_ratings", null);
            if (!tables.next()) {
                return 0;
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM comments_ratings WHERE comment IS NOT NULL AND comment != ''");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Количество оценок
    public static int getTotalRatings() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "comments_ratings", null);
            if (!tables.next()) {
                return 0;
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM comments_ratings WHERE rating IS NOT NULL AND rating > 0");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Распределение по годам выпуска
    public static Map<Integer, Integer> getCarsByYear() {
        Map<Integer, Integer> yearStats = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return yearStats;

            String sql = "SELECT year, COUNT(*) as count " +
                        "FROM Cars " +
                        "WHERE year IS NOT NULL " +
                        "GROUP BY year " +
                        "ORDER BY year DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                yearStats.put(rs.getInt("year"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return yearStats;
    }

    // Средний пробег
    public static double getAverageMileage() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT AVG(CAST(mileage AS FLOAT)) FROM Cars WHERE mileage IS NOT NULL");
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Общее количество избранных
    public static int getTotalFavorites() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return 0;

            // Проверяем существование таблицы
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Favorites", null);
            if (!tables.next()) {
                return 0; // Таблица не существует
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Favorites");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Вспомогательный метод для извлечения Car из ResultSet
    private static Car extractCarFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String model = rs.getString("model");
        double price = rs.getDouble("price");

        ResultSetMetaData meta = rs.getMetaData();
        boolean hasBrand = hasColumn(meta, "brand");
        boolean hasYear = hasColumn(meta, "year");
        boolean hasMileage = hasColumn(meta, "mileage");
        boolean hasDesc = hasColumn(meta, "description");
        boolean hasImage = hasColumn(meta, "imageUrl") || hasColumn(meta, "image_url");

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
            return new Car(id, name, model, price, brand, year, mileage, description, imageUrl);
        } else {
            return new Car(id, name, model, price);
        }
    }

    private static boolean hasColumn(ResultSetMetaData meta, String column) throws SQLException {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (meta.getColumnLabel(i).equalsIgnoreCase(column)) return true;
        }
        return false;
    }

    // ================================
    // МЕТОДЫ ДЛЯ ДЕТАЛЬНОЙ ИНФОРМАЦИИ
    // ================================

    /**
     * Получить детальную информацию по избранным
     */
    public static javafx.collections.ObservableList<models.StatDetailsItem> getFavoritesDetails() {
        javafx.collections.ObservableList<models.StatDetailsItem> items = javafx.collections.FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return items;

            String sql = "SELECT f.id, u.username, c.name + ' ' + c.model AS car_name, c.brand, f.created_at " +
                        "FROM Favorites f " +
                        "JOIN users_secure u ON f.user_id = u.id " +
                        "JOIN Cars c ON f.car_id = c.id " +
                        "ORDER BY f.created_at DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String carName = rs.getString("car_name");
                String brand = rs.getString("brand");
                String date = rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "—";

                items.add(new models.StatDetailsItem(id, username, carName, brand, date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Получить детальную информацию по бронированиям
     */
    public static javafx.collections.ObservableList<models.StatDetailsItem> getReservationsDetails() {
        javafx.collections.ObservableList<models.StatDetailsItem> items = javafx.collections.FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ getReservationsDetails: Connection is null");
                return items;
            }

            // Проверка существования таблицы
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Reservations", null);
            if (!tables.next()) {
                System.err.println("⚠️ Таблица Reservations не найдена");
                return items;
            }

            String sql = "SELECT r.id, r.customer_name, c.name + ' ' + c.model AS car_name, r.status, r.created_at " +
                        "FROM Reservations r " +
                        "JOIN Cars c ON r.car_id = c.id " +
                        "ORDER BY r.created_at DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String customerName = rs.getString("customer_name");
                String carName = rs.getString("car_name");
                String status = rs.getString("status");
                String date = rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "—";

                items.add(new models.StatDetailsItem(id, customerName, carName, status, date));
            }

            System.out.println("✅ Загружено бронирований: " + items.size());
        } catch (SQLException e) {
            System.err.println("❌ Ошибка загрузки бронирований: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Получить детальную информацию по покупкам
     */
    public static javafx.collections.ObservableList<models.StatDetailsItem> getPurchasesDetails() {
        javafx.collections.ObservableList<models.StatDetailsItem> items = javafx.collections.FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ getPurchasesDetails: Connection is null");
                LoggerUtil.error("Ошибка загрузки покупок", new Exception("Connection is null"));
                return items;
            }

            // Попробуем разные варианты имени таблицы
            String sql = "SELECT p.id, p.customer_name, c.name + ' ' + c.model AS car_name, p.status, p.created_at " +
                        "FROM purchases p " +
                        "JOIN Cars c ON p.car_id = c.id " +
                        "ORDER BY p.created_at DESC";

            System.out.println("🔍 Выполняем SQL: " + sql);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String customerName = rs.getString("customer_name");
                String carName = rs.getString("car_name");
                String status = rs.getString("status");
                String date = rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "—";

                items.add(new models.StatDetailsItem(id, customerName, carName, status, date));
                count++;
            }

            System.out.println("✅ Загружено покупок: " + count);
            LoggerUtil.info("Загружено деталей покупок: " + count);

        } catch (SQLException e) {
            System.err.println("❌ Ошибка загрузки покупок: " + e.getMessage());
            e.printStackTrace();
            LoggerUtil.error("Ошибка загрузки деталей покупок", e);
        }

        return items;
    }

    /**
     * Получить детальную информацию по комментариям
     */
    public static javafx.collections.ObservableList<models.StatDetailsItem> getCommentsDetails() {
        javafx.collections.ObservableList<models.StatDetailsItem> items = javafx.collections.FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return items;

            String sql = "SELECT cr.id, u.username, c.name + ' ' + c.model AS car_name, " +
                        "SUBSTRING(cr.comment, 1, 50) + '...' AS comment_short, cr.created_at " +
                        "FROM comments_ratings cr " +
                        "JOIN users_secure u ON cr.user_id = u.id " +
                        "JOIN Cars c ON cr.car_id = c.id " +
                        "WHERE cr.comment IS NOT NULL " +
                        "ORDER BY cr.created_at DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String carName = rs.getString("car_name");
                String comment = rs.getString("comment_short");
                String date = rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "—";

                items.add(new models.StatDetailsItem(id, username, carName, comment, date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Получить детальную информацию по оценкам
     */
    public static javafx.collections.ObservableList<models.StatDetailsItem> getRatingsDetails() {
        javafx.collections.ObservableList<models.StatDetailsItem> items = javafx.collections.FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return items;

            String sql = "SELECT cr.id, u.username, c.name + ' ' + c.model AS car_name, " +
                        "CAST(cr.rating AS VARCHAR) + '/5 звезд' AS rating_str, cr.created_at " +
                        "FROM comments_ratings cr " +
                        "JOIN users_secure u ON cr.user_id = u.id " +
                        "JOIN Cars c ON cr.car_id = c.id " +
                        "WHERE cr.rating IS NOT NULL " +
                        "ORDER BY cr.created_at DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String carName = rs.getString("car_name");
                String rating = rs.getString("rating_str");
                String date = rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "—";

                items.add(new models.StatDetailsItem(id, username, carName, rating, date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Получить детальную информацию по администраторам
     */
    public static javafx.collections.ObservableList<models.StatDetailsItem> getAdminsDetails() {
        javafx.collections.ObservableList<models.StatDetailsItem> items = javafx.collections.FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ getAdminsDetails: Connection is null");
                return items;
            }

            // Добавляем created_at в SELECT
            String sql = "SELECT id, username, fullname, role, created_at FROM users_secure WHERE role = 'admin' ORDER BY id DESC";

            System.out.println("🔍 Запрос админов: " + sql);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("📊 Выполнен запрос админов");

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username") != null ? rs.getString("username") : "—";
                String fullname = rs.getString("fullname") != null ? rs.getString("fullname") : "—";
                String role = rs.getString("role") != null ? rs.getString("role") : "—";
                String date = "—";

                try {
                    Timestamp timestamp = rs.getTimestamp("created_at");
                    if (timestamp != null) {
                        date = timestamp.toLocalDateTime().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    }
                } catch (Exception ignored) {
                    // Если created_at не существует или null
                }

                items.add(new models.StatDetailsItem(id, username, fullname, "Роль: " + role, date));
                System.out.println("✅ Админ добавлен: " + username);
            }

            System.out.println("✅ Всего загружено админов: " + items.size());
        } catch (SQLException e) {
            System.err.println("❌ Ошибка загрузки админов: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Получить детальную информацию по активным пользователям
     */
    public static javafx.collections.ObservableList<models.StatDetailsItem> getActiveUsersDetails() {
        javafx.collections.ObservableList<models.StatDetailsItem> items = javafx.collections.FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return items;

            String sql = "SELECT id, username, fullname, role, created_at FROM users_secure WHERE is_active = 1 ORDER BY id DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username") != null ? rs.getString("username") : "—";
                String fullname = rs.getString("fullname") != null ? rs.getString("fullname") : "—";
                String role = rs.getString("role") != null ? rs.getString("role") : "—";
                String date = "—";

                try {
                    Timestamp timestamp = rs.getTimestamp("created_at");
                    if (timestamp != null) {
                        date = timestamp.toLocalDateTime().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    }
                } catch (Exception ignored) {
                    // Если created_at не существует или null
                }

                items.add(new models.StatDetailsItem(id, username, fullname, "Роль: " + role, date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Получить детальную информацию по неактивным пользователям
     */
    public static javafx.collections.ObservableList<models.StatDetailsItem> getInactiveUsersDetails() {
        javafx.collections.ObservableList<models.StatDetailsItem> items = javafx.collections.FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return items;

            String sql = "SELECT id, username, fullname, role, created_at FROM users_secure WHERE is_active = 0 ORDER BY id DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username") != null ? rs.getString("username") : "—";
                String fullname = rs.getString("fullname") != null ? rs.getString("fullname") : "—";
                String role = rs.getString("role") != null ? rs.getString("role") : "—";
                String date = "—";

                try {
                    Timestamp timestamp = rs.getTimestamp("created_at");
                    if (timestamp != null) {
                        date = timestamp.toLocalDateTime().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    }
                } catch (Exception ignored) {
                    // Если created_at не существует или null
                }

                items.add(new models.StatDetailsItem(id, username, fullname, "Роль: " + role, date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }
}


