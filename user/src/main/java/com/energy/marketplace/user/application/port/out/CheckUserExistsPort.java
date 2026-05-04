package com.energy.marketplace.user.application.port.out;

import com.energy.marketplace.user.domain.valueObject.Email;
import com.energy.marketplace.user.domain.valueObject.Id;

public interface CheckUserExistsPort {
    boolean existsById(Id id);
    boolean existsByEmail(Email email);
}
