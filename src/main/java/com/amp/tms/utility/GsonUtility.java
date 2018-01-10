/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.objectbrains.commons.joda.LocalDateAdapter;
import com.objectbrains.commons.joda.LocalDateTimeAdapter;
import java.lang.reflect.Type;
import java.util.logging.Level;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 */
public class GsonUtility {

    private static final Logger LOG = LoggerFactory.getLogger(GsonUtility.class);

    public static Gson getGson(boolean prettyPrint) {
        GsonBuilder gb = new GsonBuilder();
        gb.excludeFieldsWithoutExposeAnnotation();

        gb.registerTypeAdapter(LocalDate.class, new JodaDateAdapter());
        gb.registerTypeAdapter(LocalDateTime.class, new JodaDateTimeAdapter());

        if (prettyPrint) {
            gb.setPrettyPrinting();
        }
        return gb.create();
    }

    static class JodaDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        private LocalDateAdapter adapter = new LocalDateAdapter();

        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            try {
                return new JsonPrimitive(adapter.marshal(src));
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to serialize LocalDate " + src, ex);
            }
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return adapter.unmarshal(json.getAsString());
            } catch (Exception ex) {
                throw new JsonParseException(ex);
            }
        }
    }

    static class JodaDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        private LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            try {
                return new JsonPrimitive(adapter.marshal(src));
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to serialize LocalDateTime " + src, ex);
            }
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return adapter.unmarshal(json.getAsString());
            } catch (Exception ex) {
                throw new JsonParseException(ex);
            }
        }

    }

}
