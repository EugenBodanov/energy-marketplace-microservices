package com.energy.marketplace.user.application.service;

import com.energy.marketplace.user.application.command.LoginUserCommand;
import com.energy.marketplace.user.application.port.out.LoadUserPort;
import com.energy.marketplace.user.application.port.out.PasswordHasherPort;
import com.energy.marketplace.user.application.port.out.TokenIssuerPort;
import com.energy.marketplace.user.domain.exception.InvalidCredentialsException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserServiceTest {

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private TokenIssuerPort tokenIssuerPort;

    @Test
    void logsInUserWithMatchingPassword() {
        LoginUserService service = new LoginUserService(loadUserPort, passwordHasherPort, tokenIssuerPort);
        User user = user();

        when(loadUserPort.loadByEmail(new Email("alice@example.com"))).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("raw-password", "hashed-password")).thenReturn(true);
        when(tokenIssuerPort.issueToken(user)).thenReturn("access-token");

        var result = service.login(new LoginUserCommand("ALICE@EXAMPLE.COM", "raw-password"));

        assertThat(result.userId()).isEqualTo(new Id(1L));
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.role()).isEqualTo(UserRole.CONSUMER);
    }

    @Test
    void rejectsMissingUser() {
        LoginUserService service = new LoginUserService(loadUserPort, passwordHasherPort, tokenIssuerPort);

        when(loadUserPort.loadByEmail(new Email("alice@example.com"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginUserCommand("alice@example.com", "raw-password")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or rawPassword");

        verify(passwordHasherPort, never()).matches(any(), any());
        verify(tokenIssuerPort, never()).issueToken(any());
    }

    @Test
    void rejectsWrongPasswordWithoutIssuingToken() {
        LoginUserService service = new LoginUserService(loadUserPort, passwordHasherPort, tokenIssuerPort);
        User user = user();

        when(loadUserPort.loadByEmail(new Email("alice@example.com"))).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginUserCommand("alice@example.com", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or rawPassword");

        verify(tokenIssuerPort, never()).issueToken(any());
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
