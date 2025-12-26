-- ============================================
-- ИСПРАВЛЕНИЕ: Foreign Key для favorites
-- Проблема: FK ссылается на Users_OLD_BACKUP
-- Решение: Пересоздать FK на users_secure
-- База данных: TestDB
-- Дата: 2 ноября 2025
-- ============================================

USE TestDB;
GO

PRINT '============================================';
PRINT 'ИСПРАВЛЕНИЕ FOREIGN KEY для favorites';
PRINT '============================================';
PRINT '';

-- ============================================
-- ШАГ 1: Удаляем старый FK (если существует)
-- ============================================

IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_Favorites_Users')
BEGIN
    ALTER TABLE favorites DROP CONSTRAINT FK_Favorites_Users;
    PRINT '✅ Старый FK_Favorites_Users удалён';
END
ELSE
BEGIN
    PRINT '⚠️  FK_Favorites_Users не найден';
END
GO

IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_favorites_users')
BEGIN
    ALTER TABLE favorites DROP CONSTRAINT FK_favorites_users;
    PRINT '✅ Старый FK_favorites_users удалён';
END
GO

-- ============================================
-- ШАГ 2: Создаём новый FK на users_secure
-- ============================================

ALTER TABLE favorites
ADD CONSTRAINT FK_favorites_users_secure
FOREIGN KEY (user_id) REFERENCES users_secure(id)
ON DELETE CASCADE;

PRINT '✅ Новый FK создан: FK_favorites_users_secure → users_secure(id)';
PRINT '';

-- ============================================
-- ШАГ 3: Проверяем другие таблицы
-- ============================================

PRINT '============================================';
PRINT 'ПРОВЕРКА ДРУГИХ FOREIGN KEYS:';
PRINT '============================================';

-- Таблица comments_ratings
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'comments_ratings')
BEGIN
    -- Удаляем старые FK если есть
    DECLARE @fk_name NVARCHAR(255);

    SELECT @fk_name = name
    FROM sys.foreign_keys
    WHERE parent_object_id = OBJECT_ID('comments_ratings')
      AND referenced_object_id = OBJECT_ID('Users_OLD_BACKUP');

    IF @fk_name IS NOT NULL
    BEGIN
        EXEC('ALTER TABLE comments_ratings DROP CONSTRAINT ' + @fk_name);
        PRINT '✅ Удалён FK из comments_ratings на Users_OLD_BACKUP';

        -- Создаём новый FK
        ALTER TABLE comments_ratings
        ADD CONSTRAINT FK_comments_users_secure
        FOREIGN KEY (user_id) REFERENCES users_secure(id)
        ON DELETE CASCADE;

        PRINT '✅ Создан FK: comments_ratings → users_secure';
    END
END
GO

-- Таблица reservations
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'reservations')
BEGIN
    DECLARE @fk_name NVARCHAR(255);

    SELECT @fk_name = name
    FROM sys.foreign_keys
    WHERE parent_object_id = OBJECT_ID('reservations')
      AND referenced_object_id = OBJECT_ID('Users_OLD_BACKUP');

    IF @fk_name IS NOT NULL
    BEGIN
        EXEC('ALTER TABLE reservations DROP CONSTRAINT ' + @fk_name);
        PRINT '✅ Удалён FK из reservations на Users_OLD_BACKUP';

        ALTER TABLE reservations
        ADD CONSTRAINT FK_reservations_users_secure
        FOREIGN KEY (user_id) REFERENCES users_secure(id)
        ON DELETE CASCADE;

        PRINT '✅ Создан FK: reservations → users_secure';
    END
END
GO

-- Таблица purchases
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'purchases')
BEGIN
    DECLARE @fk_name NVARCHAR(255);

    SELECT @fk_name = name
    FROM sys.foreign_keys
    WHERE parent_object_id = OBJECT_ID('purchases')
      AND referenced_object_id = OBJECT_ID('Users_OLD_BACKUP');

    IF @fk_name IS NOT NULL
    BEGIN
        EXEC('ALTER TABLE purchases DROP CONSTRAINT ' + @fk_name);
        PRINT '✅ Удалён FK из purchases на Users_OLD_BACKUP';

        ALTER TABLE purchases
        ADD CONSTRAINT FK_purchases_users_secure
        FOREIGN KEY (user_id) REFERENCES users_secure(id)
        ON DELETE CASCADE;

        PRINT '✅ Создан FK: purchases → users_secure';
    END
END
GO

-- ============================================
-- ШАГ 4: Проверяем результат
-- ============================================

PRINT '';
PRINT '============================================';
PRINT 'СПИСОК ВСЕХ FOREIGN KEYS:';
PRINT '============================================';

SELECT
    fk.name AS [Foreign Key],
    OBJECT_NAME(fk.parent_object_id) AS [From Table],
    OBJECT_NAME(fk.referenced_object_id) AS [To Table],
    COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS [From Column],
    COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS [To Column]
FROM sys.foreign_keys AS fk
INNER JOIN sys.foreign_key_columns AS fkc
    ON fk.object_id = fkc.constraint_object_id
WHERE OBJECT_NAME(fk.referenced_object_id) IN ('users_secure', 'Users_OLD_BACKUP')
ORDER BY [From Table], [To Table];

PRINT '';
PRINT '============================================';
PRINT '✅ МИГРАЦИЯ ЗАВЕРШЕНА!';
PRINT '============================================';
PRINT '';
PRINT 'ЧТО СДЕЛАНО:';
PRINT '✅ FK favorites → users_secure';
PRINT '✅ FK comments_ratings → users_secure (если была)';
PRINT '✅ FK reservations → users_secure (если была)';
PRINT '✅ FK purchases → users_secure (если была)';
PRINT '';
PRINT 'ТЕПЕРЬ:';
PRINT '✅ Обычные пользователи могут добавлять в избранное!';
PRINT '✅ Все FK ссылаются на users_secure!';
PRINT '';
GO

