-- ============================================
-- ПРОВЕРКА И ИСПРАВЛЕНИЕ БАЗЫ ДАННЫХ CarHub
-- База данных: TestDB
-- Дата: 17 ноября 2025
-- ============================================

USE TestDB;
GO

PRINT '============================================';
PRINT 'ПРОВЕРКА БАЗЫ ДАННЫХ CarHub';
PRINT '============================================';
PRINT '';

-- ============================================
-- ШАГ 1: Проверка существования таблиц
-- ============================================

PRINT '1. ПРОВЕРКА ТАБЛИЦ:';
PRINT '--------------------------------------------';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Cars')
    PRINT '✅ Таблица Cars существует'
ELSE
    PRINT '❌ Таблица Cars НЕ найдена!';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'users_secure')
    PRINT '✅ Таблица users_secure существует'
ELSE
    PRINT '❌ Таблица users_secure НЕ найдена!';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'favorites')
    PRINT '✅ Таблица favorites существует'
ELSE
    PRINT '❌ Таблица favorites НЕ найдена!';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'comments_ratings')
    PRINT '✅ Таблица comments_ratings существует'
ELSE
    PRINT '❌ Таблица comments_ratings НЕ найдена!';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'reservations')
    PRINT '✅ Таблица reservations существует'
ELSE
    PRINT '❌ Таблица reservations НЕ найдена!';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'purchases')
    PRINT '✅ Таблица purchases существует'
ELSE
    PRINT '❌ Таблица purchases НЕ найдена!';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Users_OLD_BACKUP')
    PRINT '⚠️  Таблица Users_OLD_BACKUP существует (можно удалить)'
ELSE
    PRINT '✅ Таблица Users_OLD_BACKUP уже удалена';

PRINT '';

-- ============================================
-- ШАГ 2: Проверка Foreign Keys
-- ============================================

PRINT '2. ПРОВЕРКА FOREIGN KEYS:';
PRINT '--------------------------------------------';

-- Проверка FK для favorites
IF EXISTS (
    SELECT * FROM sys.foreign_keys fk
    INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
    WHERE t.name = 'favorites' AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
)
    PRINT '❌ favorites ссылается на Users_OLD_BACKUP - ТРЕБУЕТСЯ ИСПРАВЛЕНИЕ!'
ELSE IF EXISTS (
    SELECT * FROM sys.foreign_keys fk
    INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
    WHERE t.name = 'favorites' AND fk.referenced_object_id = OBJECT_ID('users_secure')
)
    PRINT '✅ favorites корректно ссылается на users_secure'
ELSE
    PRINT '⚠️  favorites не имеет FK на users_secure';

-- Проверка FK для comments_ratings
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'comments_ratings')
BEGIN
    IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
        WHERE t.name = 'comments_ratings' AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
    )
        PRINT '❌ comments_ratings ссылается на Users_OLD_BACKUP - ТРЕБУЕТСЯ ИСПРАВЛЕНИЕ!'
    ELSE IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
        WHERE t.name = 'comments_ratings' AND fk.referenced_object_id = OBJECT_ID('users_secure')
    )
        PRINT '✅ comments_ratings корректно ссылается на users_secure'
    ELSE
        PRINT '⚠️  comments_ratings не имеет FK на users_secure';
END

-- Проверка FK для reservations
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'reservations')
BEGIN
    IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
        WHERE t.name = 'reservations' AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
    )
        PRINT '❌ reservations ссылается на Users_OLD_BACKUP - ТРЕБУЕТСЯ ИСПРАВЛЕНИЕ!'
    ELSE IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
        WHERE t.name = 'reservations' AND fk.referenced_object_id = OBJECT_ID('users_secure')
    )
        PRINT '✅ reservations корректно ссылается на users_secure'
    ELSE
        PRINT '⚠️  reservations не имеет FK на users_secure';
END

-- Проверка FK для purchases
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'purchases')
BEGIN
    IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
        WHERE t.name = 'purchases' AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
    )
        PRINT '❌ purchases ссылается на Users_OLD_BACKUP - ТРЕБУЕТСЯ ИСПРАВЛЕНИЕ!'
    ELSE IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
        WHERE t.name = 'purchases' AND fk.referenced_object_id = OBJECT_ID('users_secure')
    )
        PRINT '✅ purchases корректно ссылается на users_secure'
    ELSE
        PRINT '⚠️  purchases не имеет FK на users_secure';
END

PRINT '';

-- ============================================
-- ШАГ 3: ИСПРАВЛЕНИЕ FOREIGN KEYS (если нужно)
-- ============================================

PRINT '3. ИСПРАВЛЕНИЕ FOREIGN KEYS:';
PRINT '--------------------------------------------';

-- Исправление FK для favorites
IF EXISTS (
    SELECT * FROM sys.foreign_keys fk
    INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
    WHERE t.name = 'favorites' AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
)
BEGIN
    DECLARE @fk_favorites_name NVARCHAR(255);
    SELECT @fk_favorites_name = name
    FROM sys.foreign_keys
    WHERE parent_object_id = OBJECT_ID('favorites')
      AND referenced_object_id = OBJECT_ID('Users_OLD_BACKUP');

    EXEC('ALTER TABLE favorites DROP CONSTRAINT ' + @fk_favorites_name);
    PRINT '✅ Удалён старый FK из favorites';

    ALTER TABLE favorites
    ADD CONSTRAINT FK_favorites_users_secure
    FOREIGN KEY (user_id) REFERENCES users_secure(id)
    ON DELETE CASCADE;
    PRINT '✅ Создан новый FK: favorites → users_secure';
END
ELSE
    PRINT '✅ favorites FK уже корректен';

