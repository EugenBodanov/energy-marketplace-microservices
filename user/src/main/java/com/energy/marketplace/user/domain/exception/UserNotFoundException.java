package com.energy.marketplace.user.domain.exception;

import com.energy.marketplace.user.domain.valueObject.Id;
import org.jspecify.annotations.NonNull;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message) {
        super(message);
    }
    public UserNotFoundException(@NonNull Id userId) {
        super("User not found with id: " + userId.value());
    }
}
