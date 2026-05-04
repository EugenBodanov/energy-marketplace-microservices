package com.energy.marketplace.user.application.port.out;

import com.energy.marketplace.user.domain.model.User;

public interface TokenIssuerPort {

    String issueToken(User user);
}