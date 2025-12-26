package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Car;
import services.FavoritesService;
import utils.SessionManager;
import views.CarCardView;

import java.util.List;

public class FavoritesController {

    @FXML
    private FlowPane favoritesContainer;
    @FXML
    private Label countLabel;
    @FXML
    private VBox emptyLabel;

    public void initialize() {
        loadFavorites();
    }

    private void loadFavorites() {
        favoritesContainer.getChildren().clear();

        if (!SessionManager.isLoggedIn()) {
            showEmpty();
            return;
        }

        int userId = SessionManager.getCurrentUserId();
        List<Car> favorites = FavoritesService.getFavoritesCars(userId);

        if (favorites.isEmpty()) {
            showEmpty();
        } else {
            emptyLabel.setVisible(false);
            emptyLabel.setManaged(false);

            for (Car car : favorites) {
                CarCardView card = new CarCardView(car);

                // Callback для обновления при удалении из избранного
                card.setOnFavoriteChanged(this::loadFavorites);

                // Двойной клик - открыть детали
                card.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        openDetails(car);
                    }
                });

                favoritesContainer.getChildren().add(card);
            }
        }

        updateCount(favorites.size());
    }

    private void showEmpty() {
        emptyLabel.setVisible(true);
        emptyLabel.setManaged(true);
        updateCount(0);
    }

    private void updateCount(int count) {
        if (countLabel != null) {
            countLabel.setText(String.format("Всего избранных: %d", count));
        }
    }

    private void openDetails(Car car) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/car_details.fxml"));
            Parent root = loader.load();
            CarDetailsController controller = loader.getController();
            controller.setCar(car);

            Stage stage = new Stage();
            stage.setTitle("Информация об автомобиле");

            Scene scene = new Scene(root, 900, 700);
            stage.setScene(scene);

            // ✅ ЦЕНТРАЛИЗАЦИЯ ОКНА
            stage.centerOnScreen();

            stage.setMinWidth(800);
            stage.setMinHeight(600);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void clearAllFavorites() {
        if (!SessionManager.isLoggedIn()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Вы уверены, что хотите удалить все избранные автомобили?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Подтверждение");

        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            int userId = SessionManager.getCurrentUserId();
            List<Car> favorites = FavoritesService.getFavoritesCars(userId);

            for (Car car : favorites) {
                FavoritesService.removeFromFavorites(userId, car.getId());
            }

            loadFavorites();
        }
    }

    @FXML
    protected void backToCatalog() {
        try {
            Stage stage = (Stage) favoritesContainer.getScene().getWindow();
            String fxmlFile = SessionManager.isAdmin() ?
                    "/resources/carhub-admin-cards-filtered.fxml" :
                    "/resources/carhub-user-cards-filtered.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Каталог");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void logout() {
        try {
            SessionManager.logout();
            Parent root = FXMLLoader.load(getClass().getResource("/resources/login-view.fxml"));
            Stage stage = (Stage) favoritesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Вход");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

