package models;

import java.time.LocalDateTime;

public class Purchase {
    private int id;
    private int carId;
    private String carName;
    private int userId;
    private String userName;
    private String customerName;
    private String phone;
    private String email;
    private double price;
    private String paymentMethod; // cash, card, bank_transfer, credit
    private String status; // pending, paid, completed, cancelled
    private String notes;
    private LocalDateTime purchaseDate;
    private LocalDateTime completedAt;

    // Полный конструктор
    public Purchase(int id, int carId, String carName, int userId, String userName,
                   String customerName, String phone, String email,
                   double price, String paymentMethod, String status, String notes,
                   LocalDateTime purchaseDate, LocalDateTime completedAt) {
        this.id = id;
        this.carId = carId;
        this.carName = carName;
        this.userId = userId;
        this.userName = userName;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.price = price;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.notes = notes;
        this.purchaseDate = purchaseDate;
        this.completedAt = completedAt;
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
    public double getPrice() { return price; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    /**
     * Получить статус на русском
     */
    public String getStatusRu() {
        switch (status) {
            case "pending": return "🟡 Ожидает оплаты";
            case "paid": return "💰 Оплачено";
            case "completed": return "✅ Завершено";
            case "cancelled": return "❌ Отменено";
            default: return status;
        }
    }

    /**
     * Получить способ оплаты на русском
     */
    public String getPaymentMethodRu() {
        if (paymentMethod == null) return "—";
        switch (paymentMethod) {
            case "cash": return "💵 Наличные";
            case "card": return "💳 Карта";
            case "bank_transfer": return "🏦 Перевод";
            case "credit": return "📊 Кредит";
            default: return paymentMethod;
        }
    }
}

