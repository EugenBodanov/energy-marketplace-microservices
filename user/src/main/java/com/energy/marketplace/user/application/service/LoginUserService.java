package com.energy.marketplace.user.application.service;

import com.energy.marketplace.user.application.command.LoginUserCommand;
import com.energy.marketplace.user.application.port.in.LoginUserUseCase;
import com.energy.marketplace.user.application.port.out.LoadUserPort;
import com.energy.marketplace.user.application.port.out.PasswordHasherPort;
import com.energy.marketplace.user.application.port.out.TokenIssuerPort;
import com.energy.marketplace.user.application.result.LoginUserResult;
import com.energy.marketplace.user.domain.exception.InvalidCredentialsException;
import com.energy.marketplace.user.domain.model.User;
import com.energy.marketplace.user.domain.valueObject.Email;

public class LoginUserService implements LoginUserUseCase {

    private final LoadUserPort loadUserPort;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenIssuerPort tokenIssuerPort;

    public LoginUserService(
            LoadUserPort loadUserPort,
            PasswordHasherPort passwordHasherPort,
            TokenIssuerPort tokenIssuerPort
    ) {
        this.loadUserPort = loadUserPort;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenIssuerPort = tokenIssuerPort;
    }

    @Override
    public LoginUserResult login(LoginUserCommand command) {
        Email email = new Email(command.email());

        User user = loadUserPort.loadByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordHasherPort.matches(command.rawPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = tokenIssuerPort.issueToken(user);

        return new LoginUserResult(
                user.getId(),
                token,
                user.getRole()
        );
    }
}