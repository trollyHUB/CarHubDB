package models;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private int carId;
    private String carName;
    private int userId;
    private String userName;
    private String customerName;
    private String phone;
    private String email;
    private LocalDateTime reservationDate;
    private String status; // pending, approved, completed, cancelled
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Полный конструктор
    public Reservation(int id, int carId, String carName, int userId, String userName,
                      String customerName, String phone, String email,
                      LocalDateTime reservationDate, String status, String notes,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.carId = carId;
        this.carName = carName;
        this.userId = userId;
        this.userName = userName;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.reservationDate = reservationDate;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getId() { return id; }
    public int getCarId() { return carId; }
    public String getCarName() { return carName; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public LocalDateTime getReservationDate() { return reservationDate; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Получить статус на русском
     */
    public String getStatusRu() {
        switch (status) {
            case "pending": return "🟡 Ожидает";
            case "approved": return "🟢 Одобрено";
            case "completed": return "✅ Завершено";
            case "cancelled": return "❌ Отменено";
            default: return status;
        }
    }
}

