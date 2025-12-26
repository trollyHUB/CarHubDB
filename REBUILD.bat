@echo off
chcp 65001 > nul
echo ========================================
echo 🔨 ПЕРЕСБОРКА ПРОЕКТА CarHubDB
echo ========================================
echo.

echo 📁 Копирую обновленный FXML...
copy /Y "src\resources\admin-orders-view.fxml" "out\production\CarHubDB\resources\admin-orders-view.fxml" > nul
if %errorlevel% equ 0 (
    echo ✅ FXML файл скопирован
) else (
    echo ❌ Ошибка копирования FXML
)

echo.
echo ========================================
echo ⚠️ ТЕПЕРЬ СДЕЛАЙТЕ В INTELLIJ IDEA:
echo ========================================
echo.
echo 1. Build → Rebuild Project
echo 2. Дождитесь завершения
echo 3. Run 'Main'
echo.
echo ========================================
echo 📊 СТАТУС ИСПРАВЛЕНИЙ:
echo ========================================
echo ✅ AdminOrdersController.java - метод goBack()
echo ✅ admin-orders-view.fxml - дизайн header
echo ✅ StatisticsService.java - SQL запрос purchases
echo ⚠️ ТРЕБУЕТСЯ: Rebuild Project в IDE!
echo ========================================
pause

