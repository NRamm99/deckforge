package dk.deckforge.app.application.dto;

public class AuthResponse {

    private Long userId;
    private String email;

    public AuthResponse(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}