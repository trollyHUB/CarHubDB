package views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import models.Car;
import services.FavoritesService;
import services.CommentsService;
import utils.SessionManager;
import utils.ImageCache;

public class CarCardView extends VBox {
    private final Car car;
    private ImageView imageView;
    private Button favoriteButton;
    private Runnable onFavoriteChanged;

    public CarCardView(Car car) {
        this.car = car;
        initializeUI();
        updateFavoriteButton();
    }

    public void setOnFavoriteChanged(Runnable callback) {
        this.onFavoriteChanged = callback;
    }

    private void initializeUI() {
        // Стиль карточки с новым дизайном
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(15));
        setSpacing(10);
        setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.15), 12, 0, 0, 4); " +
                "-fx-cursor: hand;");
        setPrefWidth(300);
        setMaxWidth(300);

        // Изображение с кнопкой избранного
        imageView = new ImageView();
        imageView.setFitWidth(270);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle("-fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        loadImage();

        // Кнопка избранного (только для пользователей)
        StackPane imageContainer = new StackPane(imageView);
        if (SessionManager.isUser()) {
            favoriteButton = new Button("❤");
            favoriteButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.95); " +
                "-fx-background-radius: 25; " +
                "-fx-font-size: 20px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 8 12 8 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);"
            );
            StackPane.setAlignment(favoriteButton, Pos.TOP_RIGHT);
            StackPane.setMargin(favoriteButton, new Insets(12));

            favoriteButton.setOnAction(e -> {
                e.consume();
                toggleFavorite();
            });

            imageContainer.getChildren().add(favoriteButton);
        }

        // Название
        Label nameLabel = new Label(car.getName());
        nameLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(270);

        // Модель и бренд
        String brandInfo = (car.getBrand() != null ? car.getBrand() + " • " : "") + car.getModel();
        Label modelLabel = new Label(brandInfo);
        modelLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        modelLabel.setMaxWidth(270);

        // Информация (год, пробег)
        StringBuilder info = new StringBuilder();
        if (car.getYear() != null) info.append("📅 ").append(car.getYear());
        if (car.getMileage() != null) {
            if (!info.isEmpty()) info.append("  •  ");
            info.append("🛣️ ").append(String.format("%,d", car.getMileage())).append(" км");
        }
        Label infoLabel = new Label(info.toString());
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");
        infoLabel.setMaxWidth(270);

        // ⭐ РЕЙТИНГ И КОММЕНТАРИИ
        HBox ratingBox = new HBox(12);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        ratingBox.setStyle("-fx-padding: 5 0 0 0;");

        // Рейтинг
        double avgRating = CommentsService.getAverageRating(car.getId());
        int ratingsCount = CommentsService.getRatingsCount(car.getId());
        if (ratingsCount > 0) {
            Label ratingLabel = new Label(String.format("⭐ %.1f", avgRating));
            ratingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FF9800;");
            ratingBox.getChildren().add(ratingLabel);
        }

        // Количество комментариев
        int commentsCount = CommentsService.getCommentsCount(car.getId());
        if (commentsCount > 0) {
            Label commentsLabel = new Label(String.format("💬 %d", commentsCount));
            commentsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            ratingBox.getChildren().add(commentsLabel);
        }

        // Разделитель
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Цена с градиентом
        Label priceLabel = new Label(utils.PriceFormatter.format(car.getPrice()));
        priceLabel.setStyle(
            "-fx-font-size: 22px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: linear-gradient(to right, #667eea, #764ba2);" +
            "-fx-padding: 5 0 0 0;"
        );

        getChildren().addAll(imageContainer, nameLabel, modelLabel, infoLabel, ratingBox, spacer, priceLabel);

        // Улучшенный эффект при наведении
        setOnMouseEntered(e -> {
            setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 15; " +
                    "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.35), 20, 0, 0, 8); " +
                    "-fx-cursor: hand;");
            setScaleX(1.03);
            setScaleY(1.03);
        });

        setOnMouseExited(e -> {
            setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 15; " +
                    "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.15), 12, 0, 0, 4); " +
                    "-fx-cursor: hand;");
            setScaleX(1.0);
            setScaleY(1.0);
        });
    }

    private void toggleFavorite() {
        if (!SessionManager.isLoggedIn()) return;

        int userId = SessionManager.getCurrentUserId();
        boolean isFav = FavoritesService.isFavorite(userId, car.getId());

        if (isFav) {
            FavoritesService.removeFromFavorites(userId, car.getId());
        } else {
            FavoritesService.addToFavorites(userId, car.getId());
        }

        updateFavoriteButton();

        if (onFavoriteChanged != null) {
            onFavoriteChanged.run();
        }
    }

    private void updateFavoriteButton() {
        if (favoriteButton == null || !SessionManager.isLoggedIn()) return;

        int userId = SessionManager.getCurrentUserId();
        boolean isFav = FavoritesService.isFavorite(userId, car.getId());

        if (isFav) {
            favoriteButton.setStyle(
                "-fx-background-color: rgba(244,67,54,0.95); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 20; " +
                "-fx-font-size: 18px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 5 10 5 10;"
            );
        } else {
            favoriteButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.9); " +
                "-fx-background-radius: 20; " +
                "-fx-font-size: 18px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 5 10 5 10;"
            );
        }
    }

    private void loadImage() {
        // ✅ ИСПОЛЬЗУЕМ КЭШ для оптимизации загрузки изображений
        String url = car.getImageUrl();
        Image img = ImageCache.getImage(url);
        imageView.setImage(img);
    }

    public Car getCar() {
        return car;
    }
}

