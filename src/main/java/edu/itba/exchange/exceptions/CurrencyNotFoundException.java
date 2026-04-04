package edu.itba.exchange.exceptions;

public class CurrencyNotFoundException extends RuntimeException {
    public CurrencyNotFoundException(String msg){
        super(msg);
    }
}