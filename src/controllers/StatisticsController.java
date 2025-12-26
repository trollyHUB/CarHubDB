package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Car;
import services.StatisticsService;
import utils.SessionManager;

import java.util.Map;

public class StatisticsController {

    @FXML private Label totalCarsLabel;
    @FXML private Label avgPriceLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label avgMileageLabel;
    @FXML private Label mostExpensiveLabel;
    @FXML private Label mostExpensivePriceLabel;
    @FXML private Label cheapestLabel;
    @FXML private Label cheapestPriceLabel;
    @FXML private VBox topBrandsContainer;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalFavoritesLabel;
    @FXML private Label totalActiveUsersLabel;
    @FXML private Label totalInactiveUsersLabel;
    @FXML private Label totalReservationsLabel;
    @FXML private Label totalPurchasesLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private Label totalRatingsLabel;

    public void initialize() {
        loadStatistics();
    }

    private void loadStatistics() {
        System.out.println("📊 ========== ЗАГРУЗКА СТАТИСТИКИ ==========");

        // Основные показатели
        int totalCars = StatisticsService.getTotalCars();
        System.out.println("🚗 Всего автомобилей: " + totalCars);
        totalCarsLabel.setText(String.valueOf(totalCars));

        double avgPrice = StatisticsService.getAveragePrice();
        System.out.println("💰 Средняя цена: " + avgPrice);
        avgPriceLabel.setText(String.format("₸ %,.0f", avgPrice));

        int totalUsers = StatisticsService.getTotalUsers();
        System.out.println("👥 Всего пользователей: " + totalUsers);
        totalUsersLabel.setText(String.valueOf(totalUsers));

        double avgMileage = StatisticsService.getAverageMileage();
        System.out.println("🛣️ Средний пробег: " + avgMileage);
        avgMileageLabel.setText(String.format("%,.0f км", avgMileage));

        // Самое дорогое/дешёвое
        Car mostExpensive = StatisticsService.getMostExpensiveCar();
        if (mostExpensive != null) {
            System.out.println("💎 Самое дорогое: " + mostExpensive.getName());
            mostExpensiveLabel.setText(mostExpensive.getName() + " " + mostExpensive.getModel());
            mostExpensivePriceLabel.setText(utils.PriceFormatter.formatWithPrefix(mostExpensive.getPrice()));
        } else {
            System.out.println("⚠️ Самое дорогое: нет данных");
            mostExpensiveLabel.setText("Нет данных");
            mostExpensivePriceLabel.setText("₸ 0");
        }

        Car cheapest = StatisticsService.getCheapestCar();
        if (cheapest != null) {
            System.out.println("💵 Самое дешёвое: " + cheapest.getName());
            cheapestLabel.setText(cheapest.getName() + " " + cheapest.getModel());
            cheapestPriceLabel.setText(utils.PriceFormatter.formatWithPrefix(cheapest.getPrice()));
        } else {
            System.out.println("⚠️ Самое дешёвое: нет данных");
            cheapestLabel.setText("Нет данных");
            cheapestPriceLabel.setText("₸ 0");
        }

        // Топ-5 брендов
        loadTopBrands();

        // Дополнительная статистика
        int totalAdmins = StatisticsService.getTotalAdmins();
        System.out.println("👑 Всего админов: " + totalAdmins);
        totalAdminsLabel.setText(String.valueOf(totalAdmins));
        setupCardClickHandler(totalAdminsLabel, "admins");

        int totalFavorites = StatisticsService.getTotalFavorites();
        System.out.println("❤️ Всего избранных: " + totalFavorites);
        totalFavoritesLabel.setText(String.valueOf(totalFavorites));
        setupCardClickHandler(totalFavoritesLabel, "favorites");

        int totalActiveUsers = StatisticsService.getTotalActiveUsers();
        System.out.println("✅ Активные пользователи: " + totalActiveUsers);
        totalActiveUsersLabel.setText(String.valueOf(totalActiveUsers));
        setupCardClickHandler(totalActiveUsersLabel, "active_users");

        int totalInactiveUsers = StatisticsService.getTotalInactiveUsers();
        System.out.println("❌ Неактивные пользователи: " + totalInactiveUsers);
        totalInactiveUsersLabel.setText(String.valueOf(totalInactiveUsers));
        setupCardClickHandler(totalInactiveUsersLabel, "inactive_users");

        int totalReservations = StatisticsService.getTotalReservations();
        System.out.println("📅 Всего бронирований: " + totalReservations);
        totalReservationsLabel.setText(String.valueOf(totalReservations));
        setupCardClickHandler(totalReservationsLabel, "reservations");

        int totalPurchases = StatisticsService.getTotalPurchases();
        System.out.println("🛒 Всего покупок: " + totalPurchases);
        totalPurchasesLabel.setText(String.valueOf(totalPurchases));
        setupCardClickHandler(totalPurchasesLabel, "purchases");

        int totalComments = StatisticsService.getTotalComments();
        System.out.println("💬 Всего комментариев: " + totalComments);
        totalCommentsLabel.setText(String.valueOf(totalComments));
        setupCardClickHandler(totalCommentsLabel, "comments");

        int totalRatings = StatisticsService.getTotalRatings();
        System.out.println("⭐ Всего оценок: " + totalRatings);
        totalRatingsLabel.setText(String.valueOf(totalRatings));
        setupCardClickHandler(totalRatingsLabel, "ratings");

        System.out.println("📊 ========== СТАТИСТИКА ЗАГРУЖЕНА ==========");
    }

