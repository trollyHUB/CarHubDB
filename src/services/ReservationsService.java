package services;

import database.DatabaseConnection;
import models.Reservation;
import models.Purchase;
import utils.LoggerUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с бронированиями и покупками
 */
public class ReservationsService {

    // ========== БРОНИРОВАНИЯ ==========

    /**
     * Создать бронирование
     */
    public static boolean createReservation(int carId, int userId, String customerName,
                                           String phone, String email, LocalDateTime reservationDate, String notes) {
        String sql = "INSERT INTO Reservations (car_id, user_id, customer_name, phone, email, reservation_date, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            stmt.setInt(2, userId);
            stmt.setString(3, customerName);
            stmt.setString(4, phone);
            stmt.setString(5, email);
            stmt.setTimestamp(6, Timestamp.valueOf(reservationDate));
            stmt.setString(7, notes);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                LoggerUtil.action("Создано бронирование: " + customerName + " для авто ID=" + carId);
                return true;
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка создания бронирования", e);
        }
        return false;
    }

    /**
     * Получить все бронирования
     */
    public static List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, c.name as car_name, u.username " +
                    "FROM Reservations r " +
                    "JOIN Cars c ON r.car_id = c.id " +
                    "JOIN users_secure u ON r.user_id = u.id " +
                    "ORDER BY r.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(extractReservation(rs));
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка получения бронирований", e);
        }
        return list;
    }

    /**
     * Получить бронирования по статусу
     */
    public static List<Reservation> getReservationsByStatus(String status) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, c.name as car_name, u.username " +
                    "FROM Reservations r " +
                    "JOIN Cars c ON r.car_id = c.id " +
                    "JOIN users_secure u ON r.user_id = u.id " +
                    "WHERE r.status = ? " +
                    "ORDER BY r.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(extractReservation(rs));
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка получения бронирований по статусу", e);
        }
        return list;
    }

    /**
     * Обновить статус бронирования
     */
    public static boolean updateReservationStatus(int reservationId, String newStatus) {
        String sql = "UPDATE Reservations SET status = ?, updated_at = GETDATE() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, reservationId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                LoggerUtil.action("Статус бронирования ID=" + reservationId + " изменён на: " + newStatus);
                return true;
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка обновления статуса бронирования", e);
        }
        return false;
    }

    /**
     * Удалить бронирование
     */
    public static boolean deleteReservation(int reservationId) {
        String sql = "DELETE FROM Reservations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                LoggerUtil.action("Удалено бронирование ID=" + reservationId);
                return true;
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка удаления бронирования", e);
        }
        return false;
    }

    // ========== ПОКУПКИ ==========

    /**
     * Создать покупку
     */
    public static boolean createPurchase(int carId, int userId, String customerName,
                                        String phone, String email, double price,
                                        String paymentMethod, String notes) {
        String sql = "INSERT INTO Purchases (car_id, user_id, customer_name, phone, email, price, payment_method, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            stmt.setInt(2, userId);
            stmt.setString(3, customerName);
            stmt.setString(4, phone);
            stmt.setString(5, email);
            stmt.setDouble(6, price);
            stmt.setString(7, paymentMethod);
            stmt.setString(8, notes);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                LoggerUtil.action("Создана покупка: " + customerName + " для авто ID=" + carId + ", цена: " + price);
                return true;
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка создания покупки", e);
        }
        return false;
    }

    /**
     * Получить все покупки
     */
    public static List<Purchase> getAllPurchases() {
        List<Purchase> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name as car_name, u.username " +
                    "FROM Purchases p " +
                    "JOIN Cars c ON p.car_id = c.id " +
                    "JOIN users_secure u ON p.user_id = u.id " +
                    "ORDER BY p.purchase_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(extractPurchase(rs));
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка получения покупок", e);
        }
        return list;
    }

    /**
     * Обновить статус покупки
     */
    public static boolean updatePurchaseStatus(int purchaseId, String newStatus) {
        String sql = "UPDATE Purchases SET status = ?" +
                    (newStatus.equals("completed") ? ", completed_at = GETDATE()" : "") +
                    " WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, purchaseId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                LoggerUtil.action("Статус покупки ID=" + purchaseId + " изменён на: " + newStatus);
                return true;
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка обновления статуса покупки", e);
        }
        return false;
    }

    /**
     * Удалить покупку
     */
    public static boolean deletePurchase(int purchaseId) {
        String sql = "DELETE FROM Purchases WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, purchaseId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                LoggerUtil.action("Удалена покупка ID=" + purchaseId);
                return true;
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка удаления покупки", e);
        }
        return false;
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private static Reservation extractReservation(ResultSet rs) throws SQLException {
        return new Reservation(
            rs.getInt("id"),
            rs.getInt("car_id"),
            rs.getString("car_name"),
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("customer_name"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getTimestamp("reservation_date").toLocalDateTime(),
            rs.getString("status"),
            rs.getString("notes"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }

    private static Purchase extractPurchase(ResultSet rs) throws SQLException {
        Timestamp completedTs = rs.getTimestamp("completed_at");
        return new Purchase(
            rs.getInt("id"),
            rs.getInt("car_id"),
            rs.getString("car_name"),
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("customer_name"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getDouble("price"),
            rs.getString("payment_method"),
            rs.getString("status"),
            rs.getString("notes"),
            rs.getTimestamp("purchase_date").toLocalDateTime(),
            completedTs != null ? completedTs.toLocalDateTime() : null
        );
    }
}

