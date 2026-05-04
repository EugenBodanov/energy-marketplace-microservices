package com.energy.marketplace.user.application.port.in;

import com.energy.marketplace.user.application.command.GetUserCommand;
import com.energy.marketplace.user.application.result.UserResult;

public interface GetUserUseCase {
    UserResult getUser(GetUserCommand command);
}
