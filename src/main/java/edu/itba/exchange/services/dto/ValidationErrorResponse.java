package edu.itba.exchange.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ValidationErrorResponse {
    private String message;
    private Map<String, String[]> errors;
}
