package com.energy.marketplace.user.application.service;

import com.energy.marketplace.user.application.command.ValidateUserCommand;
import com.energy.marketplace.user.application.command.UserValidationPurpose;
import com.energy.marketplace.user.application.port.in.ValidateUserUseCase;
import com.energy.marketplace.user.application.port.out.LoadUserPort;
import com.energy.marketplace.user.application.result.ValidateUserResult;
import com.energy.marketplace.user.domain.exception.UserNotFoundException;
import com.energy.marketplace.user.domain.model.User;

public class ValidateUserService implements ValidateUserUseCase {

    private final LoadUserPort loadUserPort;

    public ValidateUserService(LoadUserPort loadUserPort) {
        this.loadUserPort = loadUserPort;
    }

    @Override
    public ValidateUserResult validateUser(ValidateUserCommand command) {
        User user = loadUserPort.loadById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        boolean valid = switch (command.purpose()) {
            case PARTICIPATE_IN_TRADE -> user.isActive();
            case BUY_ENERGY -> user.canBuyEnergy();
            case SELL_ENERGY -> user.canSellEnergy();
        };

        if (valid) {
            return ValidateUserResult.valid(
                    user.getId(),
                    user.getRole(),
                    user.getStatus()
            );
        }

        return ValidateUserResult.invalid(
                user.getId(),
                user.getRole(),
                user.getStatus(),
                invalidReason(command.purpose())
        );
    }

    private String invalidReason(UserValidationPurpose purpose) {
        return switch (purpose) {
            case PARTICIPATE_IN_TRADE -> "User cannot participate in trade";
            case BUY_ENERGY -> "User cannot buy energy";
            case SELL_ENERGY -> "User cannot sell energy";
        };
    }
}