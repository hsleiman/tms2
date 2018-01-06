/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author David
 */
public class LocalTimeDeserializer extends StdDeserializer<LocalTime> {

    public static final Logger LOG = LoggerFactory.getLogger(LocalTimeDeserializer.class);
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");

    public LocalTimeDeserializer() {
        this(null);
    }

    public LocalTimeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalTime deserialize(JsonParser jsonparser, DeserializationContext context) throws IOException, JsonProcessingException {
        String date = jsonparser.getText();
        LOG.info(date);
        return LocalTime.parse(date, formatter);
    }

}
