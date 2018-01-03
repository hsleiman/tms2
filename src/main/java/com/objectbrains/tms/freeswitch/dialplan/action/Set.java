/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.dialplan.action;

import com.objectbrains.tms.enumerated.CallDirection;

/**
 *
 * @author hsleiman
 */
public class Set extends AbstractAction {

    public Set(String name, String data) {
        super("set", name, data);
    }
    
    public Set(String data) {
        super("set", data);
    }

    public static Set create(String key, CallDirection callDirection){
        return new Set(key, callDirection.name());
    }
    
    public static Set create(String key, String value) {
        return new Set(key, value);
    }

    public static Set create(String key, Boolean value) {
        if (value) {
            return new Set(key, "true");
        } else {
            return new Set(key, "false");
        }
    }

    public static Set create(String key, Integer value) {
        return new Set(key, value + "");
    }

    public static Set create(String key, Double value) {
        return new Set(key, value + "");
    }

    public static Set create(String key, Long value) {
        return new Set(key, value + "");
    }
}
