package edu.itba.exchange;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.LocalDate;
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
	private static final Rate EUR_USD_RATE = new Rate(EUR, USD, "1.05");

	@Test
	void testConvert() throws MalformedURLException, URISyntaxException {
		// Given
		final var euros = new Money("100", EUR);
		final var dolars = new Money("105.00", USD);

		when(this.provider.getRate(EUR, List.of(USD))).thenReturn(List.of(EUR_USD_RATE));

		final var converter = new CurrencyConverter(provider);

		// When
		final var result = converter.convert(euros, USD);

		// Then
		assertThat(result, is(new ConversionResult.Success(dolars, EUR_USD_RATE)));
	}

	@Test
	void testHistoricalConvert() {
		// Given
		final var euros = new Money("100", EUR);
		final var date = LocalDate.of(2024, 1, 1);
		final var historicalRate = new Rate(EUR, USD, "1.10", date);
		final var dollars = new Money("110.00", USD);

		when(this.provider.getRate(EUR, List.of(USD), date)).thenReturn(List.of(historicalRate));

		final var converter = new CurrencyConverter(provider);

		// When
		final var result = converter.convert(euros, USD, date);

		// Then
		assertThat(result, is(new ConversionResult.Success(dollars, historicalRate)));
	}
}
