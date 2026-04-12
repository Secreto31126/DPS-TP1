package edu.itba.exchange.models;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class MoneyTest {
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void shouldCreateMoneyWithBigDecimal() {
        BigDecimal amount = new BigDecimal("100.50");
        Money money = new Money(amount, USD);
        assertEquals(amount, money.amount());
        assertEquals(USD, money.currency());
    }

    @Test
    void shouldCreateMoneyWithString() {
        Money money = new Money("100.50", USD);
        assertEquals(new BigDecimal("100.50"), money.amount());
        assertEquals(USD, money.currency());
    }

    @Test
    void shouldConvertMoneySuccessfully() {
        Money money = new Money("100", EUR);
        Rate rate = new Rate(EUR, USD, new BigDecimal("1.10"));
        
        Money result = money.convert(rate);
        
        assertEquals(new BigDecimal("110.00"), result.amount());
        assertEquals(USD, result.currency());
    }

    @Test
    void shouldThrowExceptionWhenConvertingWithIncompatibleRate() {
        Money money = new Money("100", EUR);
        Rate rate = new Rate(USD, EUR, new BigDecimal("0.90"));
        
        assertThrows(IllegalStateException.class, () -> money.convert(rate));
    }
}