package edu.itba.exchange;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.itba.exchange.interfaces.ExchangeRateProvider;
import edu.itba.exchange.models.Money;
import edu.itba.exchange.models.Rate;

@ExtendWith(MockitoExtension.class)
class CurrencyConverterTest {
	@Mock
	private ExchangeRateProvider provider;

	private static final Currency USD = Currency.getInstance("USD");
	private static final Currency EUR = Currency.getInstance("EUR");
	private static final Currency CAD = Currency.getInstance("CAD");

	private static final Rate EUR_USD_RATE = new Rate(EUR, USD, "1.05");

	@Test
	void testConvert() throws MalformedURLException, URISyntaxException {
		// Given
		final var euros = new Money("100", EUR);
		final var dolars = new Money("105.00", USD);

		when(this.provider.getRate(EUR, USD)).thenReturn(EUR_USD_RATE);

		final var converter = new CurrencyConverter(provider);

		// When
		final var result = converter.convert(euros, USD);

		// Then
		assertThat(result, is(new ConversionResult.Success(dolars, EUR_USD_RATE)));
	}

	@Test
	void testGetAvailableCurrencies() {
		// Given
		final var CURRENCIES_LIST = List.of(USD, EUR, CAD);

		when(this.provider.getAvailableCurrencies(List.of())).thenReturn(CURRENCIES_LIST);

		final var converter = new CurrencyConverter(provider);

		// When
		final var result = converter.getAvailableCurrencies();

		// Then
		assertThat(result, is(new AvailableCurrenciesResult.Success(CURRENCIES_LIST)));
	}

	@Test
	void testGetSpecificCurrencies() {
		// Given
		final var CURRENCIES_LIST = List.of(USD, EUR);
		final var CURRENCY_CODES_LIST = List.of("USD", "EUR");

		when(this.provider.getAvailableCurrencies(CURRENCY_CODES_LIST)).thenReturn(CURRENCIES_LIST);

		final var converter = new CurrencyConverter(provider);

		// When
		final var result = converter.getAvailableCurrencies(CURRENCY_CODES_LIST);

		// Then
		assertThat(result, is(new AvailableCurrenciesResult.Success(CURRENCIES_LIST)));
	}
}
