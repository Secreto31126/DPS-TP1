package edu.itba.exchange;

import java.util.Currency;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.time.LocalDate;

import edu.itba.exchange.exceptions.ApiError;
import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.interfaces.ExchangeRateProvider;
import edu.itba.exchange.models.*;
import lombok.AllArgsConstructor;

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
        } catch (final CurrencyException e) {
            return new AvailableCurrenciesResult.Failure(e.getApiError());
        }
    }

    public ConversionResult convert(final Money money, final Currency to) {
        return this.convert(money, List.of(to)).getFirst();
    }

    public List<ConversionResult> convert(final Money money, final List<Currency> to) {
        return this.collectProviderResult(
                () -> this.provider.getRate(money.currency(), to),
                rate -> new ConversionResult.Success(money.convert(rate), rate),
                ConversionResult.Failure::new);
    }

    public ConversionResult convert(final Money money, final Currency to, final LocalDate date) {
        return this.convert(money, List.of(to), date).getFirst();
    }

    public List<ConversionResult> convert(final Money money, final List<Currency> to, final LocalDate date) {
        return this.collectProviderResult(
                () -> this.provider.getRate(money.currency(), to, date),
                rate -> new ConversionResult.Success(money.convert(rate), rate),
                ConversionResult.Failure::new);
    }

    public ExchangeRateResult getExchangeRate(final Currency from, final Currency to) {
        return this.getExchangeRate(from, List.of(to)).getFirst();
    }

    public List<ExchangeRateResult> getExchangeRate(final Currency from, final List<Currency> to) {
        return this.collectProviderResult(
                () -> this.provider.getRate(from, to),
                ExchangeRateResult.Success::new,
                ExchangeRateResult.Failure::new);
    }

    public ExchangeRateResult getExchangeRate(final Currency from, final Currency to, final LocalDate date) {
        return this.getExchangeRate(from, List.of(to), date).getFirst();
    }

    public List<ExchangeRateResult> getExchangeRate(
            final Currency from,
            final List<Currency> to,
            final LocalDate date) {
        return this.collectProviderResult(
                () -> this.provider.getRate(from, to, date),
                ExchangeRateResult.Success::new,
                ExchangeRateResult.Failure::new);
    }

    private <E> List<E> collectProviderResult(final Supplier<List<Rate>> rates, final Function<Rate, E> ok,
            final Function<ApiError, E> err) {
        try {
            return rates.get().stream().map(ok).toList();
        } catch (CurrencyException e) {
            return List.of(err.apply(e.getApiError()));
        }
    }
}
