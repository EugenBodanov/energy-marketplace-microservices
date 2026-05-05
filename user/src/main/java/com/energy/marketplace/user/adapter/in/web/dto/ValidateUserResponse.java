package com.energy.marketplace.user.adapter.in.web.dto;

public record ValidateUserResponse(
        Long userId,
        boolean valid,
        String role,
        String status,
        String message
) {
}
