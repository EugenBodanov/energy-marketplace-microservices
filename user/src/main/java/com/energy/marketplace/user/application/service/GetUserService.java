package com.energy.marketplace.user.application.service;

import com.energy.marketplace.user.application.command.GetUserCommand;
import com.energy.marketplace.user.application.port.in.GetUserUseCase;
import com.energy.marketplace.user.application.port.out.LoadUserPort;
import com.energy.marketplace.user.application.result.UserResult;
import com.energy.marketplace.user.domain.exception.UserNotFoundException;

public class GetUserService implements GetUserUseCase {

    private final LoadUserPort loadUserPort;

    public GetUserService(LoadUserPort loadUserPort) {
        this.loadUserPort = loadUserPort;
    }

    @Override
    public UserResult getUser(GetUserCommand command) {
        return UserResult.from(loadUserPort.loadById(command.userId()).orElseThrow(() -> new UserNotFoundException(command.userId())));
    }
}