    /**
     * Настройка обработчика двойного клика для карточки статистики
     */
    private void setupCardClickHandler(Label label, String statType) {
        // Находим родительский VBox (карточку)
        if (label.getParent() instanceof VBox) {
            VBox card = (VBox) label.getParent();

            // Добавляем эффект наведения
            card.setOnMouseEntered(e -> {
                card.setStyle(card.getStyle() + "-fx-cursor: hand; -fx-scale-x: 1.03; -fx-scale-y: 1.03;");
            });

            card.setOnMouseExited(e -> {
                card.setStyle(card.getStyle().replace("-fx-cursor: hand; -fx-scale-x: 1.03; -fx-scale-y: 1.03;", ""));
            });

            // Обработчик двойного клика
            card.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    openDetailsWindow(statType);
                }
            });
        }
    }

    private void loadTopBrands() {
        topBrandsContainer.getChildren().clear();
        Map<String, Integer> topBrands = StatisticsService.getTopBrands(5);

        if (topBrands.isEmpty()) {
            Label emptyLabel = new Label("Нет данных о брендах");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
            topBrandsContainer.getChildren().add(emptyLabel);
            return;
        }

        // Находим максимум для масштабирования и общее количество
        int maxCount = topBrands.values().stream().max(Integer::compare).orElse(1);
        int totalCount = topBrands.values().stream().mapToInt(Integer::intValue).sum();

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topBrands.entrySet()) {
            String brand = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / maxCount;
            double percentOfTotal = (double) count / totalCount * 100;

            // Контейнер для бренда с улучшенным дизайном
            VBox brandRow = new VBox(8);
            brandRow.setPadding(new Insets(15, 0, 15, 0));
            brandRow.setStyle(
                "-fx-background-color: #FAFAFA; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);"
            );

            // Заголовок (место, логотип, бренд, количество, процент)
            HBox header = new HBox(15);
            header.setStyle("-fx-alignment: center-left;");

            // Место с медалью
            Label rankLabel = new Label(getRankEmoji(rank) + " #" + rank);
            rankLabel.setStyle(
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold; " +
                "-fx-min-width: 70; " +
                "-fx-text-fill: " + getBrandColor(rank) + ";"
            );

            // Логотип бренда (эмодзи)
            Label logoLabel = new Label(getBrandLogo(brand));
            logoLabel.setStyle("-fx-font-size: 32px;");

            // Название бренда
            VBox brandInfo = new VBox(3);
            Label brandLabel = new Label(brand.toUpperCase());
            brandLabel.setStyle(
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #333;"
            );

            Label brandCountry = new Label(getBrandCountry(brand));
            brandCountry.setStyle(
                "-fx-font-size: 12px; " +
                "-fx-text-fill: #999; " +
                "-fx-font-style: italic;"
            );
            brandInfo.getChildren().addAll(brandLabel, brandCountry);

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            // Статистика
            VBox stats = new VBox(3);
            stats.setStyle("-fx-alignment: center-right;");

            Label countLabel = new Label(count + " автомобилей");
            countLabel.setStyle(
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #333;"
            );

            Label percentLabel = new Label(String.format("%.1f%% от топ-5", percentOfTotal));
            percentLabel.setStyle(
                "-fx-font-size: 12px; " +
                "-fx-text-fill: #666;"
            );
            stats.getChildren().addAll(countLabel, percentLabel);

            header.getChildren().addAll(rankLabel, logoLabel, brandInfo, spacer, stats);

            // Прогресс-бар с градиентом
            ProgressBar progressBar = new ProgressBar(percentage);
            progressBar.setPrefWidth(650);
            progressBar.setPrefHeight(25);
            progressBar.setStyle(
                "-fx-accent: linear-gradient(to right, " + getBrandColor(rank) + ", " +
                adjustBrightness(getBrandColor(rank), 1.3) + ");"
            );

            brandRow.getChildren().addAll(header, progressBar);
            topBrandsContainer.getChildren().add(brandRow);

            rank++;
        }
    }

    /**
     * Получить логотип (эмодзи) для бренда
     */
    private String getBrandLogo(String brand) {
        String brandLower = brand.toLowerCase();

        // Японские бренды
        if (brandLower.contains("toyota")) return "🇯🇵";
        if (brandLower.contains("lexus")) return "💎";
        if (brandLower.contains("honda")) return "🔴";
        if (brandLower.contains("nissan")) return "⚪";
        if (brandLower.contains("mazda")) return "🔵";
        if (brandLower.contains("subaru")) return "⭐";
        if (brandLower.contains("suzuki")) return "🟦";
        if (brandLower.contains("mitsubishi")) return "♦️";

        // Немецкие бренды
        if (brandLower.contains("mercedes") || brandLower.contains("benz")) return "⭐";
        if (brandLower.contains("bmw")) return "🔷";
        if (brandLower.contains("audi")) return "🔘";
        if (brandLower.contains("volkswagen") || brandLower.contains("vw")) return "🔵";
        if (brandLower.contains("porsche")) return "🐎";

        // Американские бренды
        if (brandLower.contains("ford")) return "🦅";
        if (brandLower.contains("chevrolet") || brandLower.contains("chevy")) return "⚡";
        if (brandLower.contains("tesla")) return "⚡";
        if (brandLower.contains("jeep")) return "🏔️";

        // Корейские бренды
        if (brandLower.contains("hyundai")) return "🇰🇷";
        if (brandLower.contains("kia")) return "🟥";
        if (brandLower.contains("genesis")) return "✨";

        // Европейские бренды
        if (brandLower.contains("volvo")) return "🇸🇪";
        if (brandLower.contains("skoda")) return "🇨🇿";
        if (brandLower.contains("renault")) return "🇫🇷";
        if (brandLower.contains("peugeot")) return "🦁";

        // По умолчанию
        return "🚗";
    }

    /**
     * Получить страну происхождения бренда
     */
    private String getBrandCountry(String brand) {
        String brandLower = brand.toLowerCase();

        // Японские бренды
        if (brandLower.contains("toyota")) return "Япония 🇯🇵";
        if (brandLower.contains("lexus")) return "Япония (премиум) 🇯🇵";
        if (brandLower.contains("honda")) return "Япония 🇯🇵";
        if (brandLower.contains("nissan")) return "Япония 🇯🇵";
        if (brandLower.contains("mazda")) return "Япония 🇯🇵";
        if (brandLower.contains("subaru")) return "Япония 🇯🇵";
        if (brandLower.contains("suzuki")) return "Япония 🇯🇵";
        if (brandLower.contains("mitsubishi")) return "Япония 🇯🇵";

        // Немецкие бренды
        if (brandLower.contains("mercedes") || brandLower.contains("benz")) return "Германия (премиум) 🇩🇪";
        if (brandLower.contains("bmw")) return "Германия (премиум) 🇩🇪";
        if (brandLower.contains("audi")) return "Германия (премиум) 🇩🇪";
        if (brandLower.contains("volkswagen") || brandLower.contains("vw")) return "Германия 🇩🇪";
        if (brandLower.contains("porsche")) return "Германия (спорт) 🇩🇪";

        // Американские бренды
        if (brandLower.contains("ford")) return "США 🇺🇸";
        if (brandLower.contains("chevrolet") || brandLower.contains("chevy")) return "США 🇺🇸";
        if (brandLower.contains("tesla")) return "США (электро) 🇺🇸";
        if (brandLower.contains("jeep")) return "США (внедорожники) 🇺🇸";

        // Корейские бренды
        if (brandLower.contains("hyundai")) return "Южная Корея 🇰🇷";
        if (brandLower.contains("kia")) return "Южная Корея 🇰🇷";
        if (brandLower.contains("genesis")) return "Южная Корея (премиум) 🇰🇷";

        // Европейские бренды
        if (brandLower.contains("volvo")) return "Швеция 🇸🇪";
        if (brandLower.contains("skoda")) return "Чехия 🇨🇿";
        if (brandLower.contains("renault")) return "Франция 🇫🇷";
        if (brandLower.contains("peugeot")) return "Франция 🇫🇷";

        // По умолчанию
        return "Международный бренд 🌍";
    }

    /**
     * Изменить яркость цвета для градиента
     */
    private String adjustBrightness(String hexColor, double factor) {
        // Простое увеличение яркости для создания градиента
        return hexColor; // Упрощённая версия
    }

    private String getRankEmoji(int rank) {
        switch (rank) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            case 4: return "4️⃣";
            case 5: return "5️⃣";
            default: return "🔹";
        }
    }

    private String getBrandColor(int rank) {
        switch (rank) {
            case 1: return "#FFD700"; // Золото
            case 2: return "#C0C0C0"; // Серебро
            case 3: return "#CD7F32"; // Бронза
            case 4: return "#2196F3"; // Синий
            case 5: return "#4CAF50"; // Зелёный
            default: return "#9E9E9E"; // Серый
        }
    }

    @FXML
    protected void refreshStatistics() {
        utils.LoggerUtil.action("Обновление статистики");
        loadStatistics();
    }

    /**
     * Открыть окно с детальной информацией
     */
    protected void openDetailsWindow(String statType) {
        try {
            utils.LoggerUtil.action("Открытие детального окна: " + statType);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/stat-details-view.fxml"));
            Parent root = loader.load();

            StatDetailsController controller = loader.getController();
            controller.setStatType(statType);

            Stage stage = new Stage();
            stage.setTitle("Детальная информация");
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            utils.LoggerUtil.error("Ошибка открытия детального окна", e);
        }
    }

    @FXML
    protected void backToMain() {
        try {
            FXMLLoader loader;
            if ("admin".equals(SessionManager.getCurrentRole())) {
                loader = new FXMLLoader(getClass().getResource("/resources/carhub-admin-view.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/resources/carhub-user-view.fxml"));
            }

            Parent root = loader.load();
            Stage stage = (Stage) totalCarsLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 800);
            stage.setScene(scene);
            stage.centerOnScreen();

            utils.LoggerUtil.action("Возврат в главное меню из статистики");
        } catch (Exception e) {
            utils.LoggerUtil.error("Ошибка возврата в главное меню", e);
        }
    }

    @FXML
    protected void logout() {
        try {
            String username = SessionManager.getCurrentUsername();
            SessionManager.logout();
            utils.LoggerUtil.logLogout(username);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/landing-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalCarsLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 800);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            utils.LoggerUtil.error("Ошибка выхода из системы", e);
        }
    }
}

