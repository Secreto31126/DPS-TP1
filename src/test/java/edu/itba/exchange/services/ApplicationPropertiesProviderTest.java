package edu.itba.exchange.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ApplicationPropertiesProviderTest {

    @Test
    void shouldLoadPropertiesSuccessfully() {
        // Given
        final var provider = new ApplicationPropertiesProvider();

        // When / Then
        assertThat(provider.get("PROP1"), is("VAL1"));
        assertThat(provider.get("PROP2"), is("VAL2"));
    }

    @Test
    void shouldThrowWhenPropertyNotFound() {
        // Given
        final var provider = new ApplicationPropertiesProvider();

        // When / Then
        assertThrows(NullPointerException.class, () -> provider.get("NON_EXISTENT_PROPERTY"));
    }

    @Test
    void shouldThrowWhenFileNotFound() {
        // When / Then
        assertThrows(IllegalStateException.class, () -> new ApplicationPropertiesProvider("fake.properties"));
    }
}
