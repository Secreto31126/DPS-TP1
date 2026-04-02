package edu.itba.exchange.interfaces;

import java.util.Currency;

import edu.itba.exchange.models.Rate;

public interface ExchangeRateProvider {
    Rate getRate(final Currency from, final Currency to);
}
