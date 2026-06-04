package com.energy.marketplace.user.application.result;

import com.energy.marketplace.user.domain.valueObject.Id;
import com.energy.marketplace.user.domain.valueObject.UserRole;
import com.energy.marketplace.user.domain.valueObject.UserStatus;

public record ValidateUserResult(
        Id userId,
        boolean valid,
        UserRole role,
        UserStatus status,
        String message
) {

    public ValidateUserResult {
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

    public static ValidateUserResult valid(
            Id userId,
            UserRole role,
            UserStatus status
    ) {
        return new ValidateUserResult(
                userId,
                true,
                role,
                status,
                "User is valid"
        );
    }

    public static ValidateUserResult invalid(
            Id userId,
            UserRole role,
            UserStatus status,
            String message
    ) {
        return new ValidateUserResult(
                userId,
                false,
                role,
                status,
                message
        );
    }

    public static ValidateUserResult invalid(
            Id userId,
            String message
    ) {
        return new ValidateUserResult(
                userId,
                false,
                UserRole.UNKNOWN,
                UserStatus.UNKNOWN,
                message
        );
    }
}