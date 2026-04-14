package edu.itba.exchange.interfaces;

public interface FetchExceptionMapper<T extends RuntimeException> {
    T translate(final Fetch.Response e);
}
