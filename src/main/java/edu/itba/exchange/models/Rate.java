package edu.itba.exchange.models;

import java.math.BigDecimal;
import java.util.Currency;
import java.time.LocalDate;

public record Rate(Currency from, Currency to, BigDecimal value, LocalDate rateDate) {
    public Rate(Currency from, Currency to, String value, LocalDate rateDate) {
        this(from, to, new BigDecimal(value), rateDate);
    }

    public Rate(Currency from, Currency to, String value) {
        this(from, to, new BigDecimal(value), LocalDate.now());
    }
}
