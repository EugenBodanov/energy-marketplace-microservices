package com.energy.marketplace.user.application.port.out;
import com.energy.marketplace.user.domain.model.User;

public interface SaveUserPort {
    User save(User user);
}
