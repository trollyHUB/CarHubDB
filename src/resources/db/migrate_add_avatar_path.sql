-- ============================================
-- Миграция: Добавление avatar_path в users_secure
-- База данных: TestDB
-- Дата: 2 ноября 2025
-- ============================================

USE TestDB;
GO

PRINT '============================================';
PRINT 'Добавление колонки avatar_path';
PRINT '============================================';

-- Проверяем и добавляем колонку avatar_path
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users_secure') AND name = 'avatar_path')
BEGIN
    ALTER TABLE users_secure ADD avatar_path NVARCHAR(500) NULL;
    PRINT '✅ Колонка avatar_path добавлена';
END
ELSE
BEGIN
    PRINT '✅ Колонка avatar_path уже существует';
END
GO

PRINT '';
PRINT '============================================';
PRINT '✅ Миграция завершена!';
PRINT '============================================';
PRINT '';
PRINT 'Теперь пользователи могут загружать аватары!';
PRINT '';
GO

