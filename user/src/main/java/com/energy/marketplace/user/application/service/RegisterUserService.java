package com.energy.marketplace.user.application.service;

import com.energy.marketplace.user.application.command.RegisterUserCommand;
import com.energy.marketplace.user.application.port.in.RegisterUserUseCase;
import com.energy.marketplace.user.application.port.out.CheckUserExistsPort;
import com.energy.marketplace.user.application.port.out.PasswordHasherPort;
import com.energy.marketplace.user.application.port.out.SaveUserPort;
import com.energy.marketplace.user.application.result.UserResult;
import com.energy.marketplace.user.domain.exception.UserAlreadyExistsException;
import com.energy.marketplace.user.domain.model.User;
import com.energy.marketplace.user.domain.valueObject.Email;
import com.energy.marketplace.user.domain.valueObject.UserStatus;

public class RegisterUserService implements RegisterUserUseCase {

    private final SaveUserPort saveUserPort;
    private final PasswordHasherPort passwordHasherPort;
    private final CheckUserExistsPort checkUserExistsPort;

    public RegisterUserService(
            SaveUserPort saveUserPort,
            PasswordHasherPort passwordHasherPort,
            CheckUserExistsPort checkUserExistsPort
    ) {
        this.saveUserPort = saveUserPort;
        this.passwordHasherPort = passwordHasherPort;
        this.checkUserExistsPort = checkUserExistsPort;
    }

    @Override
    public UserResult register(RegisterUserCommand command) {
        Email email = new Email(command.email());

        if (checkUserExistsPort.existsByEmail(email)) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + email.value()
            );
        }

        String hashedPassword = passwordHasherPort.hash(command.rawPassword());

        User user = User.registerNew(command.name(), email, hashedPassword, command.role());

        User savedUser = saveUserPort.save(user);

        return UserResult.from(savedUser);
    }
}