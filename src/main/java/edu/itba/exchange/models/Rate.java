package edu.itba.exchange.models;

import java.math.BigDecimal;
import java.util.Currency;

public record Rate(Currency from, Currency to, BigDecimal value) {
    public Rate(Currency from, Currency to, String value) {
        this(from, to, new BigDecimal(value));
    }
}
