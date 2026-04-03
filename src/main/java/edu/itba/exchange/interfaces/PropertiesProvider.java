package edu.itba.exchange.interfaces;

public interface PropertiesProvider {
    /**
     * Get an env variable
     *
     * @param name The property name
     * @return The property value
     * @throws NullPointerException If the property doesn't exist
     */
    String get(final String name);
}
