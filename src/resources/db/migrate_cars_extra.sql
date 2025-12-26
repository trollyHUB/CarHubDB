-- Миграция: расширение таблицы Cars дополнительными полями
-- Выполните этот скрипт в SSMS, в вашей базе (TestDB)

IF COL_LENGTH('dbo.Cars', 'brand') IS NULL
    ALTER TABLE dbo.Cars ADD brand NVARCHAR(100) NULL;

IF COL_LENGTH('dbo.Cars', 'year') IS NULL
    ALTER TABLE dbo.Cars ADD [year] INT NULL;

IF COL_LENGTH('dbo.Cars', 'mileage') IS NULL
    ALTER TABLE dbo.Cars ADD mileage INT NULL;

IF COL_LENGTH('dbo.Cars', 'description') IS NULL
    ALTER TABLE dbo.Cars ADD [description] NVARCHAR(1000) NULL;

IF COL_LENGTH('dbo.Cars', 'imageUrl') IS NULL AND COL_LENGTH('dbo.Cars', 'image_url') IS NULL
    ALTER TABLE dbo.Cars ADD imageUrl NVARCHAR(500) NULL;

-- Дефолты для новых записей (создаются один раз, если нет)
IF NOT EXISTS (SELECT 1 FROM sys.default_constraints dc
               JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id
               WHERE dc.parent_object_id = OBJECT_ID('dbo.Cars') AND c.name = 'brand')
BEGIN
    ALTER TABLE dbo.Cars ADD CONSTRAINT DF_Cars_brand DEFAULT 'Unknown' FOR brand;
END

IF NOT EXISTS (SELECT 1 FROM sys.default_constraints dc
               JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id
               WHERE dc.parent_object_id = OBJECT_ID('dbo.Cars') AND c.name = 'year')
BEGIN
    ALTER TABLE dbo.Cars ADD CONSTRAINT DF_Cars_year DEFAULT 2000 FOR [year];
END

IF NOT EXISTS (SELECT 1 FROM sys.default_constraints dc
               JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id
               WHERE dc.parent_object_id = OBJECT_ID('dbo.Cars') AND c.name = 'mileage')
BEGIN
    ALTER TABLE dbo.Cars ADD CONSTRAINT DF_Cars_mileage DEFAULT 0 FOR mileage;
END

IF NOT EXISTS (SELECT 1 FROM sys.default_constraints dc
               JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id
               WHERE dc.parent_object_id = OBJECT_ID('dbo.Cars') AND c.name = 'description')
BEGIN
    ALTER TABLE dbo.Cars ADD CONSTRAINT DF_Cars_description DEFAULT 'No description' FOR [description];
END

-- Для фото учитываем возможные имена столбцов
DECLARE @imageCol sysname;
IF COL_LENGTH('dbo.Cars', 'imageUrl') IS NOT NULL SET @imageCol = 'imageUrl';
ELSE IF COL_LENGTH('dbo.Cars', 'image_url') IS NOT NULL SET @imageCol = 'image_url';

IF @imageCol IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM sys.default_constraints dc
    JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id
    WHERE dc.parent_object_id = OBJECT_ID('dbo.Cars') AND c.name = @imageCol)
BEGIN
    DECLARE @sql NVARCHAR(MAX) = N'ALTER TABLE dbo.Cars ADD CONSTRAINT DF_Cars_' + @imageCol + ' DEFAULT ''/images/default.jpg'' FOR ' + QUOTENAME(@imageCol) + ';';
    EXEC sp_executesql @sql;
END

-- Опционально: заполнить NULL в существующих строках безопасными значениями. РАСКОММЕНТИРУЙТЕ, если хотите сразу увидеть данные в приложении.
-- UPDATE dbo.Cars
-- SET brand = ISNULL(brand, 'Unknown'),
--     [year] = ISNULL([year], 2000),
--     mileage = ISNULL(mileage, 0),
--     [description] = ISNULL([description], 'No description'),
--     imageUrl = CASE WHEN COL_LENGTH('dbo.Cars', 'imageUrl') IS NOT NULL THEN ISNULL(imageUrl, '/images/default.jpg') ELSE imageUrl END,
--     image_url = CASE WHEN COL_LENGTH('dbo.Cars', 'image_url') IS NOT NULL THEN ISNULL(image_url, '/images/default.jpg') ELSE image_url END;

-- Пример точечного обновления (при необходимости):
-- UPDATE dbo.Cars SET brand = 'BMW', [year] = 2018, mileage = 45000 WHERE id = 1;
