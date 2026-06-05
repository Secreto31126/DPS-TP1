package edu.itba.exchange.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class GsonJSONTest {
    private final GsonJSON json = new GsonJSON();

    private static record Payload(LocalDate date) {
    }

    private static final Payload object = new Payload(LocalDate.EPOCH);
    private static final String string = "{\"date\":\"%s\"}".formatted(LocalDate.EPOCH);

    @Test
    void shouldParseJsonToObject() {
        // Given a JSON string with a LocalDate field

        // When
        final var out = json.parse(string, object.getClass());

        // Then
        assertThat(out, is(object));
    }

    @Test
    void shouldStringifyObjectToJson() {
        // Given a Payload with a LocalDate field

        // When
        final var out = json.stringify(object);

        // Then
        assertThat(out, is(string));
    }

    @Test
    void shouldReturnNullWhenParsingNullInput() {
        // When
        final Payload out = json.parse(null, Payload.class);

        // Then
        assertThat(out, is(nullValue()));
    }

    @Test
    void shouldThrowWhenParsingMalformedJson() {
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> json.parse("not valid json", Payload.class));
    }
}
