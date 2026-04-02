package edu.itba.exchange.services;

import java.lang.reflect.Type;

import com.google.gson.Gson;

import edu.itba.exchange.interfaces.JSON;

public class GsonJSON implements JSON {
    @Override
    public <E> E parse(final String in, final Type type) {
        return new Gson().fromJson(in, type);
    }
}
