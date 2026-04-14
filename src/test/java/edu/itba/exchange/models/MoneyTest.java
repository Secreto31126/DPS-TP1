package edu.itba.exchange.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void shouldCreateMoneyWithBigDecimal() {
        // Given
        final var amount = new BigDecimal("100.50");

        // When
        final var money = new Money(amount, USD);

        // Then
        assertThat(money.amount(), is(amount));
        assertThat(money.currency(), is(USD));
    }

    @Test
    void shouldCreateMoneyWithStringAmount() {
        // Given / When
        final var money = new Money("100.50", USD);

        // Then
        assertThat(money.amount(), is(new BigDecimal("100.50")));
        assertThat(money.currency(), is(USD));
    }

    @Test
    void shouldConvertWithMatchingRate() {
        // Given
        final var money = new Money("100", EUR);
        final var rate = new Rate(EUR, USD, new BigDecimal("1.10"));

        // When
        final var result = money.convert(rate);

        // Then
        assertThat(result.amount(), is(new BigDecimal("110.00")));
        assertThat(result.currency(), is(USD));
    }

    @Test
    void shouldThrowWhenRateCurrencyDoesNotMatch() {
        // Given
        final var money = new Money("100", EUR);
        final var rate = new Rate(USD, EUR, new BigDecimal("0.90"));

        // When / Then
        assertThrows(IllegalStateException.class, () -> money.convert(rate));
    }
}
