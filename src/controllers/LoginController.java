package controllers;

import database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import utils.PasswordUtil;
import utils.SessionManager;
import utils.LoggerUtil;
import utils.NotificationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        // Обработка Enter в поле логина
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                onLogin(null);
            }
        });

        // Обработка Enter в поле пароля
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                onLogin(null);
            }
        });
    }

    @FXML
    protected void onLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                messageLabel.setText("❌ Нет подключения к базе данных");
                return;
            }

            // Ищем пользователя в users_secure
            String query = "SELECT * FROM users_secure WHERE username = ? AND is_active = 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                messageLabel.setText("❌ Неверный логин или пароль!");
                NotificationUtil.showError("Неверный логин или пароль!");
                LoggerUtil.warning("Неудачная попытка входа: " + username);
                return;
            }

            // Проверяем пароль
            String salt = rs.getString("salt");
            String storedHash = rs.getString("password_hash");

            if (!PasswordUtil.verifyPassword(password, salt, storedHash)) {
                messageLabel.setText("❌ Неверный логин или пароль!");
                NotificationUtil.showError("Неверный логин или пароль!");
                LoggerUtil.warning("Неудачная попытка входа (неверный пароль): " + username);
                return;
            }

            // Успешный вход
            int userId = rs.getInt("id");
            String role = rs.getString("role");

            SessionManager.login(userId, username, role);
            LoggerUtil.logLogin(username, role);

            // Показываем успешное уведомление
            NotificationUtil.showSuccess("Добро пожаловать, " + username + "!");

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Parent root;

            if ("admin".equalsIgnoreCase(role)) {
                CarHubController.setAdminMode(true);
                root = FXMLLoader.load(getClass().getResource("/resources/carhub-admin-view.fxml"));
                stage.setTitle("CarHub — Панель администратора");
            } else {
                CarHubController.setAdminMode(false);
                root = FXMLLoader.load(getClass().getResource("/resources/carhub-user-view.fxml"));
                stage.setTitle("CarHub — Каталог автомобилей");
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Фиксированный размер окна
            stage.setWidth(1400);
            stage.setHeight(900);
            stage.setResizable(true);
            stage.centerOnScreen();

            stage.show();

        } catch (Exception e) {
            LoggerUtil.error("Ошибка при входе пользователя " + username, e);
            messageLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    private boolean hasColumn(ResultSetMetaData meta, String col) throws java.sql.SQLException {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (meta.getColumnLabel(i).equalsIgnoreCase(col)) return true;
        }
        return false;
    }

    @FXML
    protected void openRegisterPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/register-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Регистрация");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void openForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/forgot-password-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("CarHub — Восстановление пароля");

            // Фиксированный размер
            stage.setWidth(600);
            stage.setHeight(600);
            stage.centerOnScreen();

            LoggerUtil.action("Открыт экран восстановления пароля");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия экрана восстановления пароля", e);
            e.printStackTrace();
        }
    }

    @FXML
    protected void backToLanding(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/landing-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("CarHub — Premium Auto");
            stage.setMaximized(true);
            LoggerUtil.action("Возврат на главную страницу");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка возврата на главную", e);
            e.printStackTrace();
        }
    }
}
