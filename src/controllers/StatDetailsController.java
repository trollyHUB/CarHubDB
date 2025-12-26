package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.StatDetailsItem;
import services.StatisticsService;
import utils.LoggerUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Контроллер для отображения детальной информации по статистике
 */
public class StatDetailsController {

    @FXML private Label titleLabel;
    @FXML private TableView<StatDetailsItem> detailsTable;
    @FXML private TableColumn<StatDetailsItem, Integer> colId;
    @FXML private TableColumn<StatDetailsItem, String> colInfo1;
    @FXML private TableColumn<StatDetailsItem, String> colInfo2;
    @FXML private TableColumn<StatDetailsItem, String> colInfo3;
    @FXML private TableColumn<StatDetailsItem, String> colDate;
    @FXML private Label totalLabel;

    private String statType;

    public void setStatType(String type) {
        this.statType = type;
        loadDetails();
    }

    private void loadDetails() {
        if (statType == null) return;

        LoggerUtil.action("Открыта детальная статистика: " + statType);

        switch (statType) {
            case "favorites":
                showFavoritesDetails();
                break;
            case "reservations":
                showReservationsDetails();
                break;
            case "purchases":
                showPurchasesDetails();
                break;
            case "comments":
                showCommentsDetails();
                break;
            case "ratings":
                showRatingsDetails();
                break;
            case "admins":
                showAdminsDetails();
                break;
            case "active_users":
                showActiveUsersDetails();
                break;
            case "inactive_users":
                showInactiveUsersDetails();
                break;
            default:
                titleLabel.setText("Неизвестный тип");
        }
    }

    private void showFavoritesDetails() {
        titleLabel.setText("❤️ Детальная информация: Избранные автомобили");

        // Настройка колонок
        colId.setText("ID");
        colInfo1.setText("Пользователь");
        colInfo2.setText("Автомобиль");
        colInfo3.setText("Бренд");
        colDate.setText("Дата добавления");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInfo1.setCellValueFactory(new PropertyValueFactory<>("info1"));
        colInfo2.setCellValueFactory(new PropertyValueFactory<>("info2"));
        colInfo3.setCellValueFactory(new PropertyValueFactory<>("info3"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Загрузка данных
        ObservableList<StatDetailsItem> items = StatisticsService.getFavoritesDetails();
        detailsTable.setItems(items);
        totalLabel.setText("Всего записей: " + items.size());
    }

    private void showReservationsDetails() {
        titleLabel.setText("📅 Детальная информация: Бронирования");

        colId.setText("ID");
        colInfo1.setText("Клиент");
        colInfo2.setText("Автомобиль");
        colInfo3.setText("Статус");
        colDate.setText("Дата бронирования");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInfo1.setCellValueFactory(new PropertyValueFactory<>("info1"));
        colInfo2.setCellValueFactory(new PropertyValueFactory<>("info2"));
        colInfo3.setCellValueFactory(new PropertyValueFactory<>("info3"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        ObservableList<StatDetailsItem> items = StatisticsService.getReservationsDetails();
        detailsTable.setItems(items);
        totalLabel.setText("Всего записей: " + items.size());
    }

    private void showPurchasesDetails() {
        titleLabel.setText("🛒 Детальная информация: Покупки");

        colId.setText("ID");
        colInfo1.setText("Клиент");
        colInfo2.setText("Автомобиль");
        colInfo3.setText("Статус");
        colDate.setText("Дата покупки");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInfo1.setCellValueFactory(new PropertyValueFactory<>("info1"));
        colInfo2.setCellValueFactory(new PropertyValueFactory<>("info2"));
        colInfo3.setCellValueFactory(new PropertyValueFactory<>("info3"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        ObservableList<StatDetailsItem> items = StatisticsService.getPurchasesDetails();
        detailsTable.setItems(items);
        totalLabel.setText("Всего записей: " + items.size());
    }

    private void showCommentsDetails() {
        titleLabel.setText("💬 Детальная информация: Комментарии");

        colId.setText("ID");
        colInfo1.setText("Пользователь");
        colInfo2.setText("Автомобиль");
        colInfo3.setText("Комментарий");
        colDate.setText("Дата");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInfo1.setCellValueFactory(new PropertyValueFactory<>("info1"));
        colInfo2.setCellValueFactory(new PropertyValueFactory<>("info2"));
        colInfo3.setCellValueFactory(new PropertyValueFactory<>("info3"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        ObservableList<StatDetailsItem> items = StatisticsService.getCommentsDetails();
        detailsTable.setItems(items);
        totalLabel.setText("Всего записей: " + items.size());
    }

    private void showRatingsDetails() {
        titleLabel.setText("⭐ Детальная информация: Оценки");

        colId.setText("ID");
        colInfo1.setText("Пользователь");
        colInfo2.setText("Автомобиль");
        colInfo3.setText("Оценка");
        colDate.setText("Дата");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInfo1.setCellValueFactory(new PropertyValueFactory<>("info1"));
        colInfo2.setCellValueFactory(new PropertyValueFactory<>("info2"));
        colInfo3.setCellValueFactory(new PropertyValueFactory<>("info3"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        ObservableList<StatDetailsItem> items = StatisticsService.getRatingsDetails();
        detailsTable.setItems(items);
        totalLabel.setText("Всего записей: " + items.size());
    }

    private void showAdminsDetails() {
        titleLabel.setText("👑 Детальная информация: Администраторы");

        colId.setText("ID");
        colInfo1.setText("Логин");
        colInfo2.setText("Полное имя");
        colInfo3.setText("Роль");
        colDate.setText("Дата регистрации");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInfo1.setCellValueFactory(new PropertyValueFactory<>("info1"));
        colInfo2.setCellValueFactory(new PropertyValueFactory<>("info2"));
        colInfo3.setCellValueFactory(new PropertyValueFactory<>("info3"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        ObservableList<StatDetailsItem> items = StatisticsService.getAdminsDetails();
        detailsTable.setItems(items);
        totalLabel.setText("Всего записей: " + items.size());
    }

    private void showActiveUsersDetails() {
        titleLabel.setText("✅ Детальная информация: Активные пользователи");

        colId.setText("ID");
        colInfo1.setText("Логин");
        colInfo2.setText("Полное имя");
        colInfo3.setText("Роль");
        colDate.setText("Дата регистрации");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInfo1.setCellValueFactory(new PropertyValueFactory<>("info1"));
        colInfo2.setCellValueFactory(new PropertyValueFactory<>("info2"));
        colInfo3.setCellValueFactory(new PropertyValueFactory<>("info3"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        ObservableList<StatDetailsItem> items = StatisticsService.getActiveUsersDetails();
        detailsTable.setItems(items);
        totalLabel.setText("Всего записей: " + items.size());
    }

    private void showInactiveUsersDetails() {
        titleLabel.setText("❌ Детальная информация: Неактивные пользователи");

        colId.setText("ID");
        colInfo1.setText("Логин");
        colInfo2.setText("Полное имя");
        colInfo3.setText("Роль");
        colDate.setText("Дата регистрации");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInfo1.setCellValueFactory(new PropertyValueFactory<>("info1"));
        colInfo2.setCellValueFactory(new PropertyValueFactory<>("info2"));
        colInfo3.setCellValueFactory(new PropertyValueFactory<>("info3"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        ObservableList<StatDetailsItem> items = StatisticsService.getInactiveUsersDetails();
        detailsTable.setItems(items);
        totalLabel.setText("Всего записей: " + items.size());
    }

    @FXML
    protected void closeWindow() {
        Stage stage = (Stage) detailsTable.getScene().getWindow();
        stage.close();
    }
}

