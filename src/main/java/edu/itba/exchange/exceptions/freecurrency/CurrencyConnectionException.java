package edu.itba.exchange.exceptions.freecurrency;

public class CurrencyConnectionException extends RuntimeException{
    public CurrencyConnectionException(Throwable e){
        super(e);
    }
}
