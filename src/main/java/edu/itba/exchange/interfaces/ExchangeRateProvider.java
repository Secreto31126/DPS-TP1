package edu.itba.exchange.interfaces;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import edu.itba.exchange.models.Rate;

public interface ExchangeRateProvider {
    Rate getRate(final Currency from, final Currency to);

    List<Rate> getRate(final Currency from, final List<Currency> to);

    List<Rate> getRate(final Currency from, final List<Currency> to, final LocalDate rateDate);

    List<Currency> getAvailableCurrencies();

    List<Currency> getAvailableCurrencies(final List<Currency> currencyCodes);
}
