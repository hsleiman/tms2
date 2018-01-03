/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.constants;


public enum PreviewDialerType {
    
    REGULAR(0),
    ACCEPT_SKIP(1),
    SELECT_PHONE(2),
    DELAY_CALL(3);
    
    private int code;
    
    private PreviewDialerType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
    
    public String value() {
        return name();
    }

    public static PreviewDialerType fromValue(String v) {
        return valueOf(v);
    }
    
}
