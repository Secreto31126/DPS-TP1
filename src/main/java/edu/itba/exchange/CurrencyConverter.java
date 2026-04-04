package edu.itba.exchange;

import java.util.Currency;
import java.util.List;

import edu.itba.exchange.exceptions.CurrencyNotFoundException;
import edu.itba.exchange.interfaces.ExchangeRateProvider;
import edu.itba.exchange.models.Money;
import lombok.AllArgsConstructor;
import edu.itba.exchange.ConversionResult.*;

@AllArgsConstructor
public class CurrencyConverter {
    private final ExchangeRateProvider provider;

    public ConversionResult convert(final Money money, final Currency to) {
        try {
            final var rate = this.provider.getRate(money.currency(), to);
            return new ConversionResult.Success(money.convert(rate), rate);
        } catch (final Exception e) {
            return new ConversionResult.Failure(e.getMessage());
        }
    }

    public List<ConversionResult> convert(final Money money, final List<Currency> to) {
        return to.stream()
                .map(currency -> this.convert(money, currency))
                .toList();
    }

    public AvailableCurrenciesResult getAvailableCurrencies() {
        final var currencies = this.provider.getAvailableCurrencies();
        return new AvailableCurrenciesResult.Success(currencies);
    }

    public AvailableCurrenciesResult getAvailableCurrencies(final List<String> currencyCodes) {
        try {
            final var currencies = this.provider.getAvailableCurrencies(currencyCodes);
            return new AvailableCurrenciesResult.Success(currencies);
        } catch (final CurrencyNotFoundException e) {
            return new AvailableCurrenciesResult.Failure(e.getMessage());
        }
    }
}
