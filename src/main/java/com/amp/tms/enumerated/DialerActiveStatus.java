/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.enumerated;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hsleiman
 */
public enum DialerActiveStatus {

    INACTIVE(0),
    ACTIVE(1);


    private final int id;

    DialerActiveStatus(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }
    
    private static final Map<Integer, DialerActiveStatus> lookup = new HashMap<Integer, DialerActiveStatus>();
    
    public static DialerActiveStatus get(int value) {
        switch (value) {
            case 0:
                return INACTIVE;
            case 1:
                return ACTIVE;
        }
        return null;
    }
}
