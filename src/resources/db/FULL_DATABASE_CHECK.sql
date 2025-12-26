-- ============================================
-- ПОЛНАЯ ПРОВЕРКА И ОПТИМИЗАЦИЯ БД CarHub
-- База данных: TestDB
-- Дата: 19 ноября 2025
-- ============================================

USE TestDB;
GO

PRINT '============================================';
PRINT '🚗 CARHUB - ПОЛНАЯ ПРОВЕРКА БАЗЫ ДАННЫХ';
PRINT '============================================';
PRINT '';

-- ============================================
-- ШАГ 1: Проверка существования всех таблиц
-- ============================================

PRINT '📋 ШАГ 1: ПРОВЕРКА ТАБЛИЦ';
PRINT '--------------------------------------------';

DECLARE @tables TABLE (table_name VARCHAR(50), status VARCHAR(10));

INSERT INTO @tables VALUES ('Cars', 'MISSING');
INSERT INTO @tables VALUES ('users_secure', 'MISSING');
INSERT INTO @tables VALUES ('favorites', 'MISSING');
INSERT INTO @tables VALUES ('comments_ratings', 'MISSING');
INSERT INTO @tables VALUES ('reservations', 'MISSING');
INSERT INTO @tables VALUES ('purchases', 'MISSING');

-- Обновляем статус существующих таблиц
UPDATE @tables SET status = 'OK'
WHERE table_name IN (SELECT name FROM sys.tables);

-- Выводим результаты
SELECT
    CASE
        WHEN status = 'OK' THEN '✅'
        ELSE '❌'
    END AS [Статус],
    table_name AS [Таблица],
    CASE
        WHEN status = 'OK' THEN 'Существует'
        ELSE 'НЕ НАЙДЕНА!'
    END AS [Результат]
FROM @tables;

PRINT '';

-- ============================================
-- ШАГ 1.5: СОЗДАНИЕ ОТСУТСТВУЮЩИХ ТАБЛИЦ
-- ============================================

PRINT '🔧 ШАГ 1.5: СОЗДАНИЕ ОТСУТСТВУЮЩИХ ТАБЛИЦ';
PRINT '--------------------------------------------';

-- Создание таблицы comments_ratings если её нет
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'comments_ratings')
BEGIN
    PRINT '⚠️  Таблица comments_ratings не найдена. Создаём...';

    -- Сначала удаляем старые таблицы Comments и Ratings если они есть
    IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Comments')
    BEGIN
        DROP TABLE Comments;
        PRINT '   ✅ Удалена старая таблица Comments';
    END

    IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Ratings')
    BEGIN
        DROP TABLE Ratings;
        PRINT '   ✅ Удалена старая таблица Ratings';
    END

    -- Создаём новую объединённую таблицу
    CREATE TABLE comments_ratings (
        id INT PRIMARY KEY IDENTITY(1,1),
        car_id INT NOT NULL,
        user_id INT NOT NULL,
        comment NVARCHAR(1000),
        rating INT CHECK (rating BETWEEN 1 AND 5),
        created_at DATETIME DEFAULT GETDATE(),

        CONSTRAINT FK_comments_ratings_cars
            FOREIGN KEY (car_id) REFERENCES Cars(id) ON DELETE CASCADE,
        CONSTRAINT FK_comments_ratings_users
            FOREIGN KEY (user_id) REFERENCES users_secure(id) ON DELETE CASCADE,
        CONSTRAINT CHK_comment_or_rating
            CHECK (comment IS NOT NULL OR rating IS NOT NULL)
    );

    CREATE INDEX IX_comments_ratings_car ON comments_ratings(car_id);
    CREATE INDEX IX_comments_ratings_user ON comments_ratings(user_id);
    CREATE INDEX IX_comments_ratings_created ON comments_ratings(created_at DESC);

    PRINT '   ✅ Таблица comments_ratings создана успешно!';
    PRINT '';
END
ELSE
BEGIN
    PRINT '✅ Таблица comments_ratings уже существует';
    PRINT '';
END

PRINT '';

-- ============================================
-- ШАГ 2: Проверка структуры таблиц
-- ============================================

PRINT '🔍 ШАГ 2: ПРОВЕРКА СТРУКТУРЫ ТАБЛИЦ';
PRINT '--------------------------------------------';

