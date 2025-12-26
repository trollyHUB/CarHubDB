package controllers;

import database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import utils.PasswordUtil;
import utils.LoggerUtil;
import utils.NotificationUtil;

public class RegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField fullnameField;
    @FXML
    private Label messageLabel;

    @FXML
    protected void onRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String fullname = fullnameField.getText();

        if (username.isBlank() || password.isBlank()) {
            messageLabel.setText("❌ Заполните логин и пароль");
            NotificationUtil.showWarning("Заполните все обязательные поля!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                messageLabel.setText("❌ Нет подключения к БД");
                NotificationUtil.showError("Нет подключения к базе данных!");
                return;
            }

            // Регистрация в users_secure
            String salt = PasswordUtil.generateSaltHex(16);
            String hash = PasswordUtil.hashPassword(password, salt);
            String sql = "INSERT INTO users_secure (username, password_hash, salt, fullname, role, is_active) VALUES (?, ?, ?, ?, 'user', 1)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hash);
            stmt.setString(3, salt);
            stmt.setString(4, fullname);
            stmt.executeUpdate();

            messageLabel.setText("✅ Регистрация прошла успешно!");
            NotificationUtil.showSuccess("Регистрация завершена! Теперь можете войти в систему.");
            LoggerUtil.action("Зарегистрирован новый пользователь: " + username);

        } catch (Exception e) {
            messageLabel.setText("❌ Ошибка регистрации: " + e.getMessage());
            if (e.getMessage().contains("duplicate") || e.getMessage().contains("UNIQUE")) {
                NotificationUtil.showError("Этот логин уже занят! Выберите другой.");
            } else {
                NotificationUtil.showError("Ошибка регистрации: " + e.getMessage());
            }
        }
    }

    private boolean hasColumn(ResultSetMetaData meta, String col) throws java.sql.SQLException {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (meta.getColumnLabel(i).equalsIgnoreCase(col)) return true;
        }
        return false;
    }

    @FXML
    protected void backToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Фиксированный размер
            stage.setWidth(600);
            stage.setHeight(700);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
