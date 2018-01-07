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
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class LocalDateTimeDeserializer  extends StdDeserializer<LocalDateTime> {
    
    public static final Logger LOG = LoggerFactory.getLogger(LocalDateTimeDeserializer.class);
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm");
    
    public LocalDateTimeDeserializer() {
        this(null);
    }

    public LocalDateTimeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonparser, DeserializationContext context) throws IOException, JsonProcessingException {
        String date = jsonparser.getText();
        LOG.info(date);
        return LocalDateTime.parse(date, formatter);
    }
    
}
