package models;

public class Car {
    private int id;
    private String name;
    private String model;
    private double price;

    // Новые поля
    private String brand;       // марка
    private Integer year;       // год выпуска
    private Integer mileage;    // пробег (км)
    private String description; // описание
    private String imageUrl;    // ссылка на фото

    // Базовый конструктор (совместимость со старым кодом)
    public Car(int id, String name, String model, double price) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.price = price;
    }

    // Расширенный конструктор
    public Car(int id, String name, String model, double price,
               String brand, Integer year, Integer mileage,
               String description, String imageUrl) {
        this(id, name, model, price);
        this.brand = brand;
        this.year = year;
        this.mileage = mileage;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getModel() { return model; }
    public double getPrice() { return price; }

    public void setName(String name) { this.name = name; }
    public void setModel(String model) { this.model = model; }
    public void setPrice(double price) { this.price = price; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
