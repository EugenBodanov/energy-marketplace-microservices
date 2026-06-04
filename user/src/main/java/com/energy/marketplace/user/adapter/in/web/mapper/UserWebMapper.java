package com.energy.marketplace.user.adapter.in.web.mapper;

import com.energy.marketplace.user.adapter.in.web.dto.*;
import com.energy.marketplace.user.application.command.GetUserCommand;
import com.energy.marketplace.user.application.command.LoginUserCommand;
import com.energy.marketplace.user.application.command.RegisterUserCommand;
import com.energy.marketplace.user.application.command.UserValidationPurpose;
import com.energy.marketplace.user.application.command.ValidateUserCommand;
import com.energy.marketplace.user.application.result.LoginUserResult;
import com.energy.marketplace.user.application.result.UserResult;
import com.energy.marketplace.user.application.result.ValidateUserResult;
import com.energy.marketplace.user.domain.valueObject.Id;
import com.energy.marketplace.user.domain.valueObject.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {

    public RegisterUserCommand toCommand(RegisterUserRequest request) {
        return new RegisterUserCommand(
                request.name(),
                request.email(),
                request.rawPassword(),
                parseUserRole(request.role(), false)
        );
    }

    public LoginUserCommand toCommand(LoginUserRequest request) {
        return new LoginUserCommand(
                request.email(),
                request.rawPassword()
        );
    }

    public GetUserCommand toGetUserCommand(Long userId) {
        return new GetUserCommand(new Id(userId));
    }

    public ValidateUserCommand toValidateUserCommand(Long userId, String purpose) {
        return new ValidateUserCommand(
                new Id(userId),
                parseValidationPurpose(purpose)
        );
    }

    public UserResponse toResponse(UserResult result) {
        return new UserResponse(
                result.id().value(),
                result.name(),
                result.email().value(),
                result.role().name(),
                result.status().name()
        );
    }

    public LoginUserResponse toResponse(LoginUserResult result) {
        return new LoginUserResponse(
                result.userId().value(),
                result.accessToken(),
                result.role().name()
        );
    }

    public ValidateUserResponse toResponse(ValidateUserResult result) {
        return new ValidateUserResponse(
                result.userId().value(),
                result.valid(),
                result.role().name(),
                result.status().name(),
                result.message()
        );
    }

    private UserRole parseUserRole(String role, boolean isAdminRoleAllowed) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("User role cannot be empty");
        }

        UserRole userRole = UserRole.valueOf(role.trim().toUpperCase());

        if ((userRole == UserRole.ADMIN && !isAdminRoleAllowed) || userRole == UserRole.UNKNOWN) {
            throw new IllegalArgumentException(userRole.name() + " role cannot be selected during registration");
        }

        return userRole;
    }

    private UserValidationPurpose parseValidationPurpose(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("Validation purpose cannot be empty");
        }

        return UserValidationPurpose.valueOf(purpose.trim().toUpperCase());
    }
}
