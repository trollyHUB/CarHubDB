package models;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private int carId;
    private int userId;
    private String userName;
    private String commentText;
    private LocalDateTime createdAt;

    public Comment(int id, int carId, int userId, String userName, String commentText, LocalDateTime createdAt) {
        this.id = id;
        this.carId = carId;
        this.userId = userId;
        this.userName = userName;
        this.commentText = commentText;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getCarId() { return carId; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getCommentText() { return commentText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

