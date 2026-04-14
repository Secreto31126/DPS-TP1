package edu.itba.exchange.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class RateTest {
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void shouldCreateRateWithAllFields() {
        // Given
        final var date = LocalDate.of(2024, 6, 1);

        // When
        final var rate = new Rate(EUR, USD, new BigDecimal("1.10"), date);

        // Then
        assertThat(rate.from(), is(EUR));
        assertThat(rate.to(), is(USD));
        assertThat(rate.value(), is(new BigDecimal("1.10")));
        assertThat(rate.rateDate(), is(date));
    }

    @Test
    void shouldDefaultToTodayWhenDateOmitted() {
        // Given
        final var today = LocalDate.now();

        // When
        final var rate = new Rate(EUR, USD, new BigDecimal("1.10"));

        // Then
        assertThat(rate.rateDate(), is(today));
    }

    @Test
    void shouldParseStringValue() {
        // Given / When
        final var rate = new Rate(EUR, USD, "1.10");

        // Then
        assertThat(rate.value(), is(new BigDecimal("1.10")));
        assertThat(rate.from(), is(EUR));
        assertThat(rate.to(), is(USD));
    }

    @Test
    void shouldParseStringCurrencyCode() {
        // Given / When
        final var rate = new Rate(EUR, "USD", new BigDecimal("1.10"));

        // Then
        assertThat(rate.to(), is(USD));
        assertThat(rate.from(), is(EUR));
        assertThat(rate.value(), is(new BigDecimal("1.10")));
    }

    @Test
    void shouldParseStringValueWithDate() {
        // Given
        final var date = LocalDate.of(2024, 6, 1);

        // When
        final var rate = new Rate(EUR, USD, "1.10", date);

        // Then
        assertThat(rate.value(), is(new BigDecimal("1.10")));
        assertThat(rate.rateDate(), is(date));
        assertThat(rate.from(), is(EUR));
        assertThat(rate.to(), is(USD));
    }
}
