/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.constants;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Bishistha
 */
public enum CallDirection {
   
    INTERNAL,
    INBOUND,
    OUTBOUND;
    
    public static CallDirection getCallDirectionByName(String name) {
        if (StringUtils.isBlank(name)) return null;
        for (CallDirection c : CallDirection.values()) {
            if (c.name().trim().toLowerCase().equals(name.trim().toLowerCase())) {
                return c;
            }
        }
        return null;
    }
    
    public static void write(ObjectDataOutput out, CallDirection state) throws IOException {
        if (state == null) {
            out.writeByte(-1);
        } else {
            out.writeByte(state.ordinal());
        }
    }

    public static CallDirection read(ObjectDataInput in) throws IOException {
        byte ordinal = in.readByte();
        if (ordinal == -1) {
            return null;
        }
        return CallDirection.values()[ordinal];
    }
    
}