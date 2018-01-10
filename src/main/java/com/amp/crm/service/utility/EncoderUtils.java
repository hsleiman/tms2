/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.utility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * 
 */
public class EncoderUtils {

    /* Decodes a Base64 encoded byte array of list of string */
    public static List<String> decodeBase64List(String byteString) throws IOException {
        List<String> list = new ArrayList<>();
        if (byteString != null) {            
            byte[] byteArray = DatatypeConverter.parseBase64Binary(byteString);
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                @SuppressWarnings("unchecked")
                ArrayList<String> dataList = (ArrayList<String>) ois.readObject();
                list = dataList;
            } catch (ClassNotFoundException ex) {
            }
        }
        return list;
    }

    /* Decodes a Base64 encoded byte array of string */
    public static String decodeBase64(String byteString) {
        if (byteString == null) {
            return null;
        }
        byte[] bytes = DatatypeConverter.parseBase64Binary(byteString);
        return new String(bytes);
    }

    public static String encodeBase64String(String s) {
        if (s == null) {
            return null;
        }
        String encodedStr = DatatypeConverter.printBase64Binary(s.getBytes());
        return encodedStr;
    }

    public static String encodeUrl(Object value, String encoding) throws UnsupportedEncodingException {
        if (value == null) {
            return null;
        }
        if (StringUtils.isBlank(encoding)) {
            return value.toString();
        }
        return URLEncoder.encode(value.toString(), encoding);
    }

    public static String decodeUrl(String value, String encoding) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(encoding) || value == null) {
            return value;
        }
        return URLDecoder.decode(value, encoding);
    }

}

