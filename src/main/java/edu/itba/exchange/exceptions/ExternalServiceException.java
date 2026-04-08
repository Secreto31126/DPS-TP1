package edu.itba.exchange.exceptions;

public class ExternalServiceException extends CurrencyException{
    public ExternalServiceException(String msg){
        super(msg);
    }
}