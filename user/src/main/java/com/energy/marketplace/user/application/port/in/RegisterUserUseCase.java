package com.energy.marketplace.user.application.port.in;

import com.energy.marketplace.user.application.command.RegisterUserCommand;
import com.energy.marketplace.user.application.result.UserResult;

public interface RegisterUserUseCase {
    UserResult register(RegisterUserCommand command);
}
