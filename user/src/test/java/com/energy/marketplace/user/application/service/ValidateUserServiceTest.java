package com.energy.marketplace.user.application.service;

import com.energy.marketplace.user.application.command.UserValidationPurpose;
import com.energy.marketplace.user.application.command.ValidateUserCommand;
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
class ValidateUserServiceTest {

    @Mock
    private LoadUserPort loadUserPort;

    @Test
    void activeUserCanParticipateInTrade() {
        ValidateUserService service = new ValidateUserService(loadUserPort);

        when(loadUserPort.loadById(new Id(1L))).thenReturn(Optional.of(user(UserRole.CONSUMER, UserStatus.ACTIVE)));

        var result = service.validateUser(new ValidateUserCommand(
                new Id(1L),
                UserValidationPurpose.PARTICIPATE_IN_TRADE
        ));

        assertThat(result.valid()).isTrue();
        assertThat(result.message()).isEqualTo("User is valid");
        assertThat(result.role()).isEqualTo(UserRole.CONSUMER);
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void inactiveUserCannotParticipateInTrade() {
        ValidateUserService service = new ValidateUserService(loadUserPort);

        when(loadUserPort.loadById(new Id(1L))).thenReturn(Optional.of(user(UserRole.CONSUMER, UserStatus.INACTIVE)));

        var result = service.validateUser(new ValidateUserCommand(
                new Id(1L),
                UserValidationPurpose.PARTICIPATE_IN_TRADE
        ));

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).isEqualTo("User cannot participate in trade");
        assertThat(result.status()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void blockedUserCannotParticipateInTrade() {
        ValidateUserService service = new ValidateUserService(loadUserPort);

        when(loadUserPort.loadById(new Id(1L))).thenReturn(Optional.of(user(UserRole.PROSUMER, UserStatus.BLOCKED)));

        var result = service.validateUser(new ValidateUserCommand(
                new Id(1L),
                UserValidationPurpose.PARTICIPATE_IN_TRADE
        ));

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).isEqualTo("User cannot participate in trade");
        assertThat(result.status()).isEqualTo(UserStatus.BLOCKED);
    }

    @Test
    void activeConsumerCanBuyEnergyButCannotSellEnergy() {
        ValidateUserService service = new ValidateUserService(loadUserPort);

        when(loadUserPort.loadById(new Id(1L))).thenReturn(Optional.of(user(UserRole.CONSUMER, UserStatus.ACTIVE)));

        var buyResult = service.validateUser(new ValidateUserCommand(
                new Id(1L),
                UserValidationPurpose.BUY_ENERGY
        ));
        var sellResult = service.validateUser(new ValidateUserCommand(
                new Id(1L),
                UserValidationPurpose.SELL_ENERGY
        ));

        assertThat(buyResult.valid()).isTrue();
        assertThat(sellResult.valid()).isFalse();
        assertThat(sellResult.message()).isEqualTo("User cannot sell energy");
    }

    @Test
    void activeProsumerCanBuyAndSellEnergy() {
        ValidateUserService service = new ValidateUserService(loadUserPort);

        when(loadUserPort.loadById(new Id(1L))).thenReturn(Optional.of(user(UserRole.PROSUMER, UserStatus.ACTIVE)));

        var sellResult = service.validateUser(new ValidateUserCommand(
                new Id(1L),
                UserValidationPurpose.SELL_ENERGY
        ));
        var buyResult = service.validateUser(new ValidateUserCommand(
                new Id(1L),
                UserValidationPurpose.BUY_ENERGY
        ));

        assertThat(sellResult.valid()).isTrue();
        assertThat(buyResult.valid()).isTrue();
        assertThat(buyResult.message()).isEqualTo("User is valid");
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        ValidateUserService service = new ValidateUserService(loadUserPort);

        when(loadUserPort.loadById(new Id(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateUser(new ValidateUserCommand(
                new Id(1L),
                UserValidationPurpose.PARTICIPATE_IN_TRADE
        )))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 1");
    }

    private User user(UserRole role, UserStatus status) {
        return new User(
                new Id(1L),
                "Alice",
                new Email("alice@example.com"),
                "hashed-password",
                role,
                status
        );
    }
}
