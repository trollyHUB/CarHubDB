package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import database.DatabaseConnection;
import utils.PasswordUtil;
import utils.LoggerUtil;

import java.sql.*;
import java.security.SecureRandom;

public class ForgotPasswordController {

    @FXML
    private TextField usernameField;
    @FXML
    private Label messageLabel;
    @FXML
    private TextField tempPasswordField;
    @FXML
    private VBox tempPasswordBox;
    @FXML
    private Button resetButton;
    @FXML
    private Button loginButton;
    @FXML
    private Button copyButton;

    private String generatedPassword;

    @FXML
    protected void onResetPassword(ActionEvent event) {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            showError("❌ Введите логин!");
            return;
        }

        // Проверяем существует ли пользователь
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showError("❌ Нет подключения к базе данных");
                return;
            }

            String checkSql = "SELECT id, fullname FROM users_secure WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                showError("❌ Пользователь с таким логином не найден!");
                LoggerUtil.warning("Попытка восстановления пароля для несуществующего пользователя: " + username);
                return;
            }

            String fullname = rs.getString("fullname");

            // Генерируем новый временный пароль
            generatedPassword = generateTemporaryPassword();

            // Хешируем пароль
            String salt = PasswordUtil.generateSaltHex(16);
            String hashedPassword = PasswordUtil.hashPassword(generatedPassword, salt);

            // Обновляем пароль в БД
            String updateSql = "UPDATE users_secure SET password_hash = ?, salt = ?, updated_at = GETDATE() WHERE username = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, hashedPassword);
            updateStmt.setString(2, salt);
            updateStmt.setString(3, username);

            int affected = updateStmt.executeUpdate();

            if (affected > 0) {
                // Успешно! Показываем временный пароль
                showSuccess(fullname);
                LoggerUtil.action("Восстановлен пароль для пользователя: " + username);
            } else {
                showError("❌ Не удалось обновить пароль. Попробуйте позже.");
                LoggerUtil.error("Не удалось обновить пароль для: " + username, null);
            }

        } catch (SQLException e) {
            LoggerUtil.error("Ошибка восстановления пароля для " + username, e);
            showError("❌ Ошибка базы данных: " + e.getMessage());
        }
    }

    private String generateTemporaryPassword() {
        // Генерируем случайный пароль из 8 символов
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: 600; -fx-font-size: 14px;");
        tempPasswordBox.setVisible(false);
        tempPasswordBox.setManaged(false);
        loginButton.setVisible(false);
        loginButton.setManaged(false);
    }

    private void showSuccess(String fullname) {
        messageLabel.setText("✅ Пароль успешно сброшен, " + fullname + "!");
        messageLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: 600; -fx-font-size: 14px;");

        // Показываем блок с временным паролем
        tempPasswordField.setText(generatedPassword);
        tempPasswordBox.setVisible(true);
        tempPasswordBox.setManaged(true);

        // Показываем кнопку входа
        loginButton.setVisible(true);
        loginButton.setManaged(true);

        // Скрываем кнопку сброса
        resetButton.setVisible(false);
        resetButton.setManaged(false);

        // Отключаем поле логина
        usernameField.setDisable(true);

        // Анимация: моргание временного пароля
        highlightPassword();
    }

    private void highlightPassword() {
        // Эффект привлечения внимания к паролю
        tempPasswordField.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10; " +
            "-fx-background-color: #FFFACD; " +
            "-fx-border-color: #FFC107; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-effect: dropshadow(gaussian, rgba(255,193,7,0.6), 10, 0, 0, 0);"
        );

        // Через 3 секунды убираем эффект
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> tempPasswordField.setStyle(
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 10; " +
                    "-fx-background-color: white; " +
                    "-fx-border-color: #FFC107; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 5; " +
                    "-fx-background-radius: 5;"
                ));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    protected void copyPassword(ActionEvent event) {
        // Копируем пароль в буфер обмена
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(generatedPassword);
        clipboard.setContent(content);

        // Меняем иконку кнопки на галочку
        copyButton.setText("✅");
        copyButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 10 15; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );

        // Через 2 секунды возвращаем иконку
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    copyButton.setText("📋");
                    copyButton.setStyle(
                        "-fx-background-color: #FFC107; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 10 15; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
                    );
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        LoggerUtil.action("Пароль скопирован в буфер обмена для пользователя: " + usernameField.getText());
    }

    @FXML
    protected void goToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Вход");

            LoggerUtil.action("Переход на страницу входа после восстановления пароля");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка перехода на страницу входа", e);
            e.printStackTrace();
        }
    }

    @FXML
    protected void backToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Вход");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка возврата на страницу входа", e);
            e.printStackTrace();
        }
    }
}

