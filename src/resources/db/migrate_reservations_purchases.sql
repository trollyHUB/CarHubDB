-- ========================================
-- СИСТЕМА БРОНИРОВАНИЯ И ПОКУПОК
-- ========================================

-- 1. Добавляем статус к автомобилям
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Cars') AND name = 'status')
BEGIN
    ALTER TABLE Cars ADD status NVARCHAR(20) DEFAULT 'available';
END
GO

-- Обновляем существующие авто
UPDATE Cars SET status = 'available' WHERE status IS NULL;
GO

-- 2. Таблица бронирований
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Reservations')
BEGIN
    CREATE TABLE Reservations (
        id INT PRIMARY KEY IDENTITY(1,1),
        car_id INT NOT NULL,
        user_id INT NOT NULL,
        customer_name NVARCHAR(100) NOT NULL,
        phone NVARCHAR(20) NOT NULL,
        email NVARCHAR(100),
        reservation_date DATETIME NOT NULL,
        status NVARCHAR(20) DEFAULT 'pending', -- pending, approved, completed, cancelled
        notes NVARCHAR(500),
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (car_id) REFERENCES Cars(id) ON DELETE CASCADE,
        FOREIGN KEY (user_id) REFERENCES Users(id)
    );
END
GO

-- 3. Таблица покупок
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Purchases')
BEGIN
    CREATE TABLE Purchases (
        id INT PRIMARY KEY IDENTITY(1,1),
        car_id INT NOT NULL,
        user_id INT NOT NULL,
        customer_name NVARCHAR(100) NOT NULL,
        phone NVARCHAR(20) NOT NULL,
        email NVARCHAR(100),
        price DECIMAL(18,2) NOT NULL,
        payment_method NVARCHAR(50), -- cash, card, bank_transfer, credit
        status NVARCHAR(20) DEFAULT 'pending', -- pending, paid, completed, cancelled
        notes NVARCHAR(500),
        purchase_date DATETIME DEFAULT GETDATE(),
        completed_at DATETIME,
        FOREIGN KEY (car_id) REFERENCES Cars(id),
        FOREIGN KEY (user_id) REFERENCES Users(id)
    );
END
GO

-- Индексы для быстрого поиска
CREATE INDEX idx_reservations_car ON Reservations(car_id);
CREATE INDEX idx_reservations_user ON Reservations(user_id);
CREATE INDEX idx_reservations_status ON Reservations(status);
CREATE INDEX idx_purchases_car ON Purchases(car_id);
CREATE INDEX idx_purchases_user ON Purchases(user_id);
CREATE INDEX idx_purchases_status ON Purchases(status);
GO

-- Тестовые данные (опционально)
-- INSERT INTO Reservations (car_id, user_id, customer_name, phone, email, reservation_date, status)
-- VALUES (1, 2, 'Иван Петров', '+7 999 123-45-67', 'ivan@example.com', DATEADD(day, 3, GETDATE()), 'pending');
GO

PRINT '✅ Таблицы Reservations и Purchases созданы успешно!';
GO

