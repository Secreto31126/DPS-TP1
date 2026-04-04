package edu.itba.exchange.exceptions;

public class ExternalServiceException extends RuntimeException{
    public ExternalServiceException(String msg){
        super(msg);
    }
}