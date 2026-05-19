package com.energy.marketplace.trade.application.port.out;

public interface ValidateTradeParticipantsPort {
    boolean validateBuyer(Long buyerId);
    boolean validateSeller(Long sellerId);
}
