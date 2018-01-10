/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.utility;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * 
 */
public class JodaUtility {
    static final DateTimeFormatter DATE_FMT = DateTimeFormat.forPattern("yyyy-MM-dd");
    
    public static LocalDate stringToDate(String yyyymmdd) {
        return DATE_FMT.parseLocalDate(yyyymmdd);
    }
    
    public static String dateToString(LocalDate localDate) {
        return DATE_FMT.print(localDate);
    }
}
