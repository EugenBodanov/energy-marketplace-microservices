package com.energy.marketplace.user.application.result;

import com.energy.marketplace.user.domain.model.User;
import com.energy.marketplace.user.domain.valueObject.Email;
import com.energy.marketplace.user.domain.valueObject.Id;
import com.energy.marketplace.user.domain.valueObject.UserRole;
import com.energy.marketplace.user.domain.valueObject.UserStatus;

public record UserResult(
        Id id,
        String name,
        Email email,
        UserRole role,
        UserStatus status
) {

    public UserResult {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }

        if (email == null) {
            throw new IllegalArgumentException("User email cannot be null");
        }

        if (role == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }

        if (status == null) {
            throw new IllegalArgumentException("User status cannot be null");
        }
    }

    public static UserResult from(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        return new UserResult(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }
}