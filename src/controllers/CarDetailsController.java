package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import models.Car;
import models.CarImage;
import models.Comment;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.CommentsService;
import services.CarImagesService;
import utils.ImageCache;
import utils.SessionManager;
import utils.LoggerUtil;
import utils.NotificationUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class CarDetailsController {

    @FXML private Label nameLabel;
    @FXML private Label modelLabel;
    @FXML private Label priceLabel;
    @FXML private Label brandLabel;
    @FXML private Label yearLabel;
    @FXML private Label mileageLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView imageView;

    // Поля для галереи фото
    @FXML private Label photoCountLabel;
    @FXML private Label photoIndexLabel;
    @FXML private Button prevPhotoBtn;
    @FXML private Button nextPhotoBtn;
    @FXML private HBox adminPhotoButtons;
    @FXML private ScrollPane thumbnailsPane;
    @FXML private HBox thumbnailsContainer;

    @FXML private Label averageRatingLabel;
    @FXML private Label ratingsCountLabel;
    @FXML private HBox starsBox;
    @FXML private VBox ratingInputBox;
    @FXML private Button star1Btn, star2Btn, star3Btn, star4Btn, star5Btn;

    @FXML private Label commentsCountLabel;
    @FXML private VBox commentInputBox;
    @FXML private TextArea commentTextArea;
    @FXML private VBox commentsListBox;

    private Car car;
    private Button[] starButtons;
    private List<CarImage> carImages = new ArrayList<>();
    private int currentPhotoIndex = 0;

    public void setCar(Car car) {
        this.car = car;
        if (car == null) return;

        nameLabel.setText(nvl(car.getName()));
        modelLabel.setText(nvl(car.getModel()));
        priceLabel.setText(utils.PriceFormatter.formatWithPrefix(car.getPrice()));
        if (brandLabel != null) brandLabel.setText(nvl(car.getBrand()));
        if (yearLabel != null) yearLabel.setText(car.getYear() == null ? "—" : String.valueOf(car.getYear()));
        if (mileageLabel != null) mileageLabel.setText(car.getMileage() == null ? "—" : String.format("%,d км", car.getMileage()));
        if (descriptionLabel != null) descriptionLabel.setText(nvl(car.getDescription()));

        // Загрузка галереи фото
        loadPhotoGallery();

        starButtons = new Button[]{star1Btn, star2Btn, star3Btn, star4Btn, star5Btn};

        loadRating();
        loadComments();

        if (!SessionManager.isLoggedIn()) {
            ratingInputBox.setVisible(false);
            ratingInputBox.setManaged(false);
            commentInputBox.setVisible(false);
            commentInputBox.setManaged(false);
        }

        // Показать кнопки админа, если это админ
        if (adminPhotoButtons != null) {
            adminPhotoButtons.setVisible(SessionManager.isAdmin());
            adminPhotoButtons.setManaged(SessionManager.isAdmin());
        }
    }

    private void loadRating() {
        double avgRating = CommentsService.getAverageRating(car.getId());
        int ratingsCount = CommentsService.getRatingsCount(car.getId());

        averageRatingLabel.setText(String.format("%.1f", avgRating));
        ratingsCountLabel.setText(String.format("(%d %s)", ratingsCount,
            ratingsCount == 1 ? "оценка" : ratingsCount < 5 ? "оценки" : "оценок"));

        starsBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= Math.round(avgRating) ? "⭐" : "☆");
            star.setStyle("-fx-font-size: 24px;");
            starsBox.getChildren().add(star);
        }

        if (SessionManager.isLoggedIn()) {
            int userRating = CommentsService.getUserRating(car.getId(), SessionManager.getCurrentUserId());
            updateStarButtons(userRating);
        }
    }

    private void updateStarButtons(int rating) {
        for (int i = 0; i < starButtons.length; i++) {
            if (i < rating) {
                starButtons[i].setText("⭐");
                starButtons[i].setStyle("-fx-font-size: 24px; -fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #FF9800;");
            } else {
                starButtons[i].setText("☆");
                starButtons[i].setStyle("-fx-font-size: 24px; -fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #999;");
            }
        }
    }

    @FXML
    private void rateCar(javafx.event.ActionEvent event) {
        if (!SessionManager.isLoggedIn()) {
            showAlert("Авторизация", "Войдите для оценки автомобилей");
            return;
        }

        Button clickedButton = (Button) event.getSource();
        int rating = 0;

        for (int i = 0; i < starButtons.length; i++) {
            if (starButtons[i] == clickedButton) {
                rating = i + 1;
                break;
            }
        }

        if (CommentsService.setRating(car.getId(), SessionManager.getCurrentUserId(), rating)) {
            LoggerUtil.action("Оценка " + rating + " для авто '" + car.getName() + "'");
            loadRating();
            showAlert("Успешно", "✅ Спасибо за вашу оценку!");
        } else {
            showAlert("Ошибка", "Не удалось сохранить оценку");
        }
    }

    private void loadComments() {
        List<Comment> comments = CommentsService.getCommentsByCar(car.getId());
        commentsCountLabel.setText(String.format("(%d)", comments.size()));

        commentsListBox.getChildren().clear();

        if (comments.isEmpty()) {
            Label noComments = new Label("Пока нет комментариев. Будьте первым!");
            noComments.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            commentsListBox.getChildren().add(noComments);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (Comment comment : comments) {
            VBox commentBox = new VBox(8);
            commentBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 15; -fx-background-radius: 8;");

            HBox header = new HBox(10);
            header.setStyle("-fx-alignment: center-left;");

            Label author = new Label("👤 " + comment.getUserName());
            author.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

            Label date = new Label("• " + comment.getCreatedAt().format(formatter));
            date.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

            header.getChildren().addAll(author, date);

            if (SessionManager.isLoggedIn() &&
                (SessionManager.getCurrentUserId() == comment.getUserId() || SessionManager.isAdmin())) {
                Button deleteBtn = new Button("🗑️");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> deleteComment(comment.getId()));
                HBox.setMargin(deleteBtn, new Insets(0, 0, 0, 10));
                header.getChildren().add(deleteBtn);
            }

            Label text = new Label(comment.getCommentText());
            text.setWrapText(true);
            text.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

            commentBox.getChildren().addAll(header, new Separator(), text);
            commentsListBox.getChildren().add(commentBox);
        }
    }

    @FXML
    private void addComment() {
        if (!SessionManager.isLoggedIn()) {
            showAlert("Авторизация", "Войдите для добавления комментариев");
            return;
        }

        String commentText = commentTextArea.getText().trim();

        if (commentText.isEmpty()) {
            showAlert("Ошибка", "Напишите текст комментария");
            return;
        }

        if (commentText.length() > 1000) {
            showAlert("Ошибка", "Комментарий слишком длинный (максимум 1000 символов)");
            return;
        }

        if (CommentsService.addComment(car.getId(), SessionManager.getCurrentUserId(), commentText)) {
            LoggerUtil.action("Комментарий к авто '" + car.getName() + "' от " + SessionManager.getCurrentUsername());
            commentTextArea.clear();
            loadComments();
            showAlert("Успешно", "✅ Комментарий добавлен!");
        } else {
            showAlert("Ошибка", "Не удалось добавить комментарий");
        }
    }

    @FXML
    private void clearComment() {
        commentTextArea.clear();
    }

    private void deleteComment(int commentId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удалить комментарий?");
        confirm.setContentText("Это действие нельзя отменить.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (CommentsService.deleteComment(commentId, SessionManager.getCurrentUserId(), SessionManager.isAdmin())) {
                LoggerUtil.action("Удалён комментарий ID=" + commentId);
                loadComments();
                NotificationUtil.showInfo("Комментарий удалён");
            } else {
                showAlert("Ошибка", "Не удалось удалить комментарий");
                NotificationUtil.showError("Не удалось удалить комментарий");
            }
        }
    }

    // ========== БРОНИРОВАНИЕ С ОБЯЗАТЕЛЬНЫМ EMAIL ==========

    @FXML
    private void openReservationForm() {
        if (!SessionManager.isLoggedIn()) {
            showAlert("Авторизация", "Войдите в систему для бронирования автомобилей");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Забронировать автомобиль");
        dialog.setHeaderText("🚗 Бронирование: " + car.getName() + " " + car.getModel() +
                           "\n💰 Цена: " + utils.PriceFormatter.formatWithPrefix(car.getPrice()));

        ButtonType reserveButtonType = new ButtonType("📅 Забронировать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reserveButtonType, ButtonType.CANCEL);

        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(600);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Ваше имя");
        nameField.setText(SessionManager.getCurrentUsername());
        nameField.setPrefWidth(350);

        TextField phoneField = new TextField();
        phoneField.setPromptText("+7 999 123-45-67");
        phoneField.setPrefWidth(350);

        TextField emailField = new TextField();
        emailField.setPromptText("email@example.com (обязательно)");
        emailField.setPrefWidth(350);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Выберите дату");
        datePicker.setPrefWidth(350);

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Дополнительные пожелания...");
        notesArea.setPrefRowCount(3);
        notesArea.setPrefWidth(350);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);
        errorLabel.setPrefWidth(350);
        errorLabel.setPrefHeight(120);
        errorLabel.setMinHeight(80);
        errorLabel.setMaxHeight(200);

        grid.add(new Label("Ваше имя:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Телефон:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Email *:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Дата брони:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Примечание:"), 0, 4);
        grid.add(notesArea, 1, 4);
        grid.add(errorLabel, 0, 5, 2, 1);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dialog.getDialogPane().setContent(scrollPane);

        Button reserveButton = (Button) dialog.getDialogPane().lookupButton(reserveButtonType);
        reserveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String notes = notesArea.getText().trim();

            StringBuilder errors = new StringBuilder();
            boolean hasError = false;

            // Сброс стилей
            nameField.setStyle("");
            phoneField.setStyle("");
            emailField.setStyle("");
            datePicker.setStyle("");

            // Валидация имени
            if (name.isEmpty()) {
                errors.append("❌ Укажите ваше имя\n");
                nameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            } else if (name.length() < 2) {
                errors.append("❌ Имя должно содержать минимум 2 символа\n");
                nameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            }

            // Валидация телефона
            if (phone.isEmpty()) {
                errors.append("❌ Укажите номер телефона\n");
                phoneField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            } else {
                String phoneDigits = phone.replaceAll("[^0-9]", "");
                if (phoneDigits.length() < 10) {
                    errors.append("❌ Телефон должен содержать минимум 10 цифр\n");
                    phoneField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    hasError = true;
                }
            }

            // Валидация email (ОБЯЗАТЕЛЬНОЕ ПОЛЕ!)
            if (email.isEmpty()) {
                errors.append("❌ Укажите email\n");
                emailField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            } else if (!isValidEmail(email)) {
                errors.append("❌ Неверный формат email (пример: user@mail.com)\n");
                emailField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            }

            // Валидация даты
            if (datePicker.getValue() == null) {
                errors.append("❌ Выберите дату бронирования\n");
                datePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            } else if (datePicker.getValue().isBefore(java.time.LocalDate.now())) {
                errors.append("❌ Дата не может быть в прошлом\n");
                datePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            }

            if (hasError) {
                errorLabel.setText(errors.toString());
                event.consume();
            } else {
                // Сохранение в файл
                saveReservationToFile(name, phone, email, datePicker.getValue().toString(), notes);

                // Сохранение в базу данных
                int userId = utils.SessionManager.getCurrentUserId();
                java.time.LocalDateTime reservationDateTime = datePicker.getValue().atStartOfDay();

                boolean saved = services.ReservationsService.createReservation(
                    car.getId(),
                    userId,
                    name,
                    phone,
                    email,
                    reservationDateTime,
                    notes
                );

                if (saved) {
                    LoggerUtil.action("Бронирование сохранено в БД: " + name + " для авто '" + car.getName() + "'");
                } else {
                    LoggerUtil.error("Ошибка сохранения бронирования в БД", null);
                }

                LoggerUtil.action("Бронирование: " + name + " для авто '" + car.getName() + "'");

                NotificationUtil.showWithTitle(
                    "Бронирование оформлено!",
                    "Мы свяжемся с вами в ближайшее время",
                    NotificationUtil.NotificationType.SUCCESS
                );

                showAlert("Успешно", "✅ Заявка на бронирование отправлена!\n\n" +
                        "📞 С вами свяжется менеджер в ближайшее время.\n\n" +
                        "📅 Дата бронирования: " + datePicker.getValue().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            }
        });

        dialog.showAndWait();
    }

    // ========== ПОКУПКА С ОБЯЗАТЕЛЬНЫМ EMAIL ==========

    @FXML
    private void openPurchaseForm() {
        if (!SessionManager.isLoggedIn()) {
            showAlert("Авторизация", "Войдите в систему для покупки автомобилей");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Купить автомобиль");
        dialog.setHeaderText("💰 Покупка: " + car.getName() + " " + car.getModel() +
                           "\n💵 Цена: " + utils.PriceFormatter.formatWithPrefix(car.getPrice()));

        ButtonType buyButtonType = new ButtonType("💰 Купить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buyButtonType, ButtonType.CANCEL);

        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(600);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Ваше имя");
        nameField.setText(SessionManager.getCurrentUsername());
        nameField.setPrefWidth(350);

        TextField phoneField = new TextField();
        phoneField.setPromptText("+7 999 123-45-67");
        phoneField.setPrefWidth(350);

        TextField emailField = new TextField();
        emailField.setPromptText("email@example.com (обязательно)");
        emailField.setPrefWidth(350);

        ComboBox<String> paymentMethod = new ComboBox<>();
        paymentMethod.getItems().addAll("💵 Наличные", "💳 Карта", "🏦 Банковский перевод", "📊 Кредит");
        paymentMethod.setPromptText("Выберите способ оплаты");
        paymentMethod.setPrefWidth(350);

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Дополнительные пожелания...");
        notesArea.setPrefRowCount(3);
        notesArea.setPrefWidth(350);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);
        errorLabel.setPrefWidth(350);
        errorLabel.setPrefHeight(120);
        errorLabel.setMinHeight(80);
        errorLabel.setMaxHeight(200);

        grid.add(new Label("Ваше имя:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Телефон:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Email *:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Способ оплаты:"), 0, 3);
        grid.add(paymentMethod, 1, 3);
        grid.add(new Label("Примечание:"), 0, 4);
        grid.add(notesArea, 1, 4);
        grid.add(errorLabel, 0, 5, 2, 1);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dialog.getDialogPane().setContent(scrollPane);

        Button buyButton = (Button) dialog.getDialogPane().lookupButton(buyButtonType);
        buyButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String payment = paymentMethod.getValue();
            String notes = notesArea.getText().trim();

            StringBuilder errors = new StringBuilder();
            boolean hasError = false;

            // Сброс стилей
            nameField.setStyle("");
            phoneField.setStyle("");
            emailField.setStyle("");
            paymentMethod.setStyle("");

            // Валидация имени
            if (name.isEmpty()) {
                errors.append("❌ Укажите ваше имя\n");
                nameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            } else if (name.length() < 2) {
                errors.append("❌ Имя должно содержать минимум 2 символа\n");
                nameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            }

            // Валидация телефона
            if (phone.isEmpty()) {
                errors.append("❌ Укажите номер телефона\n");
                phoneField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            } else {
                String phoneDigits = phone.replaceAll("[^0-9]", "");
                if (phoneDigits.length() < 10) {
                    errors.append("❌ Телефон должен содержать минимум 10 цифр\n");
                    phoneField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    hasError = true;
                }
            }

            // Валидация email (ОБЯЗАТЕЛЬНОЕ ПОЛЕ!)
            if (email.isEmpty()) {
                errors.append("❌ Укажите email\n");
                emailField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            } else if (!isValidEmail(email)) {
                errors.append("❌ Неверный формат email (пример: user@mail.com)\n");
                emailField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            }

            // Валидация способа оплаты
            if (payment == null || payment.isEmpty()) {
                errors.append("❌ Выберите способ оплаты\n");
                paymentMethod.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                hasError = true;
            }

            if (hasError) {
                errorLabel.setText(errors.toString());
                event.consume();
            } else {
                // Сохранение в файл
                savePurchaseToFile(name, phone, email, payment, notes);

                // Сохранение в базу данных
                int userId = utils.SessionManager.getCurrentUserId();
                boolean saved = services.ReservationsService.createPurchase(
                    car.getId(),
                    userId,
                    name,
                    phone,
                    email,
                    car.getPrice(),
                    payment,
                    notes
                );

                if (saved) {
                    LoggerUtil.action("Покупка сохранена в БД: " + name + " для авто '" + car.getName() + "' за " + car.getPrice());
                } else {
                    LoggerUtil.error("Ошибка сохранения покупки в БД", null);
                }

                LoggerUtil.action("Покупка: " + name + " для авто '" + car.getName() + "' за " + car.getPrice());

                NotificationUtil.showWithTitle(
                    "Заявка на покупку отправлена!",
                    "Наш менеджер свяжется с вами для уточнения деталей",
                    NotificationUtil.NotificationType.SUCCESS
                );

                showAlert("Успешно", "✅ Заявка на покупку отправлена!\n\n" +
                        "💰 Сумма: " + utils.PriceFormatter.formatWithPrefix(car.getPrice()) + "\n" +
                        "💳 Способ оплаты: " + payment + "\n\n" +
                        "📞 С вами свяжется менеджер для завершения сделки.");
            }
        });

        dialog.showAndWait();
    }

    // ========== МЕТОДЫ ДЛЯ ГАЛЕРЕИ ФОТО ==========

    /**
     * Загрузка галереи фото
     */
    private void loadPhotoGallery() {
        // Загружаем фото из БД
        carImages = CarImagesService.getCarImages(car.getId());

        // Если нет фото в БД, используем старое поле imageUrl
        if (carImages.isEmpty() && car.getImageUrl() != null && !car.getImageUrl().isEmpty()) {
            // Создаём фейковое фото из старого URL
            CarImage defaultImage = new CarImage(car.getId(), car.getImageUrl(), true);
            carImages.add(defaultImage);
        }

        // Обновляем UI
        updatePhotoGallery();
    }

    /**
     * Обновление галереи фото
     */
    private void updatePhotoGallery() {
        int totalPhotos = carImages.size();

        // Обновляем счётчик
        if (photoCountLabel != null) {
            photoCountLabel.setText("(" + totalPhotos + (totalPhotos == 1 ? " фото)" : " фото)"));
        }

        // Показываем текущее фото
        if (totalPhotos > 0 && currentPhotoIndex < totalPhotos) {
            CarImage currentImage = carImages.get(currentPhotoIndex);
            if (imageView != null) {
                Image img = ImageCache.getImage(currentImage.getImageUrl());
                imageView.setImage(img);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
            }

            // Обновляем индикатор
            if (photoIndexLabel != null) {
                photoIndexLabel.setText((currentPhotoIndex + 1) + " / " + totalPhotos);
            }
        } else if (imageView != null) {
            // Нет фото - показываем placeholder
            imageView.setImage(null);
        }

        // Показываем/скрываем кнопки навигации
        boolean showNav = totalPhotos > 1;
        if (prevPhotoBtn != null) prevPhotoBtn.setVisible(showNav);
        if (nextPhotoBtn != null) nextPhotoBtn.setVisible(showNav);

        // Обновляем миниатюры
        updateThumbnails();
    }

    /**
     * Обновление миниатюр
     */
    private void updateThumbnails() {
        if (thumbnailsContainer == null) return;

        thumbnailsContainer.getChildren().clear();

        if (carImages.size() <= 1) {
            if (thumbnailsPane != null) {
                thumbnailsPane.setVisible(false);
                thumbnailsPane.setManaged(false);
            }
            return;
        }

        if (thumbnailsPane != null) {
            thumbnailsPane.setVisible(true);
            thumbnailsPane.setManaged(true);
        }

        for (int i = 0; i < carImages.size(); i++) {
            final int index = i;
            CarImage carImage = carImages.get(i);

            ImageView thumbnail = new ImageView();
            thumbnail.setFitWidth(80);
            thumbnail.setFitHeight(60);
            thumbnail.setPreserveRatio(true);
            thumbnail.setSmooth(true);
            thumbnail.setStyle(
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2); " +
                (i == currentPhotoIndex ? "-fx-border-color: #667eea; -fx-border-width: 3;" : "")
            );

            Image img = ImageCache.getImage(carImage.getImageUrl());
            thumbnail.setImage(img);

            thumbnail.setOnMouseClicked(e -> {
                currentPhotoIndex = index;
                updatePhotoGallery();
            });

            thumbnailsContainer.getChildren().add(thumbnail);
        }
    }

    /**
     * Предыдущее фото
     */
    @FXML
    private void previousPhoto() {
        if (carImages.isEmpty()) return;

        currentPhotoIndex--;
        if (currentPhotoIndex < 0) {
            currentPhotoIndex = carImages.size() - 1;
        }

        updatePhotoGallery();
    }

    /**
     * Следующее фото
     */
    @FXML
    private void nextPhoto() {
        if (carImages.isEmpty()) return;

        currentPhotoIndex++;
        if (currentPhotoIndex >= carImages.size()) {
            currentPhotoIndex = 0;
        }

        updatePhotoGallery();
    }

    /**
     * Добавить фото (админ)
     */
    @FXML
    private void addPhoto() {
        if (!SessionManager.isAdmin()) {
            NotificationUtil.showWarning("Доступ запрещён!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите фото автомобиля");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Изображения", "*.jpg", "*.jpeg", "*.png", "*.gif"),
            new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(imageView.getScene().getWindow());

        if (files != null && !files.isEmpty()) {
            for (File file : files) {
                String imageUrl = file.toURI().toString();
                boolean isMain = carImages.isEmpty(); // Первое фото - главное

                if (CarImagesService.addImage(car.getId(), imageUrl, isMain)) {
                    LoggerUtil.action("Добавлено фото для автомобиля ID=" + car.getId());
                }
            }

            // Перезагружаем галерею
            loadPhotoGallery();
            NotificationUtil.showSuccess("Фото добавлены!");
        }
    }

    /**
     * Установить текущее фото как главное (админ)
     */
    @FXML
    private void setMainPhoto() {
        if (!SessionManager.isAdmin()) {
            NotificationUtil.showWarning("Доступ запрещён!");
            return;
        }

        if (carImages.isEmpty() || currentPhotoIndex >= carImages.size()) {
            NotificationUtil.showWarning("Выберите фото!");
            return;
        }

        CarImage currentImage = carImages.get(currentPhotoIndex);

        if (currentImage.isMain()) {
            NotificationUtil.showInfo("Это фото уже является главным!");
            return;
        }

        if (CarImagesService.setMainImage(currentImage.getId())) {
            NotificationUtil.showSuccess("Главное фото установлено!");
            loadPhotoGallery();
        } else {
            NotificationUtil.showError("Ошибка установки главного фото!");
        }
    }

    /**
     * Удалить текущее фото (админ)
     */
    @FXML
    private void deletePhoto() {
        if (!SessionManager.isAdmin()) {
            NotificationUtil.showWarning("Доступ запрещён!");
            return;
        }

        if (carImages.isEmpty() || currentPhotoIndex >= carImages.size()) {
            NotificationUtil.showWarning("Выберите фото для удаления!");
            return;
        }

        if (carImages.size() == 1) {
            NotificationUtil.showWarning("Нельзя удалить последнее фото!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление фото");
        confirm.setHeaderText("Вы уверены?");
        confirm.setContentText("Удалить выбранное фото?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            CarImage currentImage = carImages.get(currentPhotoIndex);

            if (CarImagesService.deleteImage(currentImage.getId())) {
                NotificationUtil.showSuccess("Фото удалено!");

                // Переключаемся на предыдущее фото
                if (currentPhotoIndex > 0) {
                    currentPhotoIndex--;
                }

                loadPhotoGallery();
            } else {
                NotificationUtil.showError("Ошибка удаления фото!");
            }
        }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private void saveReservationToFile(String name, String phone, String email, String date, String notes) {
        try (FileWriter fw = new FileWriter("reservations.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            pw.println("================================================================================");
            pw.println("БРОНИРОВАНИЕ #" + System.currentTimeMillis());
            pw.println("================================================================================");
            pw.println("Дата создания: " + timestamp);
            pw.println("Пользователь: " + SessionManager.getCurrentUsername());
            pw.println("--------------------------------------------------------------------------------");
            pw.println("АВТОМОБИЛЬ:");
            pw.println("  Название: " + car.getName());
            pw.println("  Модель: " + car.getModel());
            pw.println("  Цена: " + String.format("₸ %,.2f", car.getPrice()));
            if (car.getYear() != null) pw.println("  Год: " + car.getYear());
            if (car.getMileage() != null) pw.println("  Пробег: " + car.getMileage() + " км");
            pw.println("--------------------------------------------------------------------------------");
            pw.println("КЛИЕНТ:");
            pw.println("  Имя: " + name);
            pw.println("  Телефон: " + phone);
            pw.println("  Email: " + email);
            pw.println("  Дата бронирования: " + date);
            if (!notes.isEmpty()) pw.println("  Примечание: " + notes);
            pw.println("--------------------------------------------------------------------------------");
            pw.println("Статус: 🟡 ОЖИДАЕТ ПОДТВЕРЖДЕНИЯ");
            pw.println("================================================================================");
            pw.println();

            LoggerUtil.info("Бронирование сохранено в файл reservations.txt");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка записи бронирования в файл", e);
        }
    }

    private void savePurchaseToFile(String name, String phone, String email, String payment, String notes) {
        try (FileWriter fw = new FileWriter("purchases.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            pw.println("================================================================================");
            pw.println("ПОКУПКА #" + System.currentTimeMillis());
            pw.println("================================================================================");
            pw.println("Дата создания: " + timestamp);
            pw.println("Пользователь: " + SessionManager.getCurrentUsername());
            pw.println("--------------------------------------------------------------------------------");
            pw.println("АВТОМОБИЛЬ:");
            pw.println("  Название: " + car.getName());
            pw.println("  Модель: " + car.getModel());
            pw.println("  Цена: " + String.format("₸ %,.2f", car.getPrice()));
            if (car.getYear() != null) pw.println("  Год: " + car.getYear());
            if (car.getMileage() != null) pw.println("  Пробег: " + car.getMileage() + " км");
            pw.println("--------------------------------------------------------------------------------");
            pw.println("КЛИЕНТ:");
            pw.println("  Имя: " + name);
            pw.println("  Телефон: " + phone);
            pw.println("  Email: " + email);
            pw.println("  Способ оплаты: " + payment);
            if (!notes.isEmpty()) pw.println("  Примечание: " + notes);
            pw.println("--------------------------------------------------------------------------------");
            pw.println("Сумма к оплате: " + String.format("₸ %,.2f", car.getPrice()));
            pw.println("Статус: 🟡 ОЖИДАЕТ ОПЛАТЫ");
            pw.println("================================================================================");
            pw.println();

            LoggerUtil.info("Покупка сохранена в файл purchases.txt");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка записи покупки в файл", e);
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String nvl(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    @FXML
    private void close() {
        ((Stage) nameLabel.getScene().getWindow()).close();
    }
}

