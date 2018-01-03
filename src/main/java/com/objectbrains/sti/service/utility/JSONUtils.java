/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.objectbrains.sti.pojo.OutboundDialerQueueRecord;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author David
 */
public class JSONUtils {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static String objectToJSON(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    @SuppressWarnings("unchecked")
    public static <T> T JSONToObject(String json, Class<T> clazz) throws IOException {
        if (clazz == null || clazz.isAssignableFrom(String.class)) return (T) json;
        if (StringUtils.isBlank(json)) return null;
        return mapper.readValue(StringEscapeUtils.unescapeJson(json), clazz);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> JSONToObjectList(String json, Class<T> elementClass) throws IOException {
        if (StringUtils.isBlank(json)) return (List<T>) Collections.EMPTY_LIST;
        return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, elementClass));
    }

    public static void objectToJSONFile(File file, Object object) throws IOException {
        mapper.writeValue(file, object);
    }

    public static <T> T JSONFileToObject(File file, Class<T> clazz) throws IOException {
        return mapper.readValue(file, clazz);
    }

    public static String mapToJSON(Map<String, String> map) throws IOException {
        return mapper.writeValueAsString(map);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> JSONToMap(String json) throws IOException {
        if (StringUtils.isBlank(json)) return (Map<String,Object>) Collections.EMPTY_MAP;
        return mapper.readValue(json, 
                new TypeReference<HashMap<String, Object>>(){});
    }

    public static void mapToJSONFile(Map<String, Object> map, File file) throws IOException {
        mapper.writeValue(file, map);
    }

    public static Map<String, Object> JSONFileToMap(File file) throws IOException {
        return mapper.readValue(file, 
                new TypeReference<HashMap<String, Object>>() {});
    }
    
    public static <T> T convertValue(Object obj, Class<T> clazz){
        return mapper.convertValue(obj, clazz);
    }


    public static void main(String[] args) throws IOException {
        OutboundDialerQueueRecord dqRecord = new OutboundDialerQueueRecord();
        dqRecord.setDqPk(1);
        System.out.println(objectToJSON(dqRecord));
    }
}
