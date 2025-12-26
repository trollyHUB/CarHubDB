package models;

import java.time.LocalDateTime;

/**
 * Модель для хранения фото автомобиля
 */
public class CarImage {
    private int id;
    private int carId;
    private String imageUrl;
    private boolean isMain;
    private int displayOrder;
    private LocalDateTime createdAt;

    // Конструктор по умолчанию
    public CarImage() {
    }

    // Конструктор с основными полями
    public CarImage(int carId, String imageUrl, boolean isMain) {
        this.carId = carId;
        this.imageUrl = imageUrl;
        this.isMain = isMain;
    }

    // Полный конструктор
    public CarImage(int id, int carId, String imageUrl, boolean isMain, int displayOrder, LocalDateTime createdAt) {
        this.id = id;
        this.carId = carId;
        this.imageUrl = imageUrl;
        this.isMain = isMain;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CarImage{" +
                "id=" + id +
                ", carId=" + carId +
                ", imageUrl='" + imageUrl + '\'' +
                ", isMain=" + isMain +
                ", displayOrder=" + displayOrder +
                '}';
    }
}

