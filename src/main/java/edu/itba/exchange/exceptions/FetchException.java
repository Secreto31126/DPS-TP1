package edu.itba.exchange.exceptions;

public class FetchException extends Exception {
    private final int status;

    public FetchException(final int status, final String body) {
        super(body);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
