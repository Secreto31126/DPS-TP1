package edu.itba.exchange.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import edu.itba.exchange.interfaces.PropertiesProvider;

public record ApplicationPropertiesProvider(Properties properties) implements PropertiesProvider {
    private static final String FILE_PATH = "application.properties";

    public ApplicationPropertiesProvider {
        try (final var in = new FileInputStream(FILE_PATH)) {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(FILE_PATH + " not found!");
        }
    }

    @Override
    public String get(final String name) {
        final var value = properties.getProperty(name);
        return Objects.requireNonNull(value);
    }
}
