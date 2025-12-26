-- Миграция существующих фото из Cars.imageUrl в таблицу CarImages
-- Дата: 20 ноября 2025

USE TestDB;
GO

PRINT '🔄 Начало миграции фото из Cars в CarImages...';
PRINT '';

-- Вставляем все существующие фото как главные
INSERT INTO CarImages (car_id, image_url, is_main, display_order)
SELECT
    id AS car_id,
    imageUrl AS image_url,
    1 AS is_main,  -- Все существующие фото становятся главными
    0 AS display_order
FROM Cars
WHERE imageUrl IS NOT NULL
  AND imageUrl != ''
  AND NOT EXISTS (
      SELECT 1 FROM CarImages WHERE car_id = Cars.id
  );

DECLARE @count INT = @@ROWCOUNT;

PRINT '✅ Миграция завершена!';
PRINT '📊 Перенесено фото: ' + CAST(@count AS NVARCHAR(10));
PRINT '';
PRINT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
PRINT 'Теперь все фото хранятся в таблице CarImages';
PRINT 'Старое поле Cars.imageUrl можно оставить для обратной совместимости';
PRINT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
GO