-- Проверка Cars
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Cars')
BEGIN
    PRINT '📊 Таблица Cars:';

    -- Обязательные поля
    IF COL_LENGTH('Cars', 'id') IS NOT NULL PRINT '   ✅ id';
    ELSE PRINT '   ❌ id - ОТСУТСТВУЕТ!';

    IF COL_LENGTH('Cars', 'name') IS NOT NULL PRINT '   ✅ name';
    ELSE PRINT '   ❌ name - ОТСУТСТВУЕТ!';

    IF COL_LENGTH('Cars', 'model') IS NOT NULL PRINT '   ✅ model';
    ELSE PRINT '   ❌ model - ОТСУТСТВУЕТ!';

    IF COL_LENGTH('Cars', 'price') IS NOT NULL PRINT '   ✅ price';
    ELSE PRINT '   ❌ price - ОТСУТСТВУЕТ!';

    -- Дополнительные поля
    IF COL_LENGTH('Cars', 'brand') IS NOT NULL PRINT '   ✅ brand';
    ELSE PRINT '   ⚠️  brand - рекомендуется добавить';

    IF COL_LENGTH('Cars', 'year') IS NOT NULL PRINT '   ✅ year';
    ELSE PRINT '   ⚠️  year - рекомендуется добавить';

    IF COL_LENGTH('Cars', 'mileage') IS NOT NULL PRINT '   ✅ mileage';
    ELSE PRINT '   ⚠️  mileage - рекомендуется добавить';

    IF COL_LENGTH('Cars', 'description') IS NOT NULL PRINT '   ✅ description';
    ELSE PRINT '   ⚠️  description - рекомендуется добавить';

    IF COL_LENGTH('Cars', 'image_url') IS NOT NULL PRINT '   ✅ image_url';
    ELSE PRINT '   ⚠️  image_url - рекомендуется добавить';

    PRINT '';
END

-- Проверка users_secure
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'users_secure')
BEGIN
    PRINT '👥 Таблица users_secure:';

    IF COL_LENGTH('users_secure', 'id') IS NOT NULL PRINT '   ✅ id';
    IF COL_LENGTH('users_secure', 'username') IS NOT NULL PRINT '   ✅ username';
    IF COL_LENGTH('users_secure', 'password') IS NOT NULL PRINT '   ✅ password';
    IF COL_LENGTH('users_secure', 'fullname') IS NOT NULL PRINT '   ✅ fullname';
    IF COL_LENGTH('users_secure', 'email') IS NOT NULL PRINT '   ✅ email';
    IF COL_LENGTH('users_secure', 'role') IS NOT NULL PRINT '   ✅ role';
    IF COL_LENGTH('users_secure', 'is_active') IS NOT NULL PRINT '   ✅ is_active';

    IF COL_LENGTH('users_secure', 'avatar_path') IS NOT NULL PRINT '   ✅ avatar_path';
    ELSE PRINT '   ⚠️  avatar_path - рекомендуется добавить';

    PRINT '';
END

PRINT '';

-- ============================================
-- ШАГ 3: Проверка Foreign Keys
-- ============================================

PRINT '🔗 ШАГ 3: ПРОВЕРКА FOREIGN KEYS';
PRINT '--------------------------------------------';

-- Проверка FK для favorites
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'favorites')
BEGIN
    PRINT '❤️  Таблица favorites:';

    -- Проверяем, на какую таблицу ссылается FK
    IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        WHERE fk.parent_object_id = OBJECT_ID('favorites')
          AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
    )
        PRINT '   ❌ Ссылается на Users_OLD_BACKUP - ТРЕБУЕТСЯ ИСПРАВЛЕНИЕ!';
    ELSE IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        WHERE fk.parent_object_id = OBJECT_ID('favorites')
          AND fk.referenced_object_id = OBJECT_ID('users_secure')
    )
        PRINT '   ✅ Корректно ссылается на users_secure';
    ELSE
        PRINT '   ⚠️  Нет FK на users_secure - рекомендуется добавить';

    PRINT '';
END

-- Проверка FK для comments_ratings
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'comments_ratings')
BEGIN
    PRINT '💬 Таблица comments_ratings:';

    IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        WHERE fk.parent_object_id = OBJECT_ID('comments_ratings')
          AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
    )
        PRINT '   ❌ Ссылается на Users_OLD_BACKUP - ТРЕБУЕТСЯ ИСПРАВЛЕНИЕ!';
    ELSE IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        WHERE fk.parent_object_id = OBJECT_ID('comments_ratings')
          AND fk.referenced_object_id = OBJECT_ID('users_secure')
    )
        PRINT '   ✅ Корректно ссылается на users_secure';
    ELSE
        PRINT '   ⚠️  Нет FK на users_secure';

    PRINT '';
END

-- Проверка FK для reservations
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'reservations')
BEGIN
    PRINT '📅 Таблица reservations:';

    IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        WHERE fk.parent_object_id = OBJECT_ID('reservations')
          AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
    )
        PRINT '   ❌ Ссылается на Users_OLD_BACKUP - ТРЕБУЕТСЯ ИСПРАВЛЕНИЕ!';
    ELSE IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        WHERE fk.parent_object_id = OBJECT_ID('reservations')
          AND fk.referenced_object_id = OBJECT_ID('users_secure')
    )
        PRINT '   ✅ Корректно ссылается на users_secure';
    ELSE
        PRINT '   ⚠️  Нет FK на users_secure';

    PRINT '';
