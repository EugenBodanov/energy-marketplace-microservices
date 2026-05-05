package com.energy.marketplace.user.adapter.in.web;

import com.energy.marketplace.user.adapter.in.web.dto.UserResponse;
import com.energy.marketplace.user.adapter.in.web.mapper.UserWebMapper;
import com.energy.marketplace.user.application.command.GetUserCommand;
import com.energy.marketplace.user.application.port.in.GetUserUseCase;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.InjectService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final UserWebMapper userWebMapper;

    @GetMapping(value = "/{userId}")
    public UserResponse getUsers(@PathVariable Long userId) {
        return userWebMapper.toResponse(getUserUseCase.getUser(userWebMapper.toGetUserCommand(userId)));
    }

}
