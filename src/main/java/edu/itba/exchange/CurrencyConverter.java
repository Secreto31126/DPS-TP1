package edu.itba.exchange;

import java.util.Currency;
import java.util.List;
import java.util.function.Function;
import java.time.LocalDate;

import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.interfaces.ExchangeRateProvider;
import edu.itba.exchange.models.Money;
import edu.itba.exchange.models.Rate;
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

    public ConversionResult convert(final Money money, final Currency to, final LocalDate date) {
        return this.convert(money, List.of(to), date).getFirst();
    }

    public List<ConversionResult> convert(final Money money, final List<Currency> to) {
        return this.convert(money, to, null);
    }

    public List<ConversionResult> convert(final Money money, final List<Currency> to, final LocalDate date) {
        try {
            return this.collectProviderResult(money.currency(), to, date, rate -> new ConversionResult.Success(money.convert(rate), rate));
        }
        catch (final CurrencyException e){
            return List.of(new ConversionResult.Failure(e.getApiError()));
        }
    }

    public ExchangeRateResult getExchangeRate(final Currency from, final Currency to) {
        return this.getExchangeRate(from, List.of(to)).getFirst();
    }

    public ExchangeRateResult getExchangeRate(final Currency from, final Currency to, final LocalDate date) {
        return this.getExchangeRate(from, List.of(to), date).getFirst();
    }

    public List<ExchangeRateResult> getExchangeRate(final Currency from, final List<Currency> to) {
        return this.getExchangeRate(from, to, null);
    }

    public List<ExchangeRateResult> getExchangeRate(final Currency from, final List<Currency> to, final LocalDate date) {
        try {
            return this.collectProviderResult(from, to, date, ExchangeRateResult.Success::new);
        } catch (CurrencyException e) {
            return List.of(new ExchangeRateResult.Failure(e.getApiError()));
        }
    }
    private <E> List<E> collectProviderResult(final Currency from, final List<Currency> to, final LocalDate date, Function<Rate, E> mapper){
        final var rates = date == null ? this.provider.getRate(from,to): this.provider.getRate(from, to, date);
        return rates.stream().map(mapper).toList();
    }
}
