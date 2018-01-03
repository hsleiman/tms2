/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.enumerated;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.objectbrains.svc.iws.DialerMode;
import com.objectbrains.svc.iws.SvDialerQueueSettings;
import com.objectbrains.svc.iws.SvInboundDialerQueueSettings;
import com.objectbrains.svc.iws.SvOutboundDialerQueueSettings;
import java.io.IOException;

/**
 *
 * @author connorpetty
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

    public static DialerType valueFrom(SvDialerQueueSettings settings) {
        if (settings == null) {
            return null;
        }
        if (settings instanceof SvInboundDialerQueueSettings) {
            return INBOUND;
        } else {
            DialerMode mode = ((SvOutboundDialerQueueSettings) settings).getDialerMode();
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
