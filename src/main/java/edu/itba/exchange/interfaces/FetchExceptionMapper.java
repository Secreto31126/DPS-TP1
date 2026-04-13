package edu.itba.exchange.interfaces;

import edu.itba.exchange.exceptions.FetchException;

public interface FetchExceptionMapper<T extends RuntimeException> {
    T translate(FetchException e);
}
