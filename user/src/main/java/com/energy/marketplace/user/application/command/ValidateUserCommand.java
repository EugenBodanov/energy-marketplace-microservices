package com.energy.marketplace.user.application.command;

import com.energy.marketplace.user.domain.valueObject.Id;

public record ValidateUserCommand(
        Id userId,
        UserValidationPurpose purpose
) {

    public ValidateUserCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }

        if (purpose == null) {
            throw new IllegalArgumentException("Validation purpose cannot be null");
        }
    }
}