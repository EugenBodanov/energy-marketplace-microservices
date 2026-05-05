package com.energy.marketplace.user.application.port.in;

import com.energy.marketplace.user.application.command.ValidateUserCommand;
import com.energy.marketplace.user.application.result.ValidateUserResult;

public interface ValidateUserUseCase {

    ValidateUserResult validateUser(ValidateUserCommand command);
}