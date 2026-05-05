package com.energy.marketplace.user.adapter.in.web.dto;

public record LoginUserResponse(
        Long userId,
        String accessToken,
        String role
) {
}
