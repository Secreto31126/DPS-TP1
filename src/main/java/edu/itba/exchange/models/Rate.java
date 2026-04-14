package edu.itba.exchange.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

public record Rate(Currency from, Currency to, BigDecimal value, LocalDate rateDate) {
    public Rate(Currency from, Currency to, BigDecimal value) {
        this(from, to, value, LocalDate.now());
    }

    public Rate(Currency from, Currency to, String value) {
        this(from, to, new BigDecimal(value));
    }

    public Rate(Currency from, String to, BigDecimal value) {
        this(from, Currency.getInstance(to), value);
    }

    public Rate(Currency from, Currency to, String value, LocalDate rateDate) {
        this(from, to, new BigDecimal(value), rateDate);
    }
}
