package utils;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Утилита для показа красивых уведомлений (Toast/Snackbar)
 * Вместо стандартных Alert создаёт современные всплывающие уведомления
 */
public class NotificationUtil {

    /**
     * Типы уведомлений
     */
    public enum NotificationType {
        SUCCESS,  // Успех (зелёный)
        ERROR,    // Ошибка (красный)
        INFO,     // Информация (синий)
        WARNING   // Предупреждение (оранжевый)
    }

    /**
     * Показать уведомление об успехе
     */
    public static void showSuccess(String message) {
        show(message, NotificationType.SUCCESS);
    }

    /**
     * Показать уведомление об ошибке
     */
    public static void showError(String message) {
        show(message, NotificationType.ERROR);
    }

    /**
     * Показать информационное уведомление
     */
    public static void showInfo(String message) {
        show(message, NotificationType.INFO);
    }

    /**
     * Показать предупреждение
     */
    public static void showWarning(String message) {
        show(message, NotificationType.WARNING);
    }

    /**
     * Показать уведомление с заданным типом
     */
    public static void show(String message, NotificationType type) {
        javafx.application.Platform.runLater(() -> {
            // Создаём окно для уведомления
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setAlwaysOnTop(true);

            // Создаём контейнер
            VBox root = new VBox(10);
            root.setAlignment(Pos.CENTER);
            root.setPrefWidth(400);
            root.setMaxWidth(400);

            // Иконка
            Label icon = new Label(getIcon(type));
            icon.setStyle("-fx-font-size: 32px;");

            // Сообщение
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setAlignment(Pos.CENTER);
            messageLabel.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 0 20 0 20;"
            );

            // Добавляем в контейнер
            root.getChildren().addAll(icon, messageLabel);

            // Применяем стиль в зависимости от типа
            root.setStyle(getStyle(type));

            // Оборачиваем в StackPane для центрирования
            StackPane wrapper = new StackPane(root);
            wrapper.setStyle("-fx-background-color: transparent;");

            // Создаём сцену
            Scene scene = new Scene(wrapper);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);

            // Позиционируем окно (правый верхний угол)
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            stage.setX(bounds.getMaxX() - 420);
            stage.setY(20);

            // Анимация появления (slide in + fade in)
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), root);
            slideIn.setFromX(500);
            slideIn.setToX(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ParallelTransition showTransition = new ParallelTransition(slideIn, fadeIn);

            // Анимация исчезновения (slide out + fade out)
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), root);
            slideOut.setFromX(0);
            slideOut.setToX(500);
            slideOut.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            ParallelTransition hideTransition = new ParallelTransition(slideOut, fadeOut);
            hideTransition.setOnFinished(e -> stage.close());

            // Показываем окно
            stage.show();

            // Запускаем анимацию появления
            showTransition.play();

            // Автоматически скрываем через 3 секунды
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> hideTransition.play());
            pause.play();

            // Закрываем при клике
            root.setOnMouseClicked(e -> {
                pause.stop();
                hideTransition.play();
            });
        });
    }

    /**
     * Получить иконку для типа уведомления
     */
    private static String getIcon(NotificationType type) {
        switch (type) {
            case SUCCESS:
                return "✅";
            case ERROR:
                return "❌";
            case INFO:
                return "ℹ️";
            case WARNING:
                return "⚠️";
            default:
                return "📢";
        }
    }

    /**
     * Получить стиль для типа уведомления
     */
    private static String getStyle(NotificationType type) {
        String baseStyle =
            "-fx-padding: 20 30 20 30; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);";

        switch (type) {
            case SUCCESS:
                return baseStyle +
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #10b981 0%, #059669 100%);";
            case ERROR:
                return baseStyle +
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ef4444 0%, #dc2626 100%);";
            case INFO:
                return baseStyle +
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3b82f6 0%, #2563eb 100%);";
            case WARNING:
                return baseStyle +
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #f59e0b 0%, #d97706 100%);";
            default:
                return baseStyle +
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #6b7280 0%, #4b5563 100%);";
        }
    }

    /**
     * Показать уведомление с заголовком
     */
    public static void showWithTitle(String title, String message, NotificationType type) {
        javafx.application.Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setAlwaysOnTop(true);

            VBox root = new VBox(5);
            root.setAlignment(Pos.CENTER_LEFT);
            root.setPrefWidth(400);
            root.setMaxWidth(400);

            // Иконка + заголовок
            javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label icon = new Label(getIcon(type));
            icon.setStyle("-fx-font-size: 24px;");

            Label titleLabel = new Label(title);
            titleLabel.setStyle(
                "-fx-font-size: 16px; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold;"
            );

            header.getChildren().addAll(icon, titleLabel);

            // Сообщение
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setStyle(
                "-fx-font-size: 13px; " +
                "-fx-text-fill: rgba(255,255,255,0.9); " +
                "-fx-padding: 0 0 0 34;"
            );

            root.getChildren().addAll(header, messageLabel);
            root.setStyle(getStyle(type));

            StackPane wrapper = new StackPane(root);
            wrapper.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(wrapper);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);

            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            stage.setX(bounds.getMaxX() - 420);
            stage.setY(20);

            // Анимации (те же что и выше)
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), root);
            slideIn.setFromX(500);
            slideIn.setToX(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ParallelTransition showTransition = new ParallelTransition(slideIn, fadeIn);

            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), root);
            slideOut.setFromX(0);
            slideOut.setToX(500);
            slideOut.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            ParallelTransition hideTransition = new ParallelTransition(slideOut, fadeOut);
            hideTransition.setOnFinished(e -> stage.close());

            stage.show();
            showTransition.play();

            PauseTransition pause = new PauseTransition(Duration.seconds(4));
            pause.setOnFinished(e -> hideTransition.play());
            pause.play();

            root.setOnMouseClicked(e -> {
                pause.stop();
                hideTransition.play();
            });
        });
    }
}

