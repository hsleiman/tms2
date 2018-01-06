/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

/**
 *
 * @author Bishistha
 */
public enum DialerMode {
    
    REGULAR(0),
    PREVIEW(1),
    PROGRESSIVE(2),
    PREDICTIVE(3),
    VOICE(4);
    
    private final int mode;
    
    private DialerMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
    
    
    public String value() {
        return name();
    }


    public static DialerMode fromValue(String v) {
        return valueOf(v);
    }
   
}
