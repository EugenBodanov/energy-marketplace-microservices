package com.energy.marketplace.user.application.result;

import com.energy.marketplace.user.domain.valueObject.Id;
import com.energy.marketplace.user.domain.valueObject.UserRole;

public record LoginUserResult(
        Id userId,
        String accessToken,
        UserRole role
) {

    public LoginUserResult {
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }

        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be empty");
        }

        if (role == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
    }
}