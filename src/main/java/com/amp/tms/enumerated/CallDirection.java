/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.enumerated;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import java.io.IOException;

/**
 *
 * @author connorpetty
 */
public enum CallDirection {

    INTERNAL(com.amp.crm.constants.CallDirection.INTERNAL),
    INBOUND(com.amp.crm.constants.CallDirection.INBOUND),
    OUTBOUND(com.amp.crm.constants.CallDirection.OUTBOUND);

    private final com.amp.crm.constants.CallDirection svcCallDirection;

    private CallDirection(com.amp.crm.constants.CallDirection svcCallDirection) {
        this.svcCallDirection = svcCallDirection;
    }

    public com.amp.crm.constants.CallDirection getSvcCallDirection() {
        return svcCallDirection;
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
