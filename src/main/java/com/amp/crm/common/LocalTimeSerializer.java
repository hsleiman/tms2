/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.common;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class LocalTimeSerializer extends StdSerializer<LocalTime> {

    public static final Logger LOG = LoggerFactory.getLogger(LocalTimeSerializer.class);
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");

    public LocalTimeSerializer() {
        this(null);
    }

    public LocalTimeSerializer(Class<LocalTime> t) {
        super(t);
    }

    @Override
    public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider sp) throws IOException, JsonGenerationException {
//        gen.writeString(formatter.format(value));
        LOG.info(value.toString(formatter));
        gen.writeString(value.toString(formatter));
    }

}
