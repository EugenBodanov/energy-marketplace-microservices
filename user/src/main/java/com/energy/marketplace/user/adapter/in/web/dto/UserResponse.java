package com.energy.marketplace.user.adapter.in.web.dto;

public record UserResponse(
        Long id,
        String name,
        String email,
        String role,
        String status
) {
}
