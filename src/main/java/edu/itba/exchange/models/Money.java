package edu.itba.exchange.models;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {
    public Money(String amount, Currency currency) {
        this(new BigDecimal(amount), currency);
    }

    public Money convert(final Rate rate) {
        if (!this.currency.equals(rate.from())) {
            throw new IllegalStateException("The provided rate doesn't match with the currency");
        }

        return new Money(this.amount.multiply(rate.value()), rate.to());
    }
}
