package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import models.User;
import database.DatabaseConnection;
import utils.SessionManager;
import utils.LoggerUtil;
import utils.PasswordUtil;

import java.sql.*;

public class UsersManagementController {

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> colId;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colFullname;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private TableColumn<User, Boolean> colActive;
    @FXML
    private TableColumn<User, Void> colActions;
    @FXML
    private TextField searchField;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalAdminsLabel;
    @FXML
    private Label activeUsersLabel;

    private final ObservableList<User> usersList = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;

    public void initialize() {
        initializeTable();
        loadUsers();
        updateStatistics();
        setupSearch();
    }

    private void initializeTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullname.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Колонка статуса - ВАЖНО: сначала устанавливаем источник данных!
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        // Затем настраиваем отображение
        colActive.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(active ? "✅ Активен" : "❌ Неактивен");
                    label.setStyle(active ?
                        "-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 13px;" :
                        "-fx-text-fill: #F44336; -fx-font-weight: bold; -fx-font-size: 13px;");
                    setGraphic(label);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Колонка действий
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Изменить");
            private final Button deleteBtn = new Button("Удалить");
            private final Button toggleBtn = new Button("Статус");
            private final HBox buttons = new HBox(5, editBtn, toggleBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-font-size: 12px; -fx-background-radius: 4;");
                deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-font-size: 12px; -fx-background-radius: 4;");
                toggleBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-font-size: 12px; -fx-background-radius: 4;");

                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });

                toggleBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    toggleUserStatus(user);
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });

                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        usersTable.setItems(usersList);
    }

    private void loadUsers() {
        usersList.clear();

        String sql = "SELECT id, username, fullname, role, is_active FROM users_secure ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    "",
                    rs.getString("fullname"),
                    rs.getString("role")
                );
                user.setActive(rs.getBoolean("is_active"));
                usersList.add(user);
            }

            LoggerUtil.action("Загружено пользователей: " + usersList.size());
        } catch (SQLException e) {
            LoggerUtil.error("Ошибка загрузки пользователей", e);
            showError("Ошибка загрузки данных", e.getMessage());
        }
    }

    private void updateStatistics() {
        int totalUsers = (int) usersList.stream().filter(u -> "user".equals(u.getRole())).count();
        int totalAdmins = (int) usersList.stream().filter(u -> "admin".equals(u.getRole())).count();
        int activeUsers = (int) usersList.stream().filter(User::isActive).count();

        totalUsersLabel.setText(String.valueOf(totalUsers));
        totalAdminsLabel.setText(String.valueOf(totalAdmins));
        activeUsersLabel.setText(String.valueOf(activeUsers));
    }

    private void setupSearch() {
        filteredUsers = new FilteredList<>(usersList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) ->
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return user.getUsername().toLowerCase().contains(lowerCaseFilter) ||
                       (user.getFullname() != null && user.getFullname().toLowerCase().contains(lowerCaseFilter));
            })
        );

        usersTable.setItems(filteredUsers);
    }

    @FXML
    protected void addUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Добавить пользователя");
        dialog.setHeaderText("Создание нового пользователя");

        ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Логин");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");

        TextField fullnameField = new TextField();
        fullnameField.setPromptText("Имя пользователя");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("user", "admin");
        roleCombo.setValue("user");

        grid.add(new Label("Логин:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Пароль:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Имя:"), 0, 2);
        grid.add(fullnameField, 1, 2);
        grid.add(new Label("Роль:"), 0, 3);
        grid.add(roleCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String fullname = fullnameField.getText().trim();
                String role = roleCombo.getValue();

                if (username.isEmpty() || password.isEmpty() || fullname.isEmpty()) {
                    showError("Ошибка", "Все поля обязательны для заполнения");
                    return null;
                }

                return createUser(username, password, fullname, role);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            if (user != null) {
                loadUsers();
                updateStatistics();
            }
        });
    }

    private User createUser(String username, String password, String fullname, String role) {
        String salt = PasswordUtil.generateSaltHex(16);
        String hashedPassword = PasswordUtil.hashPassword(password, salt);

        String sql = "INSERT INTO users_secure (username, password_hash, salt, fullname, role, is_active) VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, salt);
            pstmt.setString(4, fullname);
            pstmt.setString(5, role);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    LoggerUtil.action("Создан пользователь: " + username);
                    showInfo("Успешно", "Пользователь " + username + " создан!");

                    User user = new User(id, username, "", fullname, role);
                    user.setActive(true);
                    return user;
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Ошибка создания пользователя", e);
            showError("Ошибка создания", "Возможно, такой логин уже существует");
        }

        return null;
    }

    private void editUser(User user) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Редактировать пользователя");
        dialog.setHeaderText("Редактирование: " + user.getUsername());

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField usernameField = new TextField(user.getUsername());
        TextField fullnameField = new TextField(user.getFullname());

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Оставьте пустым, если не менять");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("user", "admin");
        roleCombo.setValue(user.getRole());

        grid.add(new Label("Логин:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Имя:"), 0, 1);
        grid.add(fullnameField, 1, 1);
        grid.add(new Label("Новый пароль:"), 0, 2);
        grid.add(newPasswordField, 1, 2);
        grid.add(new Label("Роль:"), 0, 3);
        grid.add(roleCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String newUsername = usernameField.getText().trim();
                String newFullname = fullnameField.getText().trim();
                String newPassword = newPasswordField.getText();
                String newRole = roleCombo.getValue();

                if (newUsername.isEmpty() || newFullname.isEmpty()) {
                    showError("Ошибка", "Логин и имя не могут быть пустыми!");
                    return false;
                }

                return updateUser(user.getId(), newUsername, newFullname, newPassword.isEmpty() ? null : newPassword, newRole);
            }
            return false;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                loadUsers();
                updateStatistics();
            }
        });
    }

    private boolean updateUser(int userId, String username, String fullname, String password, String role) {
        StringBuilder sql = new StringBuilder("UPDATE users_secure SET username = ?, fullname = ?, role = ?");
        boolean updatePassword = password != null && !password.isEmpty();

        if (updatePassword) {
            sql.append(", password_hash = ?, salt = ?");
        }

        sql.append(" WHERE id = ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setString(1, username);
            pstmt.setString(2, fullname);
            pstmt.setString(3, role);

            if (updatePassword) {
                String salt = PasswordUtil.generateSaltHex(16);
                String hashedPassword = PasswordUtil.hashPassword(password, salt);
                pstmt.setString(4, hashedPassword);
                pstmt.setString(5, salt);
                pstmt.setInt(6, userId);
            } else {
                pstmt.setInt(4, userId);
            }

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                LoggerUtil.action("Обновлён пользователь ID: " + userId + " (новый логин: " + username + ")");
                showInfo("Успешно", "Данные пользователя обновлены!");
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Ошибка обновления пользователя", e);
            if (e.getMessage().contains("UNIQUE") || e.getMessage().contains("duplicate")) {
                showError("Ошибка обновления", "Такой логин уже существует!");
            } else {
                showError("Ошибка обновления", e.getMessage());
            }
        }

        return false;
    }

    private void toggleUserStatus(User user) {
        boolean newStatus = !user.isActive();

        String sql = "UPDATE users_secure SET is_active = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, newStatus);
            pstmt.setInt(2, user.getId());

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                user.setActive(newStatus);
                LoggerUtil.action((newStatus ? "Активирован" : "Деактивирован") + " пользователь: " + user.getUsername());
                showInfo("Успешно", "Статус пользователя изменён!");
                usersTable.refresh();
                updateStatistics();
            }
        } catch (SQLException e) {
            LoggerUtil.error("Ошибка изменения статуса", e);
            showError("Ошибка", e.getMessage());
        }
    }

    private void deleteUser(User user) {
        if (user.getId() == SessionManager.getCurrentUserId()) {
            showError("Ошибка", "Нельзя удалить себя!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить пользователя " + user.getUsername() + "?");
        confirm.setContentText("Это действие нельзя отменить!");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM users_secure WHERE id = ?";

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setInt(1, user.getId());
                    int affected = pstmt.executeUpdate();

                    if (affected > 0) {
                        LoggerUtil.action("Удалён пользователь: " + user.getUsername());
                        showInfo("Успешно", "Пользователь удалён!");
                        loadUsers();
                        updateStatistics();
                    }
                } catch (SQLException e) {
                    LoggerUtil.error("Ошибка удаления пользователя", e);
                    showError("Ошибка удаления", e.getMessage());
                }
            }
        });
    }

    @FXML
    protected void backToMain() {
        try {
            String fxmlFile = SessionManager.isAdmin() ?
                "/resources/carhub-admin-view.fxml" :
                "/resources/carhub-user-view.fxml";

            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) usersTable.getScene().getWindow();
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
            Stage stage = (Stage) usersTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Вход");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при выходе", e);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

