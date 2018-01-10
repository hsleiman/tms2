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
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

    public static final Logger LOG = LoggerFactory.getLogger(LocalDateTimeSerializer.class);
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm");

    public LocalDateTimeSerializer() {
        this(null);
    }

    public LocalDateTimeSerializer(Class<LocalDateTime> t) {
        super(t);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider sp) throws IOException, JsonGenerationException {
//        gen.writeString(formatter.format(value));
        if(value == null){LOG.info("null value in LocalDateTime Serializer");}
        LOG.info(value.toString(formatter));
        gen.writeString(value.toString(formatter));
    }

}
