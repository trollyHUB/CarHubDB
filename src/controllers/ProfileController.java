package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import database.DatabaseConnection;
import utils.PasswordUtil;
import utils.SessionManager;
import utils.LoggerUtil;
import utils.NotificationUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class ProfileController {

    @FXML
    private ImageView avatarImage;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label fullnameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField fullnameField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label profileMessageLabel;
    @FXML
    private Label passwordMessageLabel;
    @FXML
    private Label favoritesCountLabel;
    @FXML
    private Label commentsCountLabel;
    @FXML
    private Label reservationsCountLabel;

    private String currentAvatarPath;

    public void initialize() {
        loadUserProfile();
        loadUserStatistics();
    }

    private void loadUserProfile() {
        int userId = SessionManager.getCurrentUserId();
        String username = SessionManager.getCurrentUsername();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT username, fullname, role, is_active, avatar_path FROM users_secure WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String fullname = rs.getString("fullname");
                String role = rs.getString("role");
                boolean isActive = rs.getBoolean("is_active");
                currentAvatarPath = rs.getString("avatar_path");

                // Заголовок
                usernameLabel.setText("@" + username);
                fullnameLabel.setText(fullname);

                // Роль
                if ("admin".equalsIgnoreCase(role)) {
                    roleLabel.setText("👑 Администратор");
                    roleLabel.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #FF9800; -fx-padding: 5 15; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: 600;");
                } else {
                    roleLabel.setText("👤 Пользователь");
                }

                // Статус
                if (isActive) {
                    statusLabel.setText("✅ Активен");
                } else {
                    statusLabel.setText("❌ Неактивен");
                    statusLabel.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #F44336; -fx-padding: 5 15; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: 600;");
                }

                // Поля редактирования
                fullnameField.setText(fullname);
                usernameField.setText(username);

                // Аватар
                loadAvatar();

                LoggerUtil.action("Загружен профиль пользователя: " + username);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Ошибка загрузки профиля", e);
            showProfileError("Ошибка загрузки профиля: " + e.getMessage());
        }
    }

    private void loadAvatar() {
        if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
            File avatarFile = new File(currentAvatarPath);
            if (avatarFile.exists()) {
                try {
                    Image image = new Image(avatarFile.toURI().toString());

                    // Масштабируем изображение чтобы покрыть весь круг
                    double imageWidth = image.getWidth();
                    double imageHeight = image.getHeight();
                    double targetSize = 150.0; // Размер круга

                    // Вычисляем viewport для центрирования
                    if (imageWidth > 0 && imageHeight > 0) {
                        double scale = Math.max(targetSize / imageWidth, targetSize / imageHeight);
                        double scaledWidth = imageWidth * scale;
                        double scaledHeight = imageHeight * scale;

                        // Центрируем изображение
                        double offsetX = (scaledWidth - targetSize) / 2.0 / scale;
                        double offsetY = (scaledHeight - targetSize) / 2.0 / scale;

                        avatarImage.setViewport(new javafx.geometry.Rectangle2D(
                            offsetX, offsetY,
                            targetSize / scale, targetSize / scale
                        ));
                    }

                    avatarImage.setImage(image);
                    return;
                } catch (Exception e) {
                    LoggerUtil.warning("Не удалось загрузить аватар: " + e.getMessage());
                }
            }
        }

        // Аватар по умолчанию
        try {
            Image defaultAvatar = new Image(getClass().getResourceAsStream("/resources/images/default-avatar.png"));
            avatarImage.setImage(defaultAvatar);
        } catch (Exception e) {
            LoggerUtil.warning("Аватар по умолчанию не найден");
        }
    }

    private void loadUserStatistics() {
        int userId = SessionManager.getCurrentUserId();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Избранные
            String favSql = "SELECT COUNT(*) FROM favorites WHERE user_id = ?";
            PreparedStatement favStmt = conn.prepareStatement(favSql);
            favStmt.setInt(1, userId);
            ResultSet favRs = favStmt.executeQuery();
            if (favRs.next()) {
                favoritesCountLabel.setText(String.valueOf(favRs.getInt(1)));
            }

            // Комментарии
            String commentsSql = "SELECT COUNT(*) FROM comments_ratings WHERE user_id = ?";
            PreparedStatement commentsStmt = conn.prepareStatement(commentsSql);
            commentsStmt.setInt(1, userId);
            ResultSet commentsRs = commentsStmt.executeQuery();
            if (commentsRs.next()) {
                commentsCountLabel.setText(String.valueOf(commentsRs.getInt(1)));
            }

            // Бронирования
            String resSql = "SELECT COUNT(*) FROM reservations WHERE user_id = ?";
            PreparedStatement resStmt = conn.prepareStatement(resSql);
            resStmt.setInt(1, userId);
            ResultSet resRs = resStmt.executeQuery();
            if (resRs.next()) {
                reservationsCountLabel.setText(String.valueOf(resRs.getInt(1)));
            }

        } catch (SQLException e) {
            LoggerUtil.warning("Ошибка загрузки статистики: " + e.getMessage());
        }
    }

    @FXML
    protected void uploadAvatar(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите аватар");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) avatarImage.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Создаём папку для аватаров если не существует
                File avatarsDir = new File("avatars");
                if (!avatarsDir.exists()) {
                    avatarsDir.mkdir();
                }

                // Копируем файл
                String fileName = SessionManager.getCurrentUserId() + "_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(avatarsDir, fileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Сохраняем путь в БД
                String avatarPath = destFile.getAbsolutePath();
                updateAvatarPath(avatarPath);

                // Обновляем отображение
                Image image = new Image(destFile.toURI().toString());

                // Масштабируем изображение чтобы покрыть весь круг
                double imageWidth = image.getWidth();
                double imageHeight = image.getHeight();
                double targetSize = 150.0;

                if (imageWidth > 0 && imageHeight > 0) {
                    double scale = Math.max(targetSize / imageWidth, targetSize / imageHeight);
                    double scaledWidth = imageWidth * scale;
                    double scaledHeight = imageHeight * scale;

                    double offsetX = (scaledWidth - targetSize) / 2.0 / scale;
                    double offsetY = (scaledHeight - targetSize) / 2.0 / scale;

                    avatarImage.setViewport(new javafx.geometry.Rectangle2D(
                        offsetX, offsetY,
                        targetSize / scale, targetSize / scale
                    ));
                }

                avatarImage.setImage(image);
                currentAvatarPath = avatarPath;

                showProfileSuccess("✅ Аватар обновлён!");
                NotificationUtil.showSuccess("Аватар успешно обновлён!");
                LoggerUtil.action("Обновлён аватар пользователя: " + SessionManager.getCurrentUsername());

            } catch (IOException e) {
                LoggerUtil.error("Ошибка загрузки аватара", e);
                showProfileError("❌ Не удалось загрузить аватар: " + e.getMessage());
            }
        }
    }

    private void updateAvatarPath(String avatarPath) {
        int userId = SessionManager.getCurrentUserId();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users_secure SET avatar_path = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, avatarPath);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Ошибка сохранения пути аватара", e);
        }
    }

    @FXML
    protected void saveProfile(ActionEvent event) {
        String newUsername = usernameField.getText().trim();
        String newFullname = fullnameField.getText().trim();

        if (newUsername.isEmpty() || newFullname.isEmpty()) {
            showProfileError("❌ Логин и имя не могут быть пустыми!");
            return;
        }

        int userId = SessionManager.getCurrentUserId();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users_secure SET username = ?, fullname = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newUsername);
            pstmt.setString(2, newFullname);
            pstmt.setInt(3, userId);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                // Обновляем сессию
                SessionManager.setUsername(newUsername);

                usernameLabel.setText("@" + newUsername);
                fullnameLabel.setText(newFullname);
                showProfileSuccess("✅ Профиль обновлён!");
                NotificationUtil.showSuccess("Профиль успешно обновлён!");
                LoggerUtil.action("Обновлён профиль пользователя: " + newUsername);
            } else {
                showProfileError("❌ Не удалось обновить профиль");
                NotificationUtil.showError("Не удалось обновить профиль");
            }

        } catch (SQLException e) {
            LoggerUtil.error("Ошибка обновления профиля", e);
            if (e.getMessage().contains("UNIQUE") || e.getMessage().contains("duplicate")) {
                showProfileError("❌ Логин уже занят!");
            } else {
                showProfileError("❌ Ошибка: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void changePassword(ActionEvent event) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Валидация
        if (currentPassword.isEmpty()) {
            showPasswordError("❌ Введите текущий пароль!");
            return;
        }

        if (newPassword.isEmpty()) {
            showPasswordError("❌ Введите новый пароль!");
            return;
        }

        if (newPassword.length() < 6) {
            showPasswordError("❌ Новый пароль должен быть минимум 6 символов!");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showPasswordError("❌ Пароли не совпадают!");
            return;
        }

        // Проверяем текущий пароль
        int userId = SessionManager.getCurrentUserId();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Получаем текущий хеш и соль
            String getSql = "SELECT password_hash, salt FROM users_secure WHERE id = ?";
            PreparedStatement getStmt = conn.prepareStatement(getSql);
            getStmt.setInt(1, userId);
            ResultSet rs = getStmt.executeQuery();

            if (!rs.next()) {
                showPasswordError("❌ Пользователь не найден!");
                return;
            }

            String storedHash = rs.getString("password_hash");
            String salt = rs.getString("salt");

            // Проверяем текущий пароль
            if (!PasswordUtil.verifyPassword(currentPassword, salt, storedHash)) {
                showPasswordError("❌ Неверный текущий пароль!");
                LoggerUtil.warning("Неудачная попытка смены пароля (неверный текущий): " + SessionManager.getCurrentUsername());
                return;
            }

            // Генерируем новый хеш
            String newSalt = PasswordUtil.generateSaltHex(16);
            String newHash = PasswordUtil.hashPassword(newPassword, newSalt);

            // Обновляем пароль
            String updateSql = "UPDATE users_secure SET password_hash = ?, salt = ?, updated_at = GETDATE() WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, newHash);
            updateStmt.setString(2, newSalt);
            updateStmt.setInt(3, userId);

            int affected = updateStmt.executeUpdate();

            if (affected > 0) {
                showPasswordSuccess("✅ Пароль успешно изменён!");
                LoggerUtil.action("Изменён пароль пользователя: " + SessionManager.getCurrentUsername());

                // Очищаем поля
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            } else {
                showPasswordError("❌ Не удалось изменить пароль");
            }

        } catch (SQLException e) {
            LoggerUtil.error("Ошибка смены пароля", e);
            showPasswordError("❌ Ошибка: " + e.getMessage());
        }
    }

    @FXML
    protected void backToMain() {
        try {
            String fxmlFile = SessionManager.isAdmin() ?
                "/resources/carhub-admin-view.fxml" :
                "/resources/carhub-user-view.fxml";

            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) avatarImage.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка возврата", e);
        }
    }

    @FXML
    protected void logout() {
        try {
            String username = SessionManager.getCurrentUsername();
            LoggerUtil.logLogout(username != null ? username : "Неизвестный");
            SessionManager.logout();

            Parent root = FXMLLoader.load(getClass().getResource("/resources/login-view.fxml"));
            Stage stage = (Stage) avatarImage.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Вход");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при выходе", e);
        }
    }

    private void showProfileSuccess(String message) {
        profileMessageLabel.setText(message);
        profileMessageLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: 600;");
    }

    private void showProfileError(String message) {
        profileMessageLabel.setText(message);
        profileMessageLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: 600;");
    }

    private void showPasswordSuccess(String message) {
        passwordMessageLabel.setText(message);
        passwordMessageLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: 600;");
    }

    private void showPasswordError(String message) {
        passwordMessageLabel.setText(message);
        passwordMessageLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: 600;");
    }
}

