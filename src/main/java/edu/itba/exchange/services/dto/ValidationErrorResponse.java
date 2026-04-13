package edu.itba.exchange.services.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ValidationErrorResponse {
    private String message;
    private Map<String, String[]> errors;
}
