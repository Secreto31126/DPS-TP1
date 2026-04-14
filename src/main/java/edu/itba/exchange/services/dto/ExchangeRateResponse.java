package edu.itba.exchange.services.dto;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Data;

@Data
public class ExchangeRateResponse {
    private Map<String, BigDecimal> data;
}
