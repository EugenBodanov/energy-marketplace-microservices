package com.energy.marketplace.user.domain.model;

import com.energy.marketplace.user.domain.valueObject.Email;
import com.energy.marketplace.user.domain.valueObject.Id;
import com.energy.marketplace.user.domain.valueObject.UserRole;
import com.energy.marketplace.user.domain.valueObject.UserStatus;
import lombok.Getter;

@Getter
public class User {
    private Id id;
    private String name;
    private Email email;
    private String password;
    private UserRole role;
    private UserStatus status;

    public User(Id id, String name, Email email, String password, UserRole role, UserStatus status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public static User registerNew(
            String name,
            Email email,
            String passwordHash,
            UserRole role
    ) {
        return new User(
                null,
                name,
                email,
                passwordHash,
                role,
                UserStatus.ACTIVE
        );
    }

    public boolean isValid() {
        return name != null && !name.isEmpty() && email != null && email.isValid() && password != null && !password.isEmpty() && role != null && status != null;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", email=" + email + ", role=" + role + ", status=" + status + "]";
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean canBuyEnergy() {
        return isActive() && (role == UserRole.CONSUMER || role == UserRole.PROSUMER);
    }

    public boolean canSellEnergy() {
        return isActive() && role == UserRole.PROSUMER;
    }

}
