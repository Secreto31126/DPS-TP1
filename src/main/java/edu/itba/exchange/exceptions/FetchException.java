package edu.itba.exchange.exceptions;

import lombok.Getter;

@Getter
public class FetchException extends Exception {
    public FetchException(final Throwable e) {
        super(e);
    }
}
