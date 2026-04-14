package edu.itba.exchange.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import edu.itba.exchange.interfaces.PropertiesProvider;

public record ApplicationPropertiesProvider(Properties properties, String path) implements PropertiesProvider {
    private static final String DEFAULT_FILE_PATH = "application.properties";

    public ApplicationPropertiesProvider {
        try (final var in = getClass().getClassLoader().getResourceAsStream(path)) {
            properties.load(in);
        } catch (final IOException | NullPointerException e) {
            throw new IllegalStateException(path + " not found!");
        }
    }

    public ApplicationPropertiesProvider(final String path) {
        this(new Properties(), path);
    }

    public ApplicationPropertiesProvider() {
        this(DEFAULT_FILE_PATH);
    }

    @Override
    public String get(final String name) {
        final var value = properties.getProperty(name);
        return Objects.requireNonNull(value);
    }
}
