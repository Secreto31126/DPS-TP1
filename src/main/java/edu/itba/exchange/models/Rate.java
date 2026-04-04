package edu.itba.exchange.models;

import java.math.BigDecimal;
import java.util.Currency;
import java.time.LocalDate;

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

    // At this Rate (pun intended), we should add the 2⁴ combinations
    // of String to Currency/BigDecimal + LocalDate :)
}
