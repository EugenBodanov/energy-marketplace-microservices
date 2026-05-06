package com.energy.marketplace.user.application.service;

import com.energy.marketplace.user.application.command.RegisterUserCommand;
import com.energy.marketplace.user.application.port.out.CheckUserExistsPort;
import com.energy.marketplace.user.application.port.out.PasswordHasherPort;
import com.energy.marketplace.user.application.port.out.SaveUserPort;
import com.energy.marketplace.user.domain.exception.UserAlreadyExistsException;
import com.energy.marketplace.user.domain.model.User;
import com.energy.marketplace.user.domain.valueObject.Email;
import com.energy.marketplace.user.domain.valueObject.Id;
import com.energy.marketplace.user.domain.valueObject.UserRole;
import com.energy.marketplace.user.domain.valueObject.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private SaveUserPort saveUserPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private CheckUserExistsPort checkUserExistsPort;

    @Test
    void registersNewUserWithHashedPassword() {
        RegisterUserService service = new RegisterUserService(saveUserPort, passwordHasherPort, checkUserExistsPort);
        RegisterUserCommand command = new RegisterUserCommand(
                "Alice",
                "ALICE@EXAMPLE.COM",
                "raw-password",
                UserRole.CONSUMER
        );

        when(checkUserExistsPort.existsByEmail(new Email("alice@example.com"))).thenReturn(false);
        when(passwordHasherPort.hash("raw-password")).thenReturn("hashed-password");
        when(saveUserPort.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);

            return new User(
                    new Id(1L),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole(),
                    user.getStatus()
            );
        });

        var result = service.register(command);

        assertThat(result.id()).isEqualTo(new Id(1L));
        assertThat(result.name()).isEqualTo("Alice");
        assertThat(result.email()).isEqualTo(new Email("alice@example.com"));
        assertThat(result.role()).isEqualTo(UserRole.CONSUMER);
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);

        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(saveUserPort).save(savedUserCaptor.capture());

        User savedUser = savedUserCaptor.getValue();
        assertThat(savedUser.getId()).isNull();
        assertThat(savedUser.getPassword()).isEqualTo("hashed-password");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);

        verify(passwordHasherPort).hash("raw-password");
    }

    @Test
    void rejectsDuplicateEmailWithoutHashingOrSaving() {
        RegisterUserService service = new RegisterUserService(saveUserPort, passwordHasherPort, checkUserExistsPort);
        RegisterUserCommand command = new RegisterUserCommand(
                "Alice",
                "alice@example.com",
                "raw-password",
                UserRole.CONSUMER
        );

        when(checkUserExistsPort.existsByEmail(new Email("alice@example.com"))).thenReturn(true);

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User already exists with email: alice@example.com");

        verify(passwordHasherPort, never()).hash(any());
        verify(saveUserPort, never()).save(any());
    }
}
