package edu.itba.exchange.exceptions;

import lombok.Getter;

@Getter
public class FetchException extends Exception {
    private final int status;
    private final String body;

    public FetchException(final int status, final String body) {
        super(body);
        this.status = status;
        this.body = body;
    }

}
