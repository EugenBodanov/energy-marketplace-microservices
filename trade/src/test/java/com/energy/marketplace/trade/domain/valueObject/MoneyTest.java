package com.energy.marketplace.trade.domain.valueObject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    @DisplayName("Should create Money with correct amount and currency")
    void shouldCreateMoney() {
        BigDecimal amount = new BigDecimal("100.00");
        String currencyCode = "EUR";
        Money money = Money.of(amount, currencyCode);

        assertEquals(amount, money.amount());
        assertEquals(Currency.getInstance(currencyCode), money.currency());
    }

    @Test
    @DisplayName("Should throw exception when amount is null")
    void shouldThrowExceptionWhenAmountIsNull() {
        assertThrows(NullPointerException.class, () -> new Money(null, Currency.getInstance("EUR")));
    }

    @Test
    @DisplayName("Should throw exception when currency is null")
    void shouldThrowExceptionWhenCurrencyIsNull() {
        assertThrows(NullPointerException.class, () -> new Money(BigDecimal.ONE, null));
    }

    @Test
    @DisplayName("Should throw exception when amount is negative")
    void shouldThrowExceptionWhenAmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("-1.00"), "EUR"));
    }

    @Test
    @DisplayName("Should add money with same currency")
    void shouldAddMoney() {
        Money m1 = Money.of("10.00", "EUR");
        Money m2 = Money.of("20.00", "EUR");
        Money result = m1.add(m2);

        assertEquals(new BigDecimal("30.00"), result.amount());
        assertEquals("EUR", result.currencyCode());
    }

    @Test
    @DisplayName("Should throw exception when adding money with different currency")
    void shouldThrowExceptionWhenAddingDifferentCurrency() {
        Money m1 = Money.of("10.00", "EUR");
        Money m2 = Money.of("20.00", "USD");

        assertThrows(IllegalArgumentException.class, () -> m1.add(m2));
    }

    @Test
    @DisplayName("Should subtract money")
    void shouldSubtractMoney() {
        Money m1 = Money.of("30.00", "EUR");
        Money m2 = Money.of("10.00", "EUR");
        Money result = m1.subtract(m2);

        assertEquals(new BigDecimal("20.00"), result.amount());
    }

    @Test
    @DisplayName("Should throw exception when subtraction result is negative")
    void shouldThrowExceptionWhenSubtractionResultIsNegative() {
        Money m1 = Money.of("10.00", "EUR");
        Money m2 = Money.of("30.00", "EUR");

        assertThrows(IllegalArgumentException.class, () -> m1.subtract(m2));
    }

    @Test
    @DisplayName("Should check if money is zero")
    void shouldCheckIsZero() {
        assertTrue(Money.zero("EUR").isZero());
        assertFalse(Money.of("1.00", "EUR").isZero());
    }

    @Test
    @DisplayName("Should check if money is positive")
    void shouldCheckIsPositive() {
        assertTrue(Money.of("0.01", "EUR").isPositive());
        assertFalse(Money.zero("EUR").isPositive());
    }
}
