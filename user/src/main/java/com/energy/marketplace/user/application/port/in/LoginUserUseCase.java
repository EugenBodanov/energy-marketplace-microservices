package com.energy.marketplace.user.application.port.in;

import com.energy.marketplace.user.application.command.LoginUserCommand;
import com.energy.marketplace.user.application.result.LoginUserResult;

public interface LoginUserUseCase {
    LoginUserResult login(LoginUserCommand command);
}
