package edu.itba.exchange.services;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import edu.itba.exchange.exceptions.FetchException;
import edu.itba.exchange.interfaces.JSON;

public class GsonJSON implements JSON {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Override
    public <E> E parse(final String in, final Type type) {
        try {
            return GsonJSON.gson.fromJson(in, type);
        }catch (JsonSyntaxException e){
            throw new IllegalArgumentException("");
        }
        }

    @Override
    public String stringify(final Object type) {
        return GsonJSON.gson.toJson(type);
    }

    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public void write(final JsonWriter out, final LocalDate value) throws IOException {
            out.value(value.format(formatter));
        }

        @Override
        public LocalDate read(final JsonReader in) throws IOException {
            return LocalDate.parse(in.nextString(), formatter);
        }
    }
}
