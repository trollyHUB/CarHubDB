package utils;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * Кэш для изображений, чтобы не загружать их повторно
 */
public class ImageCache {

    private static final Map<String, Image> cache = new HashMap<>();
    private static final int MAX_CACHE_SIZE = 50; // Максимум 50 изображений в кэше

    /**
     * Получить изображение из кэша или загрузить новое
     */
    public static Image getImage(String url) {
        if (url == null || url.isBlank()) {
            return getPlaceholder();
        }

        // Проверяем кэш
        if (cache.containsKey(url)) {
            return cache.get(url);
        }

        // Загружаем новое изображение
        Image image = loadImage(url);

        // Добавляем в кэш (если кэш не переполнен)
        if (cache.size() < MAX_CACHE_SIZE) {
            cache.put(url, image);
        }

        return image;
    }

    /**
     * Загрузка изображения с обработкой ошибок
     */
    private static Image loadImage(String url) {
        try {
            // Локальный файл из resources
            if (url.startsWith("/resources/") || url.startsWith("resources/")) {
                String resourcePath = url.startsWith("/") ? url : "/" + url;
                var resourceUrl = ImageCache.class.getResource(resourcePath);
                if (resourceUrl != null) {
                    return new Image(resourceUrl.toExternalForm(), true);
                }
            }

            // URL из интернета или локальный файл
            return new Image(url, true);

        } catch (Exception e) {
            System.err.println("⚠️ Ошибка загрузки изображения: " + url);
            return getPlaceholder();
        }
    }

    /**
     * Плейсхолдер для отсутствующих изображений
     */
    private static Image getPlaceholder() {
        String placeholderUrl = "https://via.placeholder.com/400x250?text=No+Image";
        if (!cache.containsKey(placeholderUrl)) {
            cache.put(placeholderUrl, new Image(placeholderUrl, true));
        }
        return cache.get(placeholderUrl);
    }

    /**
     * Очистить кэш (полезно при выходе из приложения)
     */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * Получить размер кэша
     */
    public static int getCacheSize() {
        return cache.size();
    }
}

