/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.enumerated;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.amp.crm.constants.DialerMode;
import com.amp.crm.db.entity.base.dialer.DialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.InboundDialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.OutboundDialerQueueSettings;
import java.io.IOException;

/**
 *
 * 
 */
public enum DialerType {

    INBOUND, POWER, PROGRESSIVE, PREDICTIVE, BROADCAST;

    public static void write(ObjectDataOutput out, DialerType type) throws IOException {
        if (type == null) {
            out.writeByte(-1);
        } else {
            out.writeByte(type.ordinal());
        }
    }

    public static DialerType read(ObjectDataInput in) throws IOException {
        byte ordinal = in.readByte();
        if (ordinal == -1) {
            return null;
        }
        return DialerType.values()[ordinal];
    }

    public static DialerType valueFrom(DialerQueueSettings settings) {
        if (settings == null) {
            return null;
        }
        if (settings instanceof InboundDialerQueueSettings) {
            return INBOUND;
        } else {
            DialerMode mode = ((OutboundDialerQueueSettings) settings).getDialerMode();
            if (mode == null) {
                return null;
            }
            switch (mode) {
                case PREDICTIVE:
                    return DialerType.PREDICTIVE;
                case REGULAR:
                case PREVIEW:
                    return DialerType.POWER;
                case PROGRESSIVE:
                    return DialerType.PROGRESSIVE;
                case VOICE:
                    return DialerType.BROADCAST;
                default:
                    throw new IllegalArgumentException("Unknown DialerMode :" + mode.value());
            }
        }
    }
}
