package com.secure_task.dto;

public record ApiError(
        int status,
        String message,
        String timestamp
) {
}
