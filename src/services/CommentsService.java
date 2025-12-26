package services;

import database.DatabaseConnection;
import models.Comment;
import utils.LoggerUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с комментариями и оценками
 */
public class CommentsService {

    // ========== КОММЕНТАРИИ ==========

    /**
     * Добавить комментарий к автомобилю
     */
    public static boolean addComment(int carId, int userId, String commentText) {
        String sql = "INSERT INTO comments_ratings (car_id, user_id, comment) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            stmt.setInt(2, userId);
            stmt.setString(3, commentText);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                LoggerUtil.action("Добавлен комментарий к авто ID=" + carId);
                return true;
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка добавления комментария", e);
        }
        return false;
    }

    /**
     * Получить все комментарии к автомобилю
     */
    public static List<Comment> getCommentsByCar(int carId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.id, c.car_id, c.user_id, u.username, c.comment, c.created_at " +
                    "FROM comments_ratings c " +
                    "JOIN users_secure u ON c.user_id = u.id " +
                    "WHERE c.car_id = ? AND c.comment IS NOT NULL " +
                    "ORDER BY c.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int userId = rs.getInt("user_id");
                String userName = rs.getString("username");
                String text = rs.getString("comment");
                Timestamp ts = rs.getTimestamp("created_at");
                LocalDateTime createdAt = ts.toLocalDateTime();

                comments.add(new Comment(id, carId, userId, userName, text, createdAt));
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка получения комментариев", e);
        }
        return comments;
    }

    /**
     * Удалить комментарий
     */
    public static boolean deleteComment(int commentId, int userId, boolean isAdmin) {
        // Проверяем права: админ может удалить любой, пользователь - только свой
        String sql = isAdmin ?
            "DELETE FROM comments_ratings WHERE id = ?" :
            "DELETE FROM comments_ratings WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, commentId);
            if (!isAdmin) {
                stmt.setInt(2, userId);
            }

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                LoggerUtil.action("Удалён комментарий ID=" + commentId);
                return true;
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка удаления комментария", e);
        }
        return false;
    }

    /**
     * Получить количество комментариев к авто
     */
    public static int getCommentsCount(int carId) {
        String sql = "SELECT COUNT(*) FROM comments_ratings WHERE car_id = ? AND comment IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка подсчёта комментариев", e);
        }
        return 0;
    }

    // ========== ОЦЕНКИ ==========

    /**
     * Добавить или обновить оценку автомобиля
     */
    public static boolean setRating(int carId, int userId, int rating) {
        if (rating < 1 || rating > 5) {
            return false;
        }

        // Проверяем, есть ли уже запись
        String checkSql = "SELECT id FROM comments_ratings WHERE car_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, carId);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Обновляем существующую запись
                String updateSql = "UPDATE comments_ratings SET rating = ? WHERE car_id = ? AND user_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, rating);
                    updateStmt.setInt(2, carId);
                    updateStmt.setInt(3, userId);
                    updateStmt.executeUpdate();
                }
            } else {
                // Вставляем новую запись
                String insertSql = "INSERT INTO comments_ratings (car_id, user_id, rating) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, carId);
                    insertStmt.setInt(2, userId);
                    insertStmt.setInt(3, rating);
                    insertStmt.executeUpdate();
                }
            }

            LoggerUtil.action("Оценка " + rating + " поставлена для авто ID=" + carId);
            return true;
        } catch (Exception e) {
            LoggerUtil.error("Ошибка установки оценки", e);
        }
        return false;
    }

    /**
     * Получить оценку пользователя для автомобиля
     */
    public static int getUserRating(int carId, int userId) {
        String sql = "SELECT rating FROM comments_ratings WHERE car_id = ? AND user_id = ? AND rating IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rating");
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка получения оценки пользователя", e);
        }
        return 0;
    }

    /**
     * Получить среднюю оценку автомобиля
     */
    public static double getAverageRating(int carId) {
        String sql = "SELECT AVG(CAST(rating AS FLOAT)) FROM comments_ratings WHERE car_id = ? AND rating IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка получения средней оценки", e);
        }
        return 0.0;
    }

    /**
     * Получить количество оценок автомобиля
     */
    public static int getRatingsCount(int carId) {
        String sql = "SELECT COUNT(*) FROM comments_ratings WHERE car_id = ? AND rating IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка подсчёта оценок", e);
        }
        return 0;
    }
}

