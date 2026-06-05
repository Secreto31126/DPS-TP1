package edu.itba.exchange.services.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ValidationErrorResponse {
    private String message;
    private Map<String, String[]> errors;
}
