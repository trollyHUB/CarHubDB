-- Таблица для комментариев/отзывов к автомобилям
CREATE TABLE Comments (
    id INT PRIMARY KEY IDENTITY(1,1),
    car_id INT NOT NULL,
    user_id INT NOT NULL,
    comment_text NVARCHAR(1000) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (car_id) REFERENCES Cars(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(id)
);

-- Таблица для оценок автомобилей
CREATE TABLE Ratings (
    id INT PRIMARY KEY IDENTITY(1,1),
    car_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (car_id) REFERENCES Cars(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(id),
    UNIQUE (car_id, user_id) -- Один пользователь может поставить только одну оценку авто
);

-- Индексы для быстрого поиска
CREATE INDEX idx_comments_car ON Comments(car_id);
CREATE INDEX idx_comments_user ON Comments(user_id);
CREATE INDEX idx_ratings_car ON Ratings(car_id);
CREATE INDEX idx_ratings_user ON Ratings(user_id);

-- Тестовые данные (опционально)
-- INSERT INTO Comments (car_id, user_id, comment_text) VALUES (1, 2, 'Отличный автомобиль! Очень доволен покупкой.');
-- INSERT INTO Ratings (car_id, user_id, rating) VALUES (1, 2, 5);

