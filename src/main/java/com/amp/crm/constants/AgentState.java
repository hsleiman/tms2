/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import java.io.IOException;

public enum AgentState {

    OFFLINE(false),
    ONCALL(true),
    IDLE(true),
    HOLD(true),
    PREVIEW(true),
    WRAP(true),
    //not ready
    MEETING(false),
    BREAK(false),
    FORCE(false),
    LUNCH(false);
    private final boolean readyState;

    private AgentState(boolean readyState) {
        this.readyState = readyState;
    }

    public boolean isReadyState() {
        return readyState;
    }

    public static void write(ObjectDataOutput out, AgentState state) throws IOException {
        if (state == null) {
            out.writeByte(-1);
        } else {
            out.writeByte(state.ordinal());
        }
    }

    public static AgentState read(ObjectDataInput in) throws IOException {
        byte ordinal = in.readByte();
        if (ordinal == -1) {
            return null;
        }
        return AgentState.values()[ordinal];
    }
}

