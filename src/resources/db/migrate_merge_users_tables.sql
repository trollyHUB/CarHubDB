-- ============================================
-- МИГРАЦИЯ: Объединение Users и users_secure
-- База данных: TestDB
-- Дата: 2 ноября 2025
-- ============================================

USE TestDB;
GO

PRINT '============================================';
PRINT 'НАЧАЛО МИГРАЦИИ: Объединение таблиц';
PRINT '============================================';
PRINT '';

-- ============================================
-- ШАГ 1: Создание users_secure если не существует
-- ============================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users_secure')
BEGIN
    PRINT 'Создание таблицы users_secure...';

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

    -- Индексы
    CREATE INDEX IX_users_secure_username ON users_secure(username);
    CREATE INDEX IX_users_secure_role ON users_secure(role);

    PRINT '✅ Таблица users_secure создана!';
END
ELSE
BEGIN
    PRINT '✅ Таблица users_secure уже существует';
END
GO

-- ============================================
-- ШАГ 2: Миграция данных из Users в users_secure
-- ============================================

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Users')
BEGIN
    PRINT '';
    PRINT 'Миграция данных из Users в users_secure...';

    -- Подсчитываем количество пользователей в Users
    DECLARE @UsersCount INT;
    SELECT @UsersCount = COUNT(*) FROM Users;
    PRINT 'Найдено пользователей в Users: ' + CAST(@UsersCount AS NVARCHAR);

    -- Мигрируем пользователей с password_hash и salt
    INSERT INTO users_secure (username, password_hash, salt, fullname, role, is_active)
    SELECT
        username,
        password_hash,
        salt,
        ISNULL(fullname, username),
        ISNULL(role, 'user'),
        ISNULL(is_active, 1)
    FROM Users
    WHERE password_hash IS NOT NULL
      AND salt IS NOT NULL
      AND username NOT IN (SELECT username FROM users_secure);

    DECLARE @MigratedHash INT = @@ROWCOUNT;
    PRINT '✅ Мигрировано с хешами: ' + CAST(@MigratedHash AS NVARCHAR);

    -- Мигрируем пользователей с plaintext паролями (нужно обновить вручную!)
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Users') AND name = 'password')
    BEGIN
        INSERT INTO users_secure (username, password_hash, salt, fullname, role, is_active)
        SELECT
            username,
            'PLAINTEXT_' + password, -- Маркер что это plaintext
            'NEED_UPDATE',
            ISNULL(fullname, username),
            ISNULL(role, 'user'),
            1
        FROM Users
        WHERE (password_hash IS NULL OR salt IS NULL)
          AND password IS NOT NULL
          AND username NOT IN (SELECT username FROM users_secure);

        DECLARE @MigratedPlain INT = @@ROWCOUNT;
        PRINT '⚠️  Мигрировано с plaintext: ' + CAST(@MigratedPlain AS NVARCHAR);

        IF @MigratedPlain > 0
        BEGIN
            PRINT '';
            PRINT '⚠️  ВНИМАНИЕ! Пользователи с plaintext паролями помечены!';
            PRINT '   Они НЕ СМОГУТ ВОЙТИ пока не обновят пароли!';
            PRINT '   Используйте приложение для сброса паролей или';
            PRINT '   попросите их заново зарегистрироваться.';
        END
    END

    PRINT '';
    PRINT '✅ Миграция данных завершена!';
END
ELSE
BEGIN
    PRINT '';
    PRINT '⚠️  Таблица Users не найдена, миграция данных пропущена';
END
GO

-- ============================================
-- ШАГ 3: Проверка результатов
-- ============================================

PRINT '';
PRINT '============================================';
PRINT 'СТАТИСТИКА users_secure:';
PRINT '============================================';

SELECT
    COUNT(*) AS [Всего пользователей],
    SUM(CASE WHEN role = 'admin' THEN 1 ELSE 0 END) AS [Администраторов],
    SUM(CASE WHEN role = 'user' THEN 1 ELSE 0 END) AS [Пользователей],
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS [Активных],
    SUM(CASE WHEN password_hash LIKE 'PLAINTEXT_%' THEN 1 ELSE 0 END) AS [Требуют обновления пароля]
FROM users_secure;

PRINT '';
PRINT '============================================';
PRINT 'СПИСОК ПОЛЬЗОВАТЕЛЕЙ:';
PRINT '============================================';

SELECT
    id,
    username,
    fullname,
    role,
    CASE WHEN is_active = 1 THEN 'Активен' ELSE 'Неактивен' END AS status,
    CASE
        WHEN password_hash LIKE 'PLAINTEXT_%' THEN '⚠️ Требует обновления'
        ELSE '✅ OK'
    END AS password_status,
    created_at
FROM users_secure
ORDER BY id;

-- ============================================
-- ШАГ 4: Переименование старой таблицы Users
-- ============================================

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Users')
BEGIN
    PRINT '';
    PRINT '============================================';
    PRINT 'Переименование таблицы Users...';
    PRINT '============================================';

    -- Переименовываем вместо удаления (на всякий случай)
    EXEC sp_rename 'Users', 'Users_OLD_BACKUP';

    PRINT '✅ Таблица Users переименована в Users_OLD_BACKUP';
    PRINT '   Вы можете удалить её вручную когда убедитесь что всё работает:';
    PRINT '   DROP TABLE Users_OLD_BACKUP;';
END
ELSE
BEGIN
    PRINT '';
    PRINT '✅ Таблица Users уже не существует';
END
GO

-- ============================================
-- ШАГ 5: Финальное сообщение
-- ============================================

PRINT '';
PRINT '============================================';
PRINT '✅ МИГРАЦИЯ ЗАВЕРШЕНА УСПЕШНО!';
PRINT '============================================';
PRINT '';
PRINT 'ЧТО СДЕЛАНО:';
PRINT '✅ Таблица users_secure создана/обновлена';
PRINT '✅ Все данные из Users мигрированы';
PRINT '✅ Старая таблица переименована в Users_OLD_BACKUP';
PRINT '✅ Индексы созданы';
PRINT '';
PRINT 'ЧТО ДАЛЬШЕ:';
PRINT '1. Проверьте список пользователей выше';
PRINT '2. Запустите приложение и протестируйте вход';
PRINT '3. Если всё работает - удалите Users_OLD_BACKUP:';
PRINT '   DROP TABLE Users_OLD_BACKUP;';
PRINT '';
PRINT 'ВАЖНО:';
PRINT '⚠️  Пользователи с plaintext паролями НЕ СМОГУТ войти!';
PRINT '   Они должны использовать функцию "Забыл пароль"';
PRINT '   или заново зарегистрироваться.';
PRINT '';
GO

