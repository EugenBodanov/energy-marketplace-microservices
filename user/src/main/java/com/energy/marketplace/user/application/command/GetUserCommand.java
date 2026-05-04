package com.energy.marketplace.user.application.command;

import com.energy.marketplace.user.domain.valueObject.Id;

public record GetUserCommand(Id userId) {

    public GetUserCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
    }
}