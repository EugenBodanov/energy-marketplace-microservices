package com.energy.marketplace.user.adapter.in.web;

import com.energy.marketplace.user.adapter.in.web.dto.*;
import com.energy.marketplace.user.adapter.in.web.mapper.UserWebMapper;
import com.energy.marketplace.user.application.port.in.GetUserUseCase;
import com.energy.marketplace.user.application.port.in.LoginUserUseCase;
import com.energy.marketplace.user.application.port.in.RegisterUserUseCase;
import com.energy.marketplace.user.application.port.in.ValidateUserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ValidateUserUseCase validateUserUseCase;
    private final UserWebMapper userWebMapper;

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        var command = userWebMapper.toCommand(request);
        var result = registerUserUseCase.register(command);

        return userWebMapper.toResponse(result);
    }

    @PostMapping("/login")
    public LoginUserResponse login(@Valid @RequestBody LoginUserRequest request) {
        var command = userWebMapper.toCommand(request);
        var result = loginUserUseCase.login(command);

        return userWebMapper.toResponse(result);
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable("userId") Long userId) {
        var command = userWebMapper.toGetUserCommand(userId);
        var result = getUserUseCase.getUser(command);

        return userWebMapper.toResponse(result);
    }

    @GetMapping("/{userId}/validate")
    public ValidateUserResponse validateUser(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "purpose", defaultValue = "PARTICIPATE_IN_TRADE") String purpose
    ) {
        var command = userWebMapper.toValidateUserCommand(userId, purpose);
        var result = validateUserUseCase.validateUser(command);

        return userWebMapper.toResponse(result);
    }
}
