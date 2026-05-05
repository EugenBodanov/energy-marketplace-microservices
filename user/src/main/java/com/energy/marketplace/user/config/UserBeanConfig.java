package com.energy.marketplace.user.config;

import com.energy.marketplace.user.application.port.in.GetUserUseCase;
import com.energy.marketplace.user.application.port.in.LoginUserUseCase;
import com.energy.marketplace.user.application.port.in.RegisterUserUseCase;
import com.energy.marketplace.user.application.port.in.ValidateUserUseCase;
import com.energy.marketplace.user.application.port.out.*;
import com.energy.marketplace.user.application.service.GetUserService;
import com.energy.marketplace.user.application.service.LoginUserService;
import com.energy.marketplace.user.application.service.RegisterUserService;
import com.energy.marketplace.user.application.service.ValidateUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserBeanConfig {

    @Bean
    public GetUserUseCase getUserUseCase(LoadUserPort loadUserPort) {
        return new GetUserService(loadUserPort);
    }

    @Bean
    public LoginUserUseCase saveUserUseCase(LoadUserPort loadUserPort, PasswordHasherPort passwordHasherPort, TokenIssuerPort tokenIssuerPort) {
        return new LoginUserService(loadUserPort, passwordHasherPort, tokenIssuerPort);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(SaveUserPort saveUserPort, PasswordHasherPort passwordHasherPort, CheckUserExistsPort checkUserExistsPort) {
        return new RegisterUserService(saveUserPort, passwordHasherPort, checkUserExistsPort);
    }

    @Bean
    ValidateUserUseCase validateUserUseCase(LoadUserPort loadUserPort) {
        return new ValidateUserService(loadUserPort);
    }

}
