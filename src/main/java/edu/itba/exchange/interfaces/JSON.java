package edu.itba.exchange.interfaces;

import java.lang.reflect.Type;

public interface JSON {
    /**
     * Parse a string json to an object
     *
     * @param <E>  The structured response
     * @param in   The string input
     * @param type The structure to parse
     * @return The parsed structure
     */
    <E> E parse(final String in, final Type type);

    /**
     * Parse a string json to an object
     *
     * @param <E>  The structured response
     * @param in   The string input
     * @param type The structure to parse
     * @return The parsed structure
     */
    String stringify(final Object type);
}
