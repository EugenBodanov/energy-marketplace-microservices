package com.energy.marketplace.user.application.result;

import com.energy.marketplace.user.domain.valueObject.Id;
import com.energy.marketplace.user.domain.valueObject.UserRole;
import com.energy.marketplace.user.domain.valueObject.UserStatus;

public record UserValidationResult(
        Id userId,
        boolean valid,
        UserRole role,
        UserStatus status,
        String message
) {

    public UserValidationResult {
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }

        if (role == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }

        if (status == null) {
            throw new IllegalArgumentException("User status cannot be null");
        }

        if (message == null) {
            message = "";
        }
    }

    public static UserValidationResult valid(
            Id userId,
            UserRole role,
            UserStatus status
    ) {
        return new UserValidationResult(
                userId,
                true,
                role,
                status,
                "User is valid"
        );
    }

    public static UserValidationResult invalid(
            Id userId,
            UserRole role,
            UserStatus status,
            String message
    ) {
        return new UserValidationResult(
                userId,
                false,
                role,
                status,
                message
        );
    }
}