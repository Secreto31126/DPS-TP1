package edu.itba.exchange.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.*;

class ApplicationPropertiesProviderTest {
    @Test
    void shouldLoadPropertiesSuccessfully() {
        final var provider = new ApplicationPropertiesProvider();

        assertThat(provider.get("PROP1"), is("VAL1"));
        assertThat(provider.get("PROP2"), is("VAL2"));
    }

    @Test
    void shouldThrowExceptionWhenPropertyNotFound() {
        final var provider = new ApplicationPropertiesProvider();

        assertThrows(NullPointerException.class, () -> provider.get("NON_EXISTENT_PROPERTY"));
    }

    @Test
    void shouldThrowExceptionWhenFileNotFound() {
        assertThrows(IllegalStateException.class, () -> new ApplicationPropertiesProvider("fake.properties"));
    }
}
