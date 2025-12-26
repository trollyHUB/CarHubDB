package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import database.DatabaseConnection;
import utils.LoggerUtil;

import java.sql.*;

public class LandingController {

    @FXML
    private Label totalCarsLabel;
    @FXML
    private Label happyClientsLabel;
    @FXML
    private VBox aboutSection;
    @FXML
    private VBox featuresSection;
    @FXML
    private VBox contactsSection;
    @FXML
    private ScrollPane mainScrollPane;

    // Карточки "О нас"
    @FXML
    private VBox card1;
    @FXML
    private VBox card2;
    @FXML
    private VBox card3;

    // Карточки преимуществ
    @FXML
    private VBox featureCard1;
    @FXML
    private VBox featureCard2;
    @FXML
    private VBox featureCard3;
    @FXML
    private VBox featureCard4;
    @FXML
    private VBox featureCard5;
    @FXML
    private VBox featureCard6;

    public void initialize() {
        loadStatistics();
        setupCardHoverEffects();
    }

    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Подсчёт автомобилей
            String carsSql = "SELECT COUNT(*) FROM Cars";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(carsSql);
            if (rs.next()) {
                int carsCount = rs.getInt(1);
                totalCarsLabel.setText(carsCount + "+");
            }

            // Подсчёт пользователей (довольных клиентов)
            String usersSql = "SELECT COUNT(*) FROM users_secure WHERE role = 'user'";
            ResultSet rsUsers = stmt.executeQuery(usersSql);
            if (rsUsers.next()) {
                int usersCount = rsUsers.getInt(1);
                happyClientsLabel.setText(usersCount + "+");
            }

        } catch (SQLException e) {
            LoggerUtil.error("Ошибка загрузки статистики на главной", e);
        }
    }

    private void setupCardHoverEffects() {
        // Создаём эффекты для карточек "О нас"
        VBox[] aboutCards = {card1, card2, card3};
        for (VBox card : aboutCards) {
            if (card != null) {
                addHoverEffect(card,
                    "-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 5); -fx-cursor: hand;",
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #667eea 0%, #764ba2 100%); -fx-padding: 30; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(102,126,234,0.5), 30, 0, 0, 10); -fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"
                );
            }
        }

        // Создаём эффекты для карточек преимуществ (обновлённые размеры)
        VBox[] featureCards = {featureCard1, featureCard2, featureCard3, featureCard4, featureCard5, featureCard6};
        for (VBox card : featureCards) {
            if (card != null) {
                addHoverEffect(card,
                    "-fx-background-color: #f8f9fa; -fx-padding: 35; -fx-background-radius: 12; -fx-pref-width: 320; -fx-min-height: 260; -fx-cursor: hand;",
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #667eea 0%, #764ba2 100%); -fx-padding: 35; -fx-background-radius: 12; -fx-pref-width: 320; -fx-min-height: 260; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(102,126,234,0.5), 30, 0, 0, 10); -fx-scale-x: 1.05; -fx-scale-y: 1.05;"
                );
            }
        }
    }

    private void addHoverEffect(VBox card, String normalStyle, String hoverStyle) {
        card.setOnMouseEntered(event -> {
            card.setStyle(hoverStyle);
            // Меняем цвет текста на белый при наведении
            card.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    String currentStyle = label.getStyle();
                    if (currentStyle.contains("-fx-text-fill: #333") || currentStyle.contains("-fx-text-fill: #666")) {
                        label.setStyle(currentStyle.replace("-fx-text-fill: #333", "-fx-text-fill: white")
                                                     .replace("-fx-text-fill: #666", "-fx-text-fill: rgba(255,255,255,0.95)"));
                    }
                }
            });
        });

        card.setOnMouseExited(event -> {
            card.setStyle(normalStyle);
            // Возвращаем исходный цвет текста
            card.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    String currentStyle = label.getStyle();
                    if (currentStyle.contains("-fx-text-fill: white") || currentStyle.contains("-fx-text-fill: rgba(255,255,255,0.95)")) {
                        label.setStyle(currentStyle.replace("-fx-text-fill: white", "-fx-text-fill: #333")
                                                     .replace("-fx-text-fill: rgba(255,255,255,0.95)", "-fx-text-fill: #666"));
                    }
                }
            });
        });
    }

    @FXML
    protected void scrollToAbout(ActionEvent event) {
        scrollToNode(aboutSection);
    }

    @FXML
    protected void scrollToFeatures(ActionEvent event) {
        scrollToNode(featuresSection);
    }

    @FXML
    protected void scrollToContacts(ActionEvent event) {
        scrollToNode(contactsSection);
    }

    private void scrollToNode(VBox node) {
        if (mainScrollPane != null && node != null) {
            javafx.application.Platform.runLater(() -> {
                try {
                    double contentHeight = mainScrollPane.getContent().getBoundsInLocal().getHeight();
                    double viewportHeight = mainScrollPane.getViewportBounds().getHeight();
                    double nodeY = node.getBoundsInParent().getMinY();

                    // Вычисляем позицию с небольшим отступом сверху
                    double targetY = Math.max(0, nodeY - 100);
                    double maxScroll = contentHeight - viewportHeight;

                    if (maxScroll > 0) {
                        double vValue = Math.min(1.0, targetY / maxScroll);
                        mainScrollPane.setVvalue(vValue);
                        LoggerUtil.info("✅ Прокрутка к секции: " + node.getId() + " (vValue=" + String.format("%.2f", vValue) + ")");
                    }
                } catch (Exception e) {
                    if (e != null) {
                        LoggerUtil.error("Ошибка прокрутки к секции", e);
                    }
                    node.requestFocus();
                }
            });
        } else {
            System.out.println("❌ ScrollPane или node = null (mainScrollPane=" + mainScrollPane + ", node=" + node + ")");
            if (node != null) {
                node.requestFocus();
            }
        }
    }

    @FXML
    protected void openLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/login-view.fxml"));
            Stage stage = (Stage) totalCarsLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Вход");
            LoggerUtil.action("Переход на страницу входа с главной");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия страницы входа", e);
        }
    }

    @FXML
    protected void openRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/register-view.fxml"));
            Stage stage = (Stage) totalCarsLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Регистрация");
            LoggerUtil.action("Переход на страницу регистрации с главной");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия страницы регистрации", e);
        }
    }

    @FXML
    protected void openCatalog(ActionEvent event) {
        try {
            // Показываем каталог без входа (гостевой режим)
            Parent root = FXMLLoader.load(getClass().getResource("/resources/login-view.fxml"));
            Stage stage = (Stage) totalCarsLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CarHub — Вход");
            LoggerUtil.action("Переход в каталог с главной (через вход)");
        } catch (Exception e) {
            LoggerUtil.error("Ошибка открытия каталога", e);
        }
    }

    @FXML
    protected void openTelegram(ActionEvent event) {
        showInfo("Telegram", "Скоро здесь будет ссылка на наш Telegram канал!");
    }

    @FXML
    protected void openFacebook(ActionEvent event) {
        showInfo("Facebook", "Скоро здесь будет ссылка на наш Facebook!");
    }

    @FXML
    protected void openInstagram(ActionEvent event) {
        showInfo("Instagram", "Скоро здесь будет ссылка на наш Instagram!");
    }

    @FXML
    protected void openYouTube(ActionEvent event) {
        showInfo("YouTube", "Скоро здесь будет ссылка на наш YouTube канал!");
    }

    private void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Методы для кнопок футера
    @FXML
    protected void openAboutUs(javafx.scene.input.MouseEvent event) {
        scrollToNode(aboutSection);
    }

    @FXML
    protected void openVacancies(javafx.scene.input.MouseEvent event) {
        showInfo("Вакансии", "Раздел вакансий находится в разработке.\nСледите за обновлениями!");
    }

    @FXML
    protected void openReviews(javafx.scene.input.MouseEvent event) {
        showInfo("Отзывы", "Раздел отзывов находится в разработке.\nСкоро здесь появятся отзывы наших клиентов!");
    }

    @FXML
    protected void openHowToBuy(javafx.scene.input.MouseEvent event) {
        showInfo("Как купить",
            "1. Выберите автомобиль в каталоге\n" +
            "2. Нажмите 'Забронировать' или 'Купить'\n" +
            "3. Заполните форму заявки\n" +
            "4. Наш менеджер свяжется с вами!");
    }

    @FXML
    protected void openTradeIn(javafx.scene.input.MouseEvent event) {
        showInfo("Trade-in", "Программа Trade-in находится в разработке.\nВы сможете обменять свой старый автомобиль на новый!");
    }

    @FXML
    protected void openFAQ(javafx.scene.input.MouseEvent event) {
        showInfo("FAQ",
            "Часто задаваемые вопросы:\n\n" +
            "Q: Есть ли гарантия?\n" +
            "A: Да, все автомобили с гарантией от 6 месяцев.\n\n" +
            "Q: Можно ли оформить кредит?\n" +
            "A: Да, мы работаем со всеми банками.\n\n" +
            "Q: Есть ли доставка?\n" +
            "A: Да, доставка по всей России!");
    }

    @FXML
    protected void openWarranty(javafx.scene.input.MouseEvent event) {
        showInfo("Гарантия", "Все автомобили проходят проверку и имеют гарантию от 6 до 24 месяцев в зависимости от модели.");
    }

    // Перегруженные методы для MouseEvent в футере
    @FXML
    protected void openCatalogFromFooter(javafx.scene.input.MouseEvent event) {
        openCatalog(null);
    }

    @FXML
    protected void scrollToContactsFromFooter(javafx.scene.input.MouseEvent event) {
        scrollToContacts(null);
    }
}

