package com.energy.marketplace.user.application.service;

import com.energy.marketplace.user.application.command.GetUserCommand;
import com.energy.marketplace.user.application.port.out.LoadUserPort;
import com.energy.marketplace.user.domain.exception.UserNotFoundException;
import com.energy.marketplace.user.domain.model.User;
import com.energy.marketplace.user.domain.valueObject.Email;
import com.energy.marketplace.user.domain.valueObject.Id;
import com.energy.marketplace.user.domain.valueObject.UserRole;
import com.energy.marketplace.user.domain.valueObject.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserServiceTest {

    @Mock
    private LoadUserPort loadUserPort;

    @Test
    void returnsExistingUser() {
        GetUserService service = new GetUserService(loadUserPort);
        User user = user();

        when(loadUserPort.loadById(new Id(1L))).thenReturn(Optional.of(user));

        var result = service.getUser(new GetUserCommand(new Id(1L)));

        assertThat(result.id()).isEqualTo(new Id(1L));
        assertThat(result.name()).isEqualTo("Alice");
        assertThat(result.email()).isEqualTo(new Email("alice@example.com"));
        assertThat(result.role()).isEqualTo(UserRole.CONSUMER);
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        GetUserService service = new GetUserService(loadUserPort);

        when(loadUserPort.loadById(new Id(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(new GetUserCommand(new Id(1L))))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 1");
    }

    private User user() {
        return new User(
                new Id(1L),
                "Alice",
                new Email("alice@example.com"),
                "hashed-password",
                UserRole.CONSUMER,
                UserStatus.ACTIVE
        );
    }
}
