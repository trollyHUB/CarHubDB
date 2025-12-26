package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Purchase;
import models.Reservation;
import services.ReservationsService;
import utils.LoggerUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер для управления заявками администратором
 */
public class AdminOrdersController {

    // ========== FXML элементы ==========
    @FXML private Label totalOrdersLabel;
    @FXML private Label statusLabel;

    // Кнопки переключения
    @FXML private Button btnReservations;
    @FXML private Button btnPurchases;

    // Фильтры
    @FXML private ToggleButton filterAll;
    @FXML private ToggleButton filterPending;
    @FXML private ToggleButton filterConfirmed;
    @FXML private ToggleButton filterCompleted;
    @FXML private ToggleButton filterCancelled;

    // Вьюхи
    @FXML private VBox reservationsView;
    @FXML private VBox purchasesView;

    // Таблица бронирований
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> resColId;
    @FXML private TableColumn<Reservation, String> resColCar;
    @FXML private TableColumn<Reservation, String> resColCustomer;
    @FXML private TableColumn<Reservation, String> resColPhone;
    @FXML private TableColumn<Reservation, String> resColEmail;
    @FXML private TableColumn<Reservation, String> resColDate;
    @FXML private TableColumn<Reservation, String> resColStatus;
    @FXML private TableColumn<Reservation, String> resColCreated;
    @FXML private TableColumn<Reservation, Void> resColActions;

    // Таблица покупок
    @FXML private TableView<Purchase> purchasesTable;
    @FXML private TableColumn<Purchase, String> purColId;
    @FXML private TableColumn<Purchase, String> purColCar;
    @FXML private TableColumn<Purchase, String> purColCustomer;
    @FXML private TableColumn<Purchase, String> purColPhone;
    @FXML private TableColumn<Purchase, String> purColEmail;
    @FXML private TableColumn<Purchase, String> purColPrice;
    @FXML private TableColumn<Purchase, String> purColPayment;
    @FXML private TableColumn<Purchase, String> purColStatus;
    @FXML private TableColumn<Purchase, String> purColDate;
    @FXML private TableColumn<Purchase, Void> purColActions;

    // Данные
    private ObservableList<Reservation> allReservations = FXCollections.observableArrayList();
    private ObservableList<Purchase> allPurchases = FXCollections.observableArrayList();

    private String currentFilter = "all";
    private boolean showingReservations = true;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // ========== ИНИЦИАЛИЗАЦИЯ ==========

    @FXML
    public void initialize() {
        setupReservationsTable();
        setupPurchasesTable();
        loadData();

        // Группа фильтров
        ToggleGroup filterGroup = new ToggleGroup();
        filterAll.setToggleGroup(filterGroup);
        filterPending.setToggleGroup(filterGroup);
        filterConfirmed.setToggleGroup(filterGroup);
        filterCompleted.setToggleGroup(filterGroup);
        filterCancelled.setToggleGroup(filterGroup);

        LoggerUtil.action("Открыта панель управления заявками");
    }

    // ========== НАСТРОЙКА ТАБЛИЦ ==========

