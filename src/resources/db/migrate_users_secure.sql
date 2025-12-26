-- ============================================
-- Миграция: Создание таблицы users_secure
-- База данных: TestDB (или ваша база данных)
-- Дата: 2 ноября 2025
-- ============================================

USE TestDB;
GO

-- ============================================
-- 1. Создание таблицы users_secure
-- ============================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users_secure')
BEGIN
    CREATE TABLE users_secure (
        id INT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL UNIQUE,
        password_hash NVARCHAR(128) NOT NULL,
        salt NVARCHAR(32) NOT NULL,
        fullname NVARCHAR(100) NOT NULL,
        role NVARCHAR(20) NOT NULL DEFAULT 'user',
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME NULL,
        CONSTRAINT CK_users_secure_role CHECK (role IN ('user', 'admin'))
    );

    PRINT 'Таблица users_secure создана успешно!';
END
ELSE
BEGIN
    PRINT 'Таблица users_secure уже существует.';
END
GO

-- ============================================
-- 2. Миграция данных из старой таблицы Users (если существует)
-- ============================================

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Users')
BEGIN
    -- Копируем пользователей, у которых есть password_hash и salt
    INSERT INTO users_secure (username, password_hash, salt, fullname, role, is_active)
    SELECT
        username,
        password_hash,
        salt,
        ISNULL(fullname, username), -- Если нет fullname, используем username
        ISNULL(role, 'user'),
        1 -- По умолчанию активен
    FROM Users
    WHERE password_hash IS NOT NULL
      AND salt IS NOT NULL
      AND username NOT IN (SELECT username FROM users_secure);

    PRINT 'Данные из таблицы Users мигрированы в users_secure.';
END
GO

-- ============================================
-- 3. Создание тестовых пользователей
-- ============================================

-- Проверяем, есть ли уже admin
IF NOT EXISTS (SELECT * FROM users_secure WHERE username = 'admin')
BEGIN
    -- Для пароля "admin" с солью "a1b2c3d4e5f6g7h8"
    -- password_hash = SHA256("admin" + "a1b2c3d4e5f6g7h8")
    INSERT INTO users_secure (username, password_hash, salt, fullname, role, is_active)
    VALUES (
        'admin',
        'e02a1a8a8c6e8c0b3f5f8e8f9c8d8e8f8g8h8i8j8k8l8m8n8o8p8q8r8s8t8u8v8w8x8y8z', -- Замените на реальный хеш
        'a1b2c3d4e5f6g7h8',
        'Администратор',
        'admin',
        1
    );

    PRINT 'Тестовый администратор создан (username: admin).';
    PRINT 'ВНИМАНИЕ: Установите правильный password_hash в записи!';
END
ELSE
BEGIN
    PRINT 'Администратор admin уже существует.';
END
GO

-- Проверяем, есть ли уже user
IF NOT EXISTS (SELECT * FROM users_secure WHERE username = 'user')
BEGIN
    INSERT INTO users_secure (username, password_hash, salt, fullname, role, is_active)
    VALUES (
        'user',
        'f03b2b9b9d7f9d1c4g6g9f9g0d9e9f9g9h9i9j9k9l9m9n9o9p9q9r9s9t9u9v9w9x9y9z', -- Замените на реальный хеш
        'b2c3d4e5f6g7h8i9',
        'Пользователь',
        'user',
        1
    );

    PRINT 'Тестовый пользователь создан (username: user).';
    PRINT 'ВНИМАНИЕ: Установите пр��вильный password_hash в записи!';
END
ELSE
BEGIN
    PRINT 'Пользователь user уже существует.';
END
GO

-- ============================================
-- 4. Создание индексов для производительности
-- ============================================

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_users_secure_username')
BEGIN
    CREATE INDEX IX_users_secure_username ON users_secure(username);
    PRINT 'Индекс IX_users_secure_username создан.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_users_secure_role')
BEGIN
    CREATE INDEX IX_users_secure_role ON users_secure(role);
    PRINT 'Индекс IX_users_secure_role создан.';
END
GO

-- ============================================
-- 5. Проверка результата
-- ============================================

SELECT
    COUNT(*) AS TotalUsers,
    SUM(CASE WHEN role = 'admin' THEN 1 ELSE 0 END) AS Admins,
    SUM(CASE WHEN role = 'user' THEN 1 ELSE 0 END) AS Users,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS ActiveUsers
FROM users_secure;

PRINT '';
PRINT '============================================';
PRINT 'Миграция завершена!';
PRINT '============================================';
PRINT '';
PRINT 'ВАЖНО: Пароли для тестовых пользователей:';
PRINT '- Используйте приложение для регистрации новых пользователей';
PRINT '- Или обновите password_hash вручную через приложение';
PRINT '';
GO

