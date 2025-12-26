-- Миграция: добавление таблицы Favorites для избранных автомобилей
-- Выполните этот скрипт в SSMS, в вашей базе (TestDB)

-- Создаём таблицу Favorites
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Favorites')
BEGIN
    CREATE TABLE Favorites (
        id INT PRIMARY KEY IDENTITY(1,1),
        user_id INT NOT NULL,
        car_id INT NOT NULL,
        created_at DATETIME DEFAULT GETDATE(),
        CONSTRAINT FK_Favorites_Users FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
        CONSTRAINT FK_Favorites_Cars FOREIGN KEY (car_id) REFERENCES Cars(id) ON DELETE CASCADE,
        CONSTRAINT UQ_Favorites_UserCar UNIQUE(user_id, car_id)
    );

    PRINT '✅ Таблица Favorites создана успешно!';
END
ELSE
BEGIN
    PRINT '⚠️ Таблица Favorites уже существует';
END

-- Индексы для быстрого поиска
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Favorites_UserId')
BEGIN
    CREATE INDEX IX_Favorites_UserId ON Favorites(user_id);
    PRINT '✅ Индекс IX_Favorites_UserId создан';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Favorites_CarId')
BEGIN
    CREATE INDEX IX_Favorites_CarId ON Favorites(car_id);
    PRINT '✅ Индекс IX_Favorites_CarId создан';
END

-- Проверка структуры
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Favorites'
ORDER BY ORDINAL_POSITION;

PRINT '✅ Миграция завершена! Теперь можно использовать функцию "Избранное"';