    private void setupReservationsTable() {
        resColId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        resColCar.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCarName()));
        resColCustomer.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
        resColPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        resColEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        resColDate.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getReservationDate().format(dateFormatter)
        ));
        resColStatus.setCellValueFactory(data -> new SimpleStringProperty(
            translateStatus(data.getValue().getStatus())
        ));
        resColCreated.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getCreatedAt().format(dateFormatter)
        ));

        // Колонка с действиями
        resColActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("👁 Просмотр");
            private final Button btnConfirm = new Button("✅");
            private final Button btnCancel = new Button("❌");
            private final HBox box = new HBox(5, btnView, btnConfirm, btnCancel);

            {
                box.setAlignment(Pos.CENTER);
                btnView.setStyle("-fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnConfirm.setStyle("-fx-cursor: hand; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnCancel.setStyle("-fx-cursor: hand; -fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");

                btnView.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    viewReservationDetails(reservation);
                });

                btnConfirm.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    updateReservationStatus(reservation, "confirmed");
                });

                btnCancel.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    updateReservationStatus(reservation, "cancelled");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    // Скрываем кнопки если уже завершено/отменено
                    boolean isActive = reservation.getStatus().equals("pending") || reservation.getStatus().equals("confirmed");
                    btnConfirm.setVisible(isActive);
                    btnCancel.setVisible(isActive);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupPurchasesTable() {
        purColId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        purColCar.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCarName()));
        purColCustomer.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
        purColPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        purColEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        purColPrice.setCellValueFactory(data -> new SimpleStringProperty(
            String.format("%.0f ₸", data.getValue().getPrice())
        ));
        purColPayment.setCellValueFactory(data -> new SimpleStringProperty(
            translatePaymentMethod(data.getValue().getPaymentMethod())
        ));
        purColStatus.setCellValueFactory(data -> new SimpleStringProperty(
            translateStatus(data.getValue().getStatus())
        ));
        purColDate.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getPurchaseDate().format(dateFormatter)
        ));

        // Колонка с действиями
        purColActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("👁 Просмотр");
            private final Button btnComplete = new Button("✅");
            private final Button btnCancel = new Button("❌");
            private final HBox box = new HBox(5, btnView, btnComplete, btnCancel);

            {
                box.setAlignment(Pos.CENTER);
                btnView.setStyle("-fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnComplete.setStyle("-fx-cursor: hand; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnCancel.setStyle("-fx-cursor: hand; -fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");

                btnView.setOnAction(e -> {
                    Purchase purchase = getTableView().getItems().get(getIndex());
                    viewPurchaseDetails(purchase);
                });

                btnComplete.setOnAction(e -> {
                    Purchase purchase = getTableView().getItems().get(getIndex());
                    updatePurchaseStatus(purchase, "completed");
                });

                btnCancel.setOnAction(e -> {
                    Purchase purchase = getTableView().getItems().get(getIndex());
                    updatePurchaseStatus(purchase, "cancelled");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Purchase purchase = getTableView().getItems().get(getIndex());
                    boolean isActive = purchase.getStatus().equals("pending") || purchase.getStatus().equals("confirmed");
                    btnComplete.setVisible(isActive);
                    btnCancel.setVisible(isActive);
                    setGraphic(box);
                }
            }
        });
    }

    // ========== ЗАГРУЗКА ДАННЫХ ==========

    private void loadData() {
        allReservations.clear();
        allPurchases.clear();

        List<Reservation> reservations = ReservationsService.getAllReservations();
        List<Purchase> purchases = ReservationsService.getAllPurchases();

        allReservations.addAll(reservations);
        allPurchases.addAll(purchases);

        applyFilter();
        updateTotalLabel();

        LoggerUtil.info("Загружено бронирований: " + reservations.size() + ", покупок: " + purchases.size());
    }

    @FXML
    private void refreshData() {
        loadData();
        statusLabel.setText("✅ Данные обновлены");
    }

    // ========== ПЕРЕКЛЮЧЕНИЕ ВИДОВ ==========

    @FXML
    private void showReservations() {
        showingReservations = true;
        reservationsView.setVisible(true);
        reservationsView.setManaged(true);
        purchasesView.setVisible(false);
        purchasesView.setManaged(false);

        btnReservations.getStyleClass().clear();
        btnReservations.getStyleClass().add("btn-primary");
        btnPurchases.getStyleClass().clear();
        btnPurchases.getStyleClass().add("btn-light");

        applyFilter();
        updateTotalLabel();
    }

    @FXML
    private void showPurchases() {
        showingReservations = false;
        reservationsView.setVisible(false);
        reservationsView.setManaged(false);
        purchasesView.setVisible(true);
        purchasesView.setManaged(true);

        btnPurchases.getStyleClass().clear();
        btnPurchases.getStyleClass().add("btn-primary");
        btnReservations.getStyleClass().clear();
        btnReservations.getStyleClass().add("btn-light");

        applyFilter();
        updateTotalLabel();
    }

    // ========== ФИЛЬТРАЦИЯ ==========

    @FXML
    private void filterByStatus() {
        if (filterAll.isSelected()) {
            currentFilter = "all";
        } else if (filterPending.isSelected()) {
            currentFilter = "pending";
        } else if (filterConfirmed.isSelected()) {
            currentFilter = "confirmed";
        } else if (filterCompleted.isSelected()) {
            currentFilter = "completed";
        } else if (filterCancelled.isSelected()) {
            currentFilter = "cancelled";
        }

        applyFilter();
    }

    private void applyFilter() {
        if (showingReservations) {
            if (currentFilter.equals("all")) {
                reservationsTable.setItems(allReservations);
            } else {
                ObservableList<Reservation> filtered = allReservations.filtered(
                    r -> r.getStatus().equals(currentFilter)
                );
                reservationsTable.setItems(filtered);
            }
        } else {
            if (currentFilter.equals("all")) {
                purchasesTable.setItems(allPurchases);
            } else {
                ObservableList<Purchase> filtered = allPurchases.filtered(
                    p -> p.getStatus().equals(currentFilter)
                );
                purchasesTable.setItems(filtered);
            }
        }

        updateTotalLabel();
    }

    private void updateTotalLabel() {
        int count = showingReservations ? reservationsTable.getItems().size() : purchasesTable.getItems().size();
        totalOrdersLabel.setText("Всего: " + count);
    }

    // ========== ДЕЙСТВИЯ С ЗАЯВКАМИ ==========

    private void viewReservationDetails(Reservation reservation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Детали бронирования #" + reservation.getId());
        alert.setHeaderText("Информация о бронировании");

        String details = String.format(
            "🚗 Автомобиль: %s\n" +
            "👤 Клиент: %s\n" +
            "📞 Телефон: %s\n" +
            "📧 Email: %s\n" +
            "📅 Дата брони: %s\n" +
            "📊 Статус: %s\n" +
            "👥 Пользователь: %s\n" +
            "📝 Примечания: %s\n" +
            "🕐 Создано: %s\n" +
            "🕑 Обновлено: %s",
            reservation.getCarName(),
            reservation.getCustomerName(),
            reservation.getPhone(),
            reservation.getEmail(),
            reservation.getReservationDate().format(dateFormatter),
            translateStatus(reservation.getStatus()),
            reservation.getUserName(),
            reservation.getNotes() != null ? reservation.getNotes() : "—",
            reservation.getCreatedAt().format(dateFormatter),
            reservation.getUpdatedAt().format(dateFormatter)
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void viewPurchaseDetails(Purchase purchase) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Детали покупки #" + purchase.getId());
        alert.setHeaderText("Информация о покупке");

        String details = String.format(
            "🚗 Автомобиль: %s\n" +
            "👤 Клиент: %s\n" +
            "📞 Телефон: %s\n" +
            "📧 Email: %s\n" +
            "💰 Цена: %.0f ₸\n" +
            "💳 Способ оплаты: %s\n" +
            "📊 Статус: %s\n" +
            "👥 Пользователь: %s\n" +
            "📝 Примечания: %s\n" +
            "🕐 Дата покупки: %s\n" +
            "✅ Завершено: %s",
            purchase.getCarName(),
            purchase.getCustomerName(),
            purchase.getPhone(),
            purchase.getEmail(),
            purchase.getPrice(),
            translatePaymentMethod(purchase.getPaymentMethod()),
            translateStatus(purchase.getStatus()),
            purchase.getUserName(),
            purchase.getNotes() != null ? purchase.getNotes() : "—",
            purchase.getPurchaseDate().format(dateFormatter),
            purchase.getCompletedAt() != null ? purchase.getCompletedAt().format(dateFormatter) : "—"
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void updateReservationStatus(Reservation reservation, String newStatus) {
        String statusText = translateStatus(newStatus);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Изменить статус бронирования?");
        confirm.setContentText("Установить статус \"" + statusText + "\" для бронирования #" + reservation.getId() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = ReservationsService.updateReservationStatus(reservation.getId(), newStatus);

            if (success) {
                statusLabel.setText("✅ Статус изменён на: " + statusText);
                refreshData();
            } else {
                statusLabel.setText("❌ Ошибка изменения статуса");
            }
        }
    }

    private void updatePurchaseStatus(Purchase purchase, String newStatus) {
        String statusText = translateStatus(newStatus);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Изменить статус покупки?");
        confirm.setContentText("Установить статус \"" + statusText + "\" для покупки #" + purchase.getId() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = ReservationsService.updatePurchaseStatus(purchase.getId(), newStatus);

            if (success) {
                statusLabel.setText("✅ Статус изменён на: " + statusText);
                refreshData();
            } else {
                statusLabel.setText("❌ Ошибка изменения статуса");
            }
        }
    }

    // ========== УТИЛИТЫ ==========

    private String translateStatus(String status) {
        return switch (status) {
            case "pending" -> "⏳ В обработке";
            case "confirmed" -> "✅ Подтверждено";
            case "completed" -> "🎉 Завершено";
            case "cancelled" -> "❌ Отменено";
            default -> status;
        };
    }

    private String translatePaymentMethod(String method) {
        return switch (method) {
            case "cash" -> "💵 Наличные";
            case "card" -> "💳 Карта";
            case "transfer" -> "🏦 Перевод";
            case "installment" -> "📅 Рассрочка";
            default -> method;
        };
    }

    // ========== НАВИГАЦИЯ ==========

    @FXML
    private void goBack() {
        try {
            Stage currentStage = (Stage) reservationsTable.getScene().getWindow();
            Stage newStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("/resources/carhub-admin-view.fxml"));
            newStage.setScene(new Scene(root));
            newStage.setTitle("CarHub — Панель администратора");
            newStage.show();
            LoggerUtil.action("Закрыта панель управления заявками, возврат в админ-панель");
            currentStage.close();
        } catch (Exception e) {
            LoggerUtil.error("Ошибка возвращения в панель администратора", e);
        }
    }
}
