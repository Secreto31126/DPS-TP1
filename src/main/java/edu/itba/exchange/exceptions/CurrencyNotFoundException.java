package edu.itba.exchange.exceptions;

public class CurrencyNotFoundException extends CurrencyException {
    public CurrencyNotFoundException(String msg){
        super(msg);
    }
}