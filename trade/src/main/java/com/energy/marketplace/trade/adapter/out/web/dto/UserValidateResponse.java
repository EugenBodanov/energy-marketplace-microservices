package com.energy.marketplace.trade.adapter.out.web.dto;

public record UserValidateResponse(
        Long userId,
        boolean valid,
        String role,
        String status,
        String message
) {
}
