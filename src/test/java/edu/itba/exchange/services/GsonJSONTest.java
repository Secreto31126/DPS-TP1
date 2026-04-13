package edu.itba.exchange.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.util.Currency;

import org.junit.jupiter.api.Test;

import edu.itba.exchange.models.Money;

public class GsonJSONTest {
    private static final GsonJSON json = new GsonJSON();

    private static record Payload(LocalDate date) {
    }

    private static final Payload object = new Payload(LocalDate.EPOCH);
    private static final String string = "{\"date\":\"%s\"}".formatted(LocalDate.EPOCH);

    @Test
    void testParse() {
        final var out = json.parse(string, object.getClass());
        assertThat(out, is(object));
    }

    @Test
    void testStringify() {
        final var out = json.stringify(object);
        assertThat(out, is(string));
    }
}
