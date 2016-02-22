package com.freva.masteroppgave.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JSONUtils {
    /**
     * Converts object instance to JSON formatted String
     * @param object Object to convert to JSON
     * @param pretty Use pretty formatting or not
     * @return JSON formatted String
     */
    public static String toJSON(Object object, boolean pretty) {
        Gson gson = (pretty ? new GsonBuilder().setPrettyPrinting() : new GsonBuilder()).create();
        return gson.toJson(object);
    }


    /**
     * Converts object to JSON formatted string with typeToken adapter
     * @param object Object to convert to JSON
     * @param typeToken Adapter to use for conversion
     * @param pretty Use pretty formatting or not
     * @return JSON formatted String
     */
    public static String toJSON(Object object, TypeToken typeToken, boolean pretty) {
        Gson gson = (pretty ? new GsonBuilder().setPrettyPrinting() : new GsonBuilder()).registerTypeAdapter(typeToken.getType(), object).create();
        return gson.toJson(object);
    }


    /**
     * Parses JSON String and returns corresponding instance
     * @param typeToken Type of object in JSON
     * @return Object of type specified by typeToken
     */
    public static<T> T fromJSON(String json, TypeToken<T> typeToken) {
        return new Gson().fromJson(json, typeToken.getType());
    }
}
