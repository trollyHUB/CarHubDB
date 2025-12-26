-- ============================================
-- СОЗДАНИЕ ТАБЛИЦЫ comments_ratings
-- База данных: TestDB
-- ============================================

USE TestDB;
GO

PRINT '============================================';
PRINT '📝 СОЗДАНИЕ ТАБЛИЦЫ comments_ratings';
PRINT '============================================';
PRINT '';

-- Удаляем старые таблицы если они есть
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Comments')
BEGIN
    DROP TABLE Comments;
    PRINT '✅ Удалена старая таблица Comments';
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Ratings')
BEGIN
    DROP TABLE Ratings;
    PRINT '✅ Удалена старая таблица Ratings';
END

-- Создаём объединённую таблицу comments_ratings
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'comments_ratings')
BEGIN
    CREATE TABLE comments_ratings (
        id INT PRIMARY KEY IDENTITY(1,1),
        car_id INT NOT NULL,
        user_id INT NOT NULL,
        comment NVARCHAR(1000),           -- Комментарий (может быть NULL если только оценка)
        rating INT CHECK (rating BETWEEN 1 AND 5), -- Оценка 1-5 (может быть NULL если только комментарий)
        created_at DATETIME DEFAULT GETDATE(),

        -- Foreign Keys
        CONSTRAINT FK_comments_ratings_cars
            FOREIGN KEY (car_id) REFERENCES Cars(id) ON DELETE CASCADE,
        CONSTRAINT FK_comments_ratings_users
            FOREIGN KEY (user_id) REFERENCES users_secure(id) ON DELETE CASCADE,

        -- Проверка: должен быть хотя бы комментарий или оценка
        CONSTRAINT CHK_comment_or_rating
            CHECK (comment IS NOT NULL OR rating IS NOT NULL)
    );

    PRINT '✅ Создана таблица comments_ratings';
    PRINT '';

    -- Создаём индексы для быстрого поиска
    CREATE INDEX IX_comments_ratings_car ON comments_ratings(car_id);
    PRINT '✅ Создан индекс IX_comments_ratings_car';

    CREATE INDEX IX_comments_ratings_user ON comments_ratings(user_id);
    PRINT '✅ Создан индекс IX_comments_ratings_user';

    CREATE INDEX IX_comments_ratings_created ON comments_ratings(created_at DESC);
    PRINT '✅ Создан индекс IX_comments_ratings_created';

    PRINT '';
    PRINT '🎉 Таблица comments_ratings успешно создана!';
END
ELSE
BEGIN
    PRINT '⚠️  Таблица comments_ratings уже существует';
END

PRINT '';

-- Показываем структуру таблицы
PRINT '📋 СТРУКТУРА ТАБЛИЦЫ comments_ratings:';
PRINT '--------------------------------------------';

SELECT
    COLUMN_NAME AS [Поле],
    DATA_TYPE AS [Тип],
    CHARACTER_MAXIMUM_LENGTH AS [Размер],
    IS_NULLABLE AS [Nullable],
    COLUMN_DEFAULT AS [По умолчанию]
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'comments_ratings'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT '✅ ГОТОВО! Таблица comments_ratings создана и готова к использованию!';
PRINT '';

