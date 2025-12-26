-- Таблица для хранения нескольких фото одного автомобиля
-- Дата создания: 20 ноября 2025

USE TestDB;
GO

-- Проверка существования таблицы
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'CarImages')
BEGIN
    CREATE TABLE CarImages (
        id INT PRIMARY KEY IDENTITY(1,1),
        car_id INT NOT NULL,
        image_url NVARCHAR(500) NOT NULL,
        is_main BIT DEFAULT 0,
        display_order INT DEFAULT 0,
        created_at DATETIME DEFAULT GETDATE(),

        CONSTRAINT FK_CarImages_Cars FOREIGN KEY (car_id)
            REFERENCES Cars(id) ON DELETE CASCADE
    );

    PRINT '✅ Таблица CarImages создана успешно!';
END
ELSE
BEGIN
    PRINT '⚠️ Таблица CarImages уже существует!';
END
GO

-- Создание индекса для быстрого поиска фото по car_id
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_CarImages_CarId')
BEGIN
    CREATE INDEX IX_CarImages_CarId ON CarImages(car_id);
    PRINT '✅ Индекс IX_CarImages_CarId создан!';
END
GO

-- Создание индекса для главного фото
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_CarImages_IsMain')
BEGIN
    CREATE INDEX IX_CarImages_IsMain ON CarImages(car_id, is_main);
    PRINT '✅ Индекс IX_CarImages_IsMain создан!';
END
GO

PRINT '';
PRINT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
PRINT '✅ Скрипт выполнен успешно!';
PRINT '📊 Таблица CarImages готова к использованию';
PRINT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';

