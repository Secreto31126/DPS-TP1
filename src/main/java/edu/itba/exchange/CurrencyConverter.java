package edu.itba.exchange;

import java.util.Currency;

import edu.itba.exchange.interfaces.ExchangeRateProvider;
import edu.itba.exchange.models.Money;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CurrencyConverter {
    private final ExchangeRateProvider provider;

    public Money convert(final Money money, final Currency to) {
        final var rate = this.provider.getRate(money.currency(), to);
        return money.convert(rate);
    }
}
