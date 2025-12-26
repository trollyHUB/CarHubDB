package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Car;
import services.CarImagesService;
import utils.ValidationUtil;
import utils.NotificationUtil;
import utils.LoggerUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CarFormController {

    @FXML private TextField nameField;
    @FXML private TextField modelField;
    @FXML private TextField priceField;

    // Новые поля
    @FXML private TextField brandField;
    @FXML private TextField yearField;
    @FXML private TextField mileageField;
    @FXML private TextArea descriptionArea;

    // Поля для фото
    @FXML private Label photosCountLabel;
    @FXML private VBox photosListBox;

    private Car editingCar; // null = добавление, не null = редактирование
    private Runnable onSaveCallback;
    private final List<File> selectedPhotos = new ArrayList<>();

    public void setCar(Car car) {
        this.editingCar = car;
        if (car != null) {
            nameField.setText(car.getName());
            modelField.setText(car.getModel());
            priceField.setText(String.valueOf(car.getPrice()));
            if (brandField != null) brandField.setText(car.getBrand());
            if (yearField != null && car.getYear() != null) yearField.setText(String.valueOf(car.getYear()));
            if (mileageField != null && car.getMileage() != null) mileageField.setText(String.valueOf(car.getMileage()));
            if (descriptionArea != null) descriptionArea.setText(car.getDescription());

            // ✅ ЗАГРУЖАЕМ СУЩЕСТВУЮЩИЕ ФОТОГРАФИИ
            loadExistingPhotos(car.getId());
        }
    }

    /**
     * Загружает существующие фотографии автомобиля из БД
     */
    private void loadExistingPhotos(int carId) {
        try {
            List<models.CarImage> images = CarImagesService.getCarImages(carId);

            if (!images.isEmpty()) {
                // Очищаем список и добавляем файлы из БД
                selectedPhotos.clear();

                for (models.CarImage img : images) {
                    try {
                        // Конвертируем URL в File
                        java.net.URI uri = new java.net.URI(img.getImageUrl());
                        File file = new File(uri);
                        if (file.exists()) {
                            selectedPhotos.add(file);
                        }
                    } catch (Exception e) {
                        LoggerUtil.error("Ошибка загрузки фото: " + img.getImageUrl(), e);
                    }
                }

                // Обновляем отображение
                updatePhotosList();

                LoggerUtil.info("Загружено " + selectedPhotos.size() + " существующих фото для автомобиля ID=" + carId);
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка загрузки существующих фото", e);
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void addPhotos() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите фото автомобиля");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(nameField.getScene().getWindow());

        if (files != null && !files.isEmpty()) {
            selectedPhotos.addAll(files);
            updatePhotosList();
        }
    }

    private void updatePhotosList() {
        photosListBox.getChildren().clear();

        if (photosCountLabel != null) {
            photosCountLabel.setText("(" + selectedPhotos.size() + " фото)");
        }

        for (int i = 0; i < selectedPhotos.size(); i++) {
            final int index = i;
            File photo = selectedPhotos.get(i);

            HBox photoItem = new HBox(10);
            photoItem.setAlignment(Pos.CENTER_LEFT);
            photoItem.setStyle("-fx-padding: 5; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");

            Label indexLabel = new Label((i + 1) + ".");
            indexLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");

            Label nameLabel = new Label(photo.getName());
            nameLabel.setStyle("-fx-text-fill: #333;");
            HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

            CheckBox mainCheckbox = new CheckBox("Главное");
            if (i == 0) {
                mainCheckbox.setSelected(true);
            }
            mainCheckbox.setOnAction(e -> {
                // Снять выбор с других
                photosListBox.getChildren().forEach(node -> {
                    if (node instanceof HBox) {
                        ((HBox) node).getChildren().forEach(child -> {
                            if (child instanceof CheckBox && child != mainCheckbox) {
                                ((CheckBox) child).setSelected(false);
                            }
                        });
                    }
                });
            });

            Button deleteBtn = new Button("🗑️");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                selectedPhotos.remove(index);
                updatePhotosList();
            });

            photoItem.getChildren().addAll(indexLabel, nameLabel, mainCheckbox, deleteBtn);
            photosListBox.getChildren().add(photoItem);
        }
    }


    @FXML
    private void saveCar() {
        String name = nameField.getText();
        String model = modelField.getText();
        String priceText = priceField.getText();
        String brand = brandField != null ? brandField.getText() : null;
        String yearText = yearField != null ? yearField.getText() : null;
        String mileageText = mileageField != null ? mileageField.getText() : null;
        String description = descriptionArea != null ? descriptionArea.getText() : null;

        // ✅ ВАЛИДАЦИЯ ОБЯЗАТЕЛЬНЫХ ПОЛЕЙ
        if (!ValidationUtil.isNotEmpty(name)) {
            showError("Название", "не может быть пустым");
            return;
        }
        if (!ValidationUtil.isValidLength(name, 2, 100)) {
            showError("Название", "должно быть от 2 до 100 символов");
            return;
        }

        if (!ValidationUtil.isNotEmpty(model)) {
            showError("Модель", "не может быть пустой");
            return;
        }
        if (!ValidationUtil.isValidLength(model, 1, 50)) {
            showError("Модель", "должна быть от 1 до 50 символов");
            return;
        }

        if (!ValidationUtil.isValidPrice(priceText)) {
            showError("Цена", "должна быть положительным числом");
            return;
        }

        // ✅ ВАЛИДАЦИЯ ОПЦИОНАЛЬНЫХ ПОЛЕЙ
        if (brand != null && !brand.isBlank() && !ValidationUtil.isValidLength(brand, 2, 50)) {
            showError("Бренд", "должен быть от 2 до 50 символов");
            return;
        }

        if (yearText != null && !yearText.isBlank() && !ValidationUtil.isValidYear(yearText)) {
            showError("Год", "должен быть в диапазоне 1900-2030");
            return;
        }

        if (mileageText != null && !mileageText.isBlank() && !ValidationUtil.isValidMileage(mileageText)) {
            showError("Пробег", "должен быть от 0 до 999999 км");
            return;
        }

        if (description != null && !description.isBlank() && description.length() > 500) {
            showError("Описание", "не должно превышать 500 символов");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            Integer year = (yearText == null || yearText.isBlank()) ? null : Integer.parseInt(yearText);
            Integer mileage = (mileageText == null || mileageText.isBlank()) ? null : Integer.parseInt(mileageText);

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    new Alert(Alert.AlertType.ERROR, "❌ Нет подключения к базе данных").show();
                    return;
                }

                int carId;

                if (editingCar == null) {
                    // ДОБАВЛЕНИЕ НОВОГО АВТО
                    String sql = "INSERT INTO Cars (name, model, price, brand, year, mileage, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, name);
                    stmt.setString(2, model);
                    stmt.setDouble(3, price);
                    stmt.setString(4, emptyToNull(brand));
                    if (year == null) stmt.setNull(5, java.sql.Types.INTEGER); else stmt.setInt(5, year);
                    if (mileage == null) stmt.setNull(6, java.sql.Types.INTEGER); else stmt.setInt(6, mileage);
                    stmt.setString(7, emptyToNull(description));

                    stmt.executeUpdate();

                    // Получаем ID нового авто
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        carId = rs.getInt(1);
                    } else {
                        throw new Exception("Не удалось получить ID нового автомобиля");
                    }

                    LoggerUtil.action("Добавлен автомобиль: " + name + " " + model);
                } else {
                    // РЕДАКТИРОВАНИЕ СУЩЕСТВУЮЩЕГО АВТО
                    carId = editingCar.getId();
                    String sql = "UPDATE Cars SET name=?, model=?, price=?, brand=?, year=?, mileage=?, description=? WHERE id=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.setString(2, model);
                    stmt.setDouble(3, price);
                    stmt.setString(4, emptyToNull(brand));
                    if (year == null) stmt.setNull(5, java.sql.Types.INTEGER); else stmt.setInt(5, year);
                    if (mileage == null) stmt.setNull(6, java.sql.Types.INTEGER); else stmt.setInt(6, mileage);
                    stmt.setString(7, emptyToNull(description));
                    stmt.setInt(8, editingCar.getId());

                    stmt.executeUpdate();
                    LoggerUtil.action("Обновлён автомобиль: " + name + " " + model);
                }

                // СОХРАНЕНИЕ ФОТО В ТАБЛИЦУ CarImages
                if (!selectedPhotos.isEmpty()) {
                    // Определяем какое фото главное
                    int mainPhotoIndex = 0;
                    for (int i = 0; i < photosListBox.getChildren().size(); i++) {
                        javafx.scene.Node node = photosListBox.getChildren().get(i);
                        if (node instanceof HBox) {
                            for (javafx.scene.Node child : ((HBox) node).getChildren()) {
                                if (child instanceof CheckBox && ((CheckBox) child).isSelected()) {
                                    mainPhotoIndex = i;
                                    break;
                                }
                            }
                        }
                    }

                    // Сохраняем все фото
                    for (int i = 0; i < selectedPhotos.size(); i++) {
                        File photoFile = selectedPhotos.get(i);
                        String imageUrl = photoFile.toURI().toString();
                        boolean isMain = (i == mainPhotoIndex);

                        CarImagesService.addImage(carId, imageUrl, isMain);
                    }

                    LoggerUtil.info("Добавлено " + selectedPhotos.size() + " фото для автомобиля ID=" + carId);
                }

                NotificationUtil.showSuccess(editingCar == null ? "Автомобиль добавлен!" : "Автомобиль обновлён!");

                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }

                close();
            }
        } catch (NumberFormatException e) {
            showError("Ошибка", "Проверьте формат числовых полей");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "❌ Ошибка сохранения: " + e.getMessage()).show();
            LoggerUtil.error("Ошибка сохранения автомобиля", e);
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private void showError(String fieldName, String issue) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка валидации");
        alert.setHeaderText(ValidationUtil.formatValidationError(fieldName, issue));
        alert.showAndWait();
    }

    private void close() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void cancel() {
        close();
    }
}
