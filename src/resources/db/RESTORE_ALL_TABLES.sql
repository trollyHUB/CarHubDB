-- ============================================
-- ПОЛНОЕ ВОССТАНОВЛЕНИЕ ВСЕХ ТАБЛИЦ БД
-- База данных: TestDB
-- ============================================

USE TestDB;
GO

PRINT '============================================';
PRINT '🔧 ВОССТАНОВЛЕНИЕ ТАБЛИЦ БАЗЫ ДАННЫХ';
PRINT '============================================';
PRINT '';

-- ============================================
-- 1. ТАБЛИЦА comments_ratings
-- ============================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'comments_ratings')
BEGIN
    PRINT '📝 Создаём таблицу comments_ratings...';

    -- Удаляем старые таблицы если есть
    IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Comments')
        DROP TABLE Comments;

    IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Ratings')
        DROP TABLE Ratings;

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

    PRINT '✅ Таблица comments_ratings создана!';
    PRINT '';
END
ELSE
    PRINT '✅ Таблица comments_ratings уже существует';

-- ============================================
-- 2. ТАБЛИЦА reservations
-- ============================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'reservations')
BEGIN
    PRINT '📅 Создаём таблицу reservations...';

    CREATE TABLE reservations (
        id INT PRIMARY KEY IDENTITY(1,1),
        car_id INT NOT NULL,
        user_id INT NOT NULL,
        name NVARCHAR(100) NOT NULL,
        phone VARCHAR(20) NOT NULL,
        email VARCHAR(100) NOT NULL,
        date_from DATE NOT NULL,
        date_to DATE NOT NULL,
        status VARCHAR(20) DEFAULT 'Новая',
        created_at DATETIME DEFAULT GETDATE(),

        CONSTRAINT FK_reservations_cars
            FOREIGN KEY (car_id) REFERENCES Cars(id) ON DELETE CASCADE,
        CONSTRAINT FK_reservations_users
            FOREIGN KEY (user_id) REFERENCES users_secure(id) ON DELETE CASCADE
    );

    CREATE INDEX IX_reservations_car ON reservations(car_id);
    CREATE INDEX IX_reservations_user ON reservations(user_id);
    CREATE INDEX IX_reservations_status ON reservations(status);

    PRINT '✅ Таблица reservations создана!';
    PRINT '';
END
ELSE
    PRINT '✅ Таблица reservations уже существует';

-- ============================================
-- 3. ТАБЛИЦА purchases
-- ============================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'purchases')
BEGIN
    PRINT '💰 Создаём таблицу purchases...';

    CREATE TABLE purchases (
        id INT PRIMARY KEY IDENTITY(1,1),
        car_id INT NOT NULL,
        user_id INT NOT NULL,
        name NVARCHAR(100) NOT NULL,
        phone VARCHAR(20) NOT NULL,
        email VARCHAR(100) NOT NULL,
        address NVARCHAR(200) NOT NULL,
        payment_method VARCHAR(50) NOT NULL,
        status VARCHAR(20) DEFAULT 'Новая',
        created_at DATETIME DEFAULT GETDATE(),

        CONSTRAINT FK_purchases_cars
            FOREIGN KEY (car_id) REFERENCES Cars(id) ON DELETE CASCADE,
        CONSTRAINT FK_purchases_users
            FOREIGN KEY (user_id) REFERENCES users_secure(id) ON DELETE CASCADE
    );

    CREATE INDEX IX_purchases_car ON purchases(car_id);
    CREATE INDEX IX_purchases_user ON purchases(user_id);
    CREATE INDEX IX_purchases_status ON purchases(status);

    PRINT '✅ Таблица purchases создана!';
    PRINT '';
END
ELSE
    PRINT '✅ Таблица purchases уже существует';

-- ============================================
-- 4. ТАБЛИЦА favorites
-- ============================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'favorites')
BEGIN
    PRINT '❤️  Создаём таблицу favorites...';

    CREATE TABLE favorites (
        id INT PRIMARY KEY IDENTITY(1,1),
        user_id INT NOT NULL,
        car_id INT NOT NULL,
        created_at DATETIME DEFAULT GETDATE(),

        CONSTRAINT FK_favorites_users
            FOREIGN KEY (user_id) REFERENCES users_secure(id) ON DELETE CASCADE,
        CONSTRAINT FK_favorites_cars
            FOREIGN KEY (car_id) REFERENCES Cars(id) ON DELETE CASCADE,
        CONSTRAINT UQ_favorites_user_car UNIQUE (user_id, car_id)
    );

    CREATE INDEX IX_favorites_user ON favorites(user_id);
    CREATE INDEX IX_favorites_car ON favorites(car_id);

    PRINT '✅ Таблица favorites создана!';
    PRINT '';
END
ELSE
    PRINT '✅ Таблица favorites уже существует';

PRINT '';
PRINT '============================================';
PRINT '✅ ПРОВЕРКА ЗАВЕРШЕНА!';
PRINT '============================================';
PRINT '';

-- Показываем все таблицы
SELECT
    '✅' AS [Статус],
    name AS [Таблица]
FROM sys.tables
WHERE name IN ('Cars', 'users_secure', 'favorites', 'comments_ratings', 'reservations', 'purchases')
ORDER BY name;

PRINT '';
PRINT '🎉 Все необходимые таблицы проверены и созданы!';

