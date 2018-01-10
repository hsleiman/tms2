/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

public enum CallerId {

    ACTUAL,
    CUSTOM,
    BLOCK_CALLER_ID;

    public String value() {
        return name();
    }

    public static CallerId fromValue(String v) {
        return valueOf(v);
    }

}
