import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.LoggerUtil;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // ✅ ЛОГИРУЕМ ЗАПУСК ПРИЛОЖЕНИЯ
        LoggerUtil.info("═══════════════════════════════════════════════════════════");
        LoggerUtil.info("🚀 ЗАПУСК ПРИЛОЖЕНИЯ CARHUB");
        LoggerUtil.info("═══════════════════════════════════════════════════════════");

        // Запускаем с главной страницы (Landing Page)
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/landing-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("CarHub Kazakhstan — Premium Auto в Астане 🇰🇿");
        stage.setScene(scene);
        stage.setMaximized(true); // Открываем на весь экран
        stage.show();

        LoggerUtil.info("✅ Главная страница открыта");
    }

    public static void main(String[] args) {
        launch();
    }
}
