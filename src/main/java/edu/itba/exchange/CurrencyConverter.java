package edu.itba.exchange;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

import edu.itba.exchange.interfaces.ExchangeRateProvider;
import edu.itba.exchange.models.Money;
import lombok.AllArgsConstructor;
import edu.itba.exchange.ConversionResult.*;

@AllArgsConstructor
public class CurrencyConverter {
    private final ExchangeRateProvider provider;

    public AvailableCurrenciesResult getAvailableCurrencies() {
        return this.getAvailableCurrencies(List.of());
    }

    public AvailableCurrenciesResult getAvailableCurrencies(final List<String> currencyCodes) {
        try {
            final var currencies = this.provider.getAvailableCurrencies(currencyCodes);
            return new AvailableCurrenciesResult.Success(currencies);
        } catch (final Exception e) {
            return new AvailableCurrenciesResult.Failure(e.getMessage());
        }
    }

    public ConversionResult convert(final Money money, final Currency to) {
        return this.convert(money, List.of(to)).getFirst();
    }

    public ConversionResult convert(final Money money, final Currency to, final LocalDate date) {
        return this.convert(money, List.of(to), date).getFirst();
    }

    public List<ConversionResult> convert(final Money money, final List<Currency> to) {
        return this.convert(money, to, null);
    }

    public List<ConversionResult> convert(final Money money, final List<Currency> to, final LocalDate date) {
        try {
            final var rates = date == null ? this.provider.getRate(money.currency(), to) : this.provider.getRate(money.currency(), to, date);
            return rates.stream().map(rate -> new ConversionResult.Success(money.convert(rate), rate)).collect(Collectors.toUnmodifiableList());
        } catch (final Exception e) {
            return List.of(
                new ConversionResult.Failure(e.getMessage())
            );
        }
    }

    public ExchangeRateResult getExchangeRate(final Currency from, final Currency to) {
        return this.getExchangeRate(from, List.of(to)).getFirst();
    }

    public ExchangeRateResult getExchangeRate(final Currency from, final Currency to, final java.time.LocalDate date) {
        return this.getExchangeRate(from, List.of(to), date).getFirst();
    }

    public List<ExchangeRateResult> getExchangeRate(final Currency from, final List<Currency> to) {
        return this.getExchangeRate(from, to, null);
    }

    public List<ExchangeRateResult> getExchangeRate(final Currency from, final List<Currency> to, final java.time.LocalDate date) {
        try {
            final var rates = date == null ? this.provider.getRate(from, to) : this.provider.getRate(from, to, date);
            return rates.stream().map(ExchangeRateResult.Success::new).collect(Collectors.toUnmodifiableList());
        } catch (final Exception e) {
            return List.of(
                new ExchangeRateResult.Failure(e.getMessage())
            );
        }
    }
}
