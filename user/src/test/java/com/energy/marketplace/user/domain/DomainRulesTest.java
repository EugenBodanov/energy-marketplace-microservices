package com.energy.marketplace.user.domain;

import com.energy.marketplace.user.domain.model.User;
import com.energy.marketplace.user.domain.valueObject.Email;
import com.energy.marketplace.user.domain.valueObject.Id;
import com.energy.marketplace.user.domain.valueObject.UserRole;
import com.energy.marketplace.user.domain.valueObject.UserStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainRulesTest {

    @Test
    void emailNormalizesCaseAndWhitespace() {
        Email email = new Email("  Alice@Example.COM  ");

        assertThat(email.value()).isEqualTo("alice@example.com");
        assertThat(email.isValid()).isTrue();
    }

    @Test
    void emailRejectsEmptyAndInvalidValues() {
        assertThatThrownBy(() -> new Email(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email must not be empty");

        assertThatThrownBy(() -> new Email("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    void idRejectsNullAndNonPositiveValues() {
        assertThatThrownBy(() -> new Id(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Id must not be null");

        assertThatThrownBy(() -> new Id(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Id must be positive");
    }

    @Test
    void registerNewCreatesActiveUserWithoutId() {
        User user = User.registerNew(
                "Alice",
                new Email("alice@example.com"),
                "hashed-password",
                UserRole.CONSUMER
        );

        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isEqualTo("Alice");
        assertThat(user.getEmail()).isEqualTo(new Email("alice@example.com"));
        assertThat(user.getPassword()).isEqualTo("hashed-password");
        assertThat(user.getRole()).isEqualTo(UserRole.CONSUMER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isValid()).isTrue();
    }

    @Test
    void userEnergyCapabilitiesDependOnRoleAndStatus() {
        User activeConsumer = user(UserRole.CONSUMER, UserStatus.ACTIVE);
        User activeProsumer = user(UserRole.PROSUMER, UserStatus.ACTIVE);
        User blockedProsumer = user(UserRole.PROSUMER, UserStatus.BLOCKED);

        assertThat(activeConsumer.canBuyEnergy()).isTrue();
        assertThat(activeConsumer.canSellEnergy()).isFalse();
        assertThat(activeProsumer.canBuyEnergy()).isTrue();
        assertThat(activeProsumer.canSellEnergy()).isTrue();
        assertThat(blockedProsumer.isActive()).isFalse();
        assertThat(blockedProsumer.canBuyEnergy()).isFalse();
        assertThat(blockedProsumer.canSellEnergy()).isFalse();
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
