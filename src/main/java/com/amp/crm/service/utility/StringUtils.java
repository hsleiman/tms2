/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static String getLastnCharacters(String str, int substringLength) {
        if (str == null) {
            return null;
        }
        return str.substring(Math.max(str.length() - substringLength, 0));
    }

    public static String getFirstNCharacters(String str, int substringLength) {
        if (str == null) {
            return null;
        }
        return str.substring(0, Math.min(str.length(), substringLength));
    }

    public static Double parseDouble(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        return Double.parseDouble(s);
    }

    public static Integer parseInt(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        return Integer.parseInt(s);
    }

    public static Long parseLong(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        return Long.parseLong(s);
    }

    public static Boolean parseBoolean(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        return Boolean.parseBoolean(s);
    }

    public static String valueOf(Object o) {
        if (o == null) {
            return null;
        }
        return String.valueOf(o);
    }

    /*
     * Returns true if searchObj is null or equals obj
     */
    public static boolean equalsOrNull(String string, String searchStr, boolean ignoreCase) {
        if (searchStr != null) {
            if (ignoreCase) {
                return searchStr.equalsIgnoreCase(string);
            }
            return searchStr.equals(string);
        }
        return true;
    }
    
    /*
     * Returns true if searchObj is null or equals obj
     */
    public static boolean equalsOrNull(Object obj, Object searchObj) {
        if (searchObj != null) {
            return searchObj.equals(obj);
        }
        return true;
    }

    /*
     * Returns true if searchStr is null or str contains searchStr, similar to like in sql
     */
    public static boolean containsOrNull(String str, String searchStr, boolean ignoreCase) {
        if (searchStr != null) {
            if (str == null) {
                return false;
            }
            if (ignoreCase) {
                return StringUtils.containsIgnoreCase(str, searchStr);
            }
            return str.contains(searchStr);
        }
        return true;
    }

    public static List<String> findMatches(String searchFor, String searchAt) {
        List<String> list = new ArrayList<>();
        if (searchFor != null && searchAt != null) {
            Matcher matcher = Pattern.compile(searchFor).matcher(searchAt);
            while (matcher.find()) {
                if (!list.contains(matcher.group())) {
                    list.add(matcher.group());
                }
            }
        }
        return list;
    }

    public static List<String> findMatchesIgnoreCase(String searchFor, String searchAt) {
        List<String> list = new ArrayList<>();
        if (searchFor != null && searchAt != null) {
            Matcher matcher = Pattern.compile(searchFor, Pattern.CASE_INSENSITIVE).matcher(searchAt);
            while (matcher.find()) {
                if (!StringUtils.containsIgnoreCase(list, matcher.group())) {
                    list.add(matcher.group());
                }
            }
        }
        return list;
    }

    public static boolean containsIgnoreCase(List<String> list, String searchStr) {
        if (list == null) {
            return false;
        }
        for (String string : list) {
            if (string.equalsIgnoreCase(searchStr)) {
                return true;
            }
        }
        return false;
    }
    
    public static String convertObjectToMapString(Object object){
        if (object == null) return null;
        return JSONUtils.convertValue(object, Map.class).toString();
    }
    
     public static String getStackTraceString(Throwable th) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        return sw.toString();
    }

}
