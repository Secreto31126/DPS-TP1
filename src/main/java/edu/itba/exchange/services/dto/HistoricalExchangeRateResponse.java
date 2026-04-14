package edu.itba.exchange.services.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

import lombok.Data;

@Data
public class HistoricalExchangeRateResponse {
    private Map<LocalDate, Map<Currency, BigDecimal>> data;
}