-- Исправление FK для comments_ratings
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'comments_ratings')
BEGIN
    IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
        WHERE t.name = 'comments_ratings' AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
    )
    BEGIN
        DECLARE @fk_comments_name NVARCHAR(255);
        SELECT @fk_comments_name = name
        FROM sys.foreign_keys
        WHERE parent_object_id = OBJECT_ID('comments_ratings')
          AND referenced_object_id = OBJECT_ID('Users_OLD_BACKUP');

        EXEC('ALTER TABLE comments_ratings DROP CONSTRAINT ' + @fk_comments_name);
        PRINT '✅ Удалён старый FK из comments_ratings';

        ALTER TABLE comments_ratings
        ADD CONSTRAINT FK_comments_users_secure
        FOREIGN KEY (user_id) REFERENCES users_secure(id)
        ON DELETE CASCADE;
        PRINT '✅ Создан новый FK: comments_ratings → users_secure';
    END
    ELSE
        PRINT '✅ comments_ratings FK уже корректен';
END

-- Исправление FK для reservations
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'reservations')
BEGIN
    IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
        WHERE t.name = 'reservations' AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
    )
    BEGIN
        DECLARE @fk_reservations_name NVARCHAR(255);
        SELECT @fk_reservations_name = name
        FROM sys.foreign_keys
        WHERE parent_object_id = OBJECT_ID('reservations')
          AND referenced_object_id = OBJECT_ID('Users_OLD_BACKUP');

        EXEC('ALTER TABLE reservations DROP CONSTRAINT ' + @fk_reservations_name);
        PRINT '✅ Удалён старый FK из reservations';

        ALTER TABLE reservations
        ADD CONSTRAINT FK_reservations_users_secure
        FOREIGN KEY (user_id) REFERENCES users_secure(id)
        ON DELETE CASCADE;
        PRINT '✅ Создан новый FK: reservations → users_secure';
    END
    ELSE
        PRINT '✅ reservations FK уже корректен';
END

-- Исправление FK для purchases
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'purchases')
BEGIN
    IF EXISTS (
        SELECT * FROM sys.foreign_keys fk
        INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
        WHERE t.name = 'purchases' AND fk.referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
    )
    BEGIN
        DECLARE @fk_purchases_name NVARCHAR(255);
        SELECT @fk_purchases_name = name
        FROM sys.foreign_keys
        WHERE parent_object_id = OBJECT_ID('purchases')
          AND referenced_object_id = OBJECT_ID('Users_OLD_BACKUP');

        EXEC('ALTER TABLE purchases DROP CONSTRAINT ' + @fk_purchases_name);
        PRINT '✅ Удалён старый FK из purchases';

        ALTER TABLE purchases
        ADD CONSTRAINT FK_purchases_users_secure
        FOREIGN KEY (user_id) REFERENCES users_secure(id)
        ON DELETE CASCADE;
        PRINT '✅ Создан новый FK: purchases → users_secure';
    END
    ELSE
        PRINT '✅ purchases FK уже корректен';
END

PRINT '';

-- ============================================
-- ШАГ 4: Проверка данных
-- ============================================

PRINT '4. СТАТИСТИКА ДАННЫХ:';
PRINT '--------------------------------------------';

DECLARE @carsCount INT, @usersCount INT, @favCount INT, @commentsCount INT, @resCount INT, @purchCount INT;

SELECT @carsCount = COUNT(*) FROM Cars;
SELECT @usersCount = COUNT(*) FROM users_secure;
SELECT @favCount = COUNT(*) FROM favorites;
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'comments_ratings')
    SELECT @commentsCount = COUNT(*) FROM comments_ratings;
ELSE
    SET @commentsCount = 0;
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'reservations')
    SELECT @resCount = COUNT(*) FROM reservations;
ELSE
    SET @resCount = 0;
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'purchases')
    SELECT @purchCount = COUNT(*) FROM purchases;
ELSE
    SET @purchCount = 0;

PRINT 'Автомобилей: ' + CAST(@carsCount AS VARCHAR(10));
PRINT 'Пользователей: ' + CAST(@usersCount AS VARCHAR(10));
PRINT 'Избранных: ' + CAST(@favCount AS VARCHAR(10));
PRINT 'Отзывов: ' + CAST(@commentsCount AS VARCHAR(10));
PRINT 'Бронирований: ' + CAST(@resCount AS VARCHAR(10));
PRINT 'Покупок: ' + CAST(@purchCount AS VARCHAR(10));

PRINT '';

-- ============================================
-- ШАГ 5: Удаление старой таблицы (опционально)
-- ============================================

PRINT '5. ОЧИСТКА:';
PRINT '--------------------------------------------';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Users_OLD_BACKUP')
BEGIN
    -- Проверяем, есть ли FK на эту таблицу
    IF EXISTS (
        SELECT * FROM sys.foreign_keys
        WHERE referenced_object_id = OBJECT_ID('Users_OLD_BACKUP')
    )
    BEGIN
        PRINT '⚠️  Таблица Users_OLD_BACKUP всё ещё используется в FK!';
        PRINT '   Сначала выполните исправление FK (Шаг 3)';
    END
    ELSE
    BEGIN
        -- DROP TABLE Users_OLD_BACKUP;
        PRINT '✅ Таблица Users_OLD_BACKUP готова к удалению';
        PRINT '   (раскомментируйте DROP TABLE для удаления)';
    END
END
ELSE
    PRINT '✅ Таблица Users_OLD_BACKUP уже удалена';

PRINT '';
PRINT '============================================';
PRINT 'ПРОВЕРКА ЗАВЕРШЕНА';
PRINT '============================================';
GO

