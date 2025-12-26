package utils;

public class SessionManager {
    private static int currentUserId = -1;
    private static String currentUsername = null;
    private static String currentRole = null;

    public static void login(int userId, String username, String role) {
        currentUserId = userId;
        currentUsername = username;
        currentRole = role;
    }

    public static void logout() {
        currentUserId = -1;
        currentUsername = null;
        currentRole = null;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setUsername(String username) {
        currentUsername = username;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static boolean isLoggedIn() {
        return currentUserId > 0;
    }

    public static boolean isAdmin() {
        return "admin".equalsIgnoreCase(currentRole);
    }

    public static boolean isUser() {
        return "user".equalsIgnoreCase(currentRole);
    }
}

