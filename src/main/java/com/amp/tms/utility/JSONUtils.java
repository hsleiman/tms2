/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.utility;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 *
 * @author raine.cabal
 */
public class JSONUtils {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }

    public static String objectToJSON(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    public static Object JSONToObject(String json, Class<?> clazz) throws IOException {
        return mapper.readValue(json, clazz);
    }

    public static void objectToJSONFile(File file, Object object) throws IOException {
        mapper.writeValue(file, object);
    }

    public static Object JSONFileToObject(File file, Class<?> clazz) throws IOException {
        return mapper.readValue(file, clazz);
    }

    public static String mapToJSON(Map<String, String> map) throws IOException {
        return mapper.writeValueAsString(map);
    }

    public static Map<String, Object> JSONToMap(String json) throws IOException {
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
}
