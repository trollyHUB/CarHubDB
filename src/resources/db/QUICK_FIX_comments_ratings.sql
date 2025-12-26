-- ============================================
-- БЫСТРОЕ СОЗДАНИЕ ТАБЛИЦЫ comments_ratings
-- Запустите этот скрипт ПРЯМО СЕЙЧАС!
-- ============================================

USE TestDB;
GO

-- Удаляем старые таблицы
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Comments')
    DROP TABLE Comments;

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Ratings')
    DROP TABLE Ratings;

-- Создаём comments_ratings
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'comments_ratings')
BEGIN
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

    PRINT '✅ Таблица comments_ratings создана!';
END

SELECT '✅ ГОТОВО!' AS [Статус];