END

-- Проверка FK для purchases
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'purchases')
BEGIN
    PRINT '💰 Таблица purchases:';

    IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        WHERE fk.parent_object_id = OBJECT_ID('purchases')
          AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
    )
        PRINT '   ❌ Ссылается на Users_OLD_BACKUP - ТРЕБУЕТСЯ ИСПРАВЛЕНИЕ!';
    ELSE IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        WHERE fk.parent_object_id = OBJECT_ID('purchases')
          AND fk.referenced_object_id = OBJECT_ID('users_secure')
    )
        PRINT '   ✅ Корректно ссылается на users_secure';
    ELSE
        PRINT '   ⚠️  Нет FK на users_secure';

    PRINT '';
END

PRINT '';

-- ============================================
-- ШАГ 4: АВТОМАТИЧЕСКОЕ ИСПРАВЛЕНИЕ FK
-- ============================================

PRINT '🔧 ШАГ 4: АВТОМАТИЧЕСКОЕ ИСПРАВЛЕНИЕ FOREIGN KEYS';
PRINT '--------------------------------------------';

-- Исправление FK для favorites
IF EXISTS (
    SELECT * FROM sys.foreign_keys fk
    WHERE fk.parent_object_id = OBJECT_ID('favorites')
      AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
)
BEGIN
    DECLARE @fk_name NVARCHAR(255);

    SELECT @fk_name = name
    FROM sys.foreign_keys fk
    WHERE fk.parent_object_id = OBJECT_ID('favorites')
      AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP');

    EXEC('ALTER TABLE favorites DROP CONSTRAINT ' + @fk_name);
    PRINT '✅ Удалён старый FK из favorites: ' + @fk_name;

    ALTER TABLE favorites
    ADD CONSTRAINT FK_favorites_users_secure
    FOREIGN KEY (user_id) REFERENCES users_secure(id)
    ON DELETE CASCADE;
    PRINT '✅ Создан новый FK: favorites → users_secure';
    PRINT '';
END
ELSE
    PRINT '✅ favorites FK уже корректен или не существует';

-- Исправление FK для comments_ratings
IF EXISTS (
    SELECT * FROM sys.foreign_keys fk
    WHERE fk.parent_object_id = OBJECT_ID('comments_ratings')
      AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
)
BEGIN
    SELECT @fk_name = name
    FROM sys.foreign_keys fk
    WHERE fk.parent_object_id = OBJECT_ID('comments_ratings')
      AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP');

    EXEC('ALTER TABLE comments_ratings DROP CONSTRAINT ' + @fk_name);
    PRINT '✅ Удалён старый FK из comments_ratings: ' + @fk_name;

    ALTER TABLE comments_ratings
    ADD CONSTRAINT FK_comments_users_secure
    FOREIGN KEY (user_id) REFERENCES users_secure(id)
    ON DELETE CASCADE;
    PRINT '✅ Создан новый FK: comments_ratings → users_secure';
    PRINT '';
END

-- Исправление FK для reservations
IF EXISTS (
    SELECT * FROM sys.foreign_keys fk
    WHERE fk.parent_object_id = OBJECT_ID('reservations')
      AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
)
BEGIN
    SELECT @fk_name = name
    FROM sys.foreign_keys fk
    WHERE fk.parent_object_id = OBJECT_ID('reservations')
      AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP');

    EXEC('ALTER TABLE reservations DROP CONSTRAINT ' + @fk_name);
    PRINT '✅ Удалён старый FK из reservations: ' + @fk_name;

    ALTER TABLE reservations
    ADD CONSTRAINT FK_reservations_users_secure
    FOREIGN KEY (user_id) REFERENCES users_secure(id)
    ON DELETE CASCADE;
    PRINT '✅ Создан новый FK: reservations → users_secure';
    PRINT '';
END

-- Исправление FK для purchases
IF EXISTS (
    SELECT * FROM sys.foreign_keys fk
    WHERE fk.parent_object_id = OBJECT_ID('purchases')
      AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
)
BEGIN
    SELECT @fk_name = name
    FROM sys.foreign_keys fk
    WHERE fk.parent_object_id = OBJECT_ID('purchases')
      AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP');

    EXEC('ALTER TABLE purchases DROP CONSTRAINT ' + @fk_name);
    PRINT '✅ Удалён старый FK из purchases: ' + @fk_name;

    ALTER TABLE purchases
    ADD CONSTRAINT FK_purchases_users_secure
    FOREIGN KEY (user_id) REFERENCES users_secure(id)
    ON DELETE CASCADE;
    PRINT '✅ Создан новый FK: purchases → users_secure';
    PRINT '';
