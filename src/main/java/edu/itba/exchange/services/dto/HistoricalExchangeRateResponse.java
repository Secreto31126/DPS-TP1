package edu.itba.exchange.services.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class HistoricalExchangeRateResponse {
    private Map<String, Map<String, BigDecimal>> data;
}
