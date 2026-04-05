package com.secure_task.dto;
// 3. AUTH RESPONSE  ←  returned after login/register
public record AuthResponse(
        String token,       // JWT token
        String tokenType,   // "Bearer"
        String email,
        String name,
        String role
) {
    // Static factory for convenience
    public static AuthResponse of(String token, String email,
                                  String name, String role) {
        return new AuthResponse(token, "Bearer", email, name, role);
    }
}