END

PRINT '';

-- ============================================
-- ШАГ 5: Проверка данных
-- ============================================

PRINT '📊 ШАГ 5: СТАТИСТИКА ДАННЫХ';
PRINT '--------------------------------------------';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Cars')
BEGIN
    DECLARE @cars_count INT;
    SELECT @cars_count = COUNT(*) FROM Cars;
    PRINT '🚗 Автомобилей в базе: ' + CAST(@cars_count AS VARCHAR(10));
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'users_secure')
BEGIN
    DECLARE @users_count INT, @admins_count INT;
    SELECT @users_count = COUNT(*) FROM users_secure WHERE role = 'user';
    SELECT @admins_count = COUNT(*) FROM users_secure WHERE role = 'admin';
    PRINT '👥 Пользователей: ' + CAST(@users_count AS VARCHAR(10));
    PRINT '👨‍💼 Администраторов: ' + CAST(@admins_count AS VARCHAR(10));
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'favorites')
BEGIN
    DECLARE @favorites_count INT;
    SELECT @favorites_count = COUNT(*) FROM favorites;
    PRINT '❤️  Избранных: ' + CAST(@favorites_count AS VARCHAR(10));
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'comments_ratings')
BEGIN
    DECLARE @comments_count INT;
    SELECT @comments_count = COUNT(*) FROM comments_ratings;
    PRINT '💬 Комментариев: ' + CAST(@comments_count AS VARCHAR(10));
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'reservations')
BEGIN
    DECLARE @reservations_count INT;
    SELECT @reservations_count = COUNT(*) FROM reservations;
    PRINT '📅 Бронирований: ' + CAST(@reservations_count AS VARCHAR(10));
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'purchases')
BEGIN
    DECLARE @purchases_count INT;
    SELECT @purchases_count = COUNT(*) FROM purchases;
    PRINT '💰 Покупок: ' + CAST(@purchases_count AS VARCHAR(10));
END

PRINT '';

-- ============================================
-- ШАГ 6: Проверка индексов
-- ============================================

PRINT '🔍 ШАГ 6: ПРОВЕРКА ИНДЕКСОВ';
PRINT '--------------------------------------------';

-- Индексы для Cars
IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Cars_Brand' AND object_id = OBJECT_ID('Cars'))
    PRINT '✅ Индекс IX_Cars_Brand существует';
ELSE
BEGIN
    IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Cars' AND COL_LENGTH('Cars', 'brand') IS NOT NULL)
    BEGIN
        CREATE INDEX IX_Cars_Brand ON Cars(brand);
        PRINT '✅ Создан индекс IX_Cars_Brand';
    END
    ELSE
        PRINT '⚠️  Невозможно создать IX_Cars_Brand - поле brand отсутствует';
END

-- Индексы для users_secure
IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Users_Username' AND object_id = OBJECT_ID('users_secure'))
    PRINT '✅ Индекс IX_Users_Username существует';
ELSE
BEGIN
    IF EXISTS (SELECT * FROM sys.tables WHERE name = 'users_secure')
    BEGIN
        CREATE UNIQUE INDEX IX_Users_Username ON users_secure(username);
        PRINT '✅ Создан уникальный индекс IX_Users_Username';
    END
END

PRINT '';

-- ============================================
-- ШАГ 7: Очистка (опционально)
-- ============================================

PRINT '🧹 ШАГ 7: ОЧИСТКА';
PRINT '--------------------------------------------';

-- Проверка устаревших таблиц
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Users_OLD_BACKUP')
BEGIN
    PRINT '⚠️  Найдена устаревшая таблица Users_OLD_BACKUP';
    PRINT '   Рекомендация: Удалите её вручную если она больше не нужна';
    PRINT '   Команда: DROP TABLE Users_OLD_BACKUP;';
END
ELSE
    PRINT '✅ Устаревших таблиц не найдено';

PRINT '';

-- ============================================
-- ФИНАЛЬНЫЙ ОТЧЁТ
-- ============================================

PRINT '';
PRINT '============================================';
PRINT '✅ ПРОВЕРКА ЗАВЕРШЕНА!';
PRINT '============================================';
PRINT '';
PRINT '📋 РЕКОМЕНДАЦИИ:';
PRINT '1. Если были исправлены FK - перезапустите приложение';
PRINT '2. Проверьте работу всех функций приложения';
PRINT '3. Сделайте резервную копию БД:';
PRINT '   BACKUP DATABASE TestDB TO DISK = ''C:\Backups\CarHub.bak'';';
PRINT '';
PRINT '🎉 База данных готова к работе!';
PRINT '';

