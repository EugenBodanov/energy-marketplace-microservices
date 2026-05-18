package com.energy.marketplace.trade.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }

        int fractionDigits = currency.getDefaultFractionDigits();

        if (fractionDigits >= 0) {
            amount = amount.setScale(fractionDigits, RoundingMode.UNNECESSARY);
        }
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        Objects.requireNonNull(currencyCode, "Currency code must not be null");
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    public static Money of(String amount, String currencyCode) {
        return of(new BigDecimal(amount), currencyCode);
    }

    public static Money zero(String currencyCode) {
        return of(BigDecimal.ZERO, currencyCode);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);

        BigDecimal result = this.amount.subtract(other.amount);

        if (result.signum() < 0) {
            throw new IllegalArgumentException("Money result must not be negative");
        }

        return new Money(result, this.currency);
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public String currencyCode() {
        return currency.getCurrencyCode();
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "Other money must not be null");

        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + this.currencyCode() + " != " + other.currencyCode()
            );
        }
    }
}