/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import java.io.IOException;

public enum DialerState {

        INIT,
        RUNNING,
        IDLE,
        PAUSED,
        STOPPED,
        COMPLETED;

        public static void write(ObjectDataOutput out, DialerState state) throws IOException {
            if (state == null) {
                out.writeByte(-1);
            } else {
                out.writeByte(state.ordinal());
            }
        }

        public static DialerState read(ObjectDataInput in) throws IOException {
            byte ordinal = in.readByte();
            if (ordinal == -1) {
                return null;
            }
            return DialerState.values()[ordinal];
        }
    }
