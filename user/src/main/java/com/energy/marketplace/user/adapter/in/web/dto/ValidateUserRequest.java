package com.energy.marketplace.user.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateUserRequest(
        @NotBlank(message = "User ID cannot be empty")
        Long userId,

        @NotBlank(message = "Purpose cannot be empty")
        String purpose
) {
}
