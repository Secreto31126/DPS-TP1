package edu.itba.exchange.exceptions;

import edu.itba.exchange.interfaces.Fetch;
import lombok.Getter;

@Getter
public class FetchException extends Exception {

    public FetchException(Throwable e) {
        super(e);
    }
}
