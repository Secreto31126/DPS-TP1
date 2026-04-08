package edu.itba.exchange.exceptions;

public abstract class CurrencyException extends RuntimeException {
    public CurrencyException(String message) {
        super(message);
    }
}
