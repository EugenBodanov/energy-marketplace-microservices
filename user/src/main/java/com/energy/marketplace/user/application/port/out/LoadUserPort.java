package com.energy.marketplace.user.application.port.out;
import com.energy.marketplace.user.domain.model.User;
import com.energy.marketplace.user.domain.valueObject.Email;
import com.energy.marketplace.user.domain.valueObject.Id;

import java.util.Optional;

public interface LoadUserPort {
    Optional<User> loadById(Id userId);
    Optional<User> loadByEmail(Email email);
}
