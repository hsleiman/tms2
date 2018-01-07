/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import java.io.IOException;

/**
 *
 * @author Hoang, J, Bishistha
 */
public enum CallState {
    //these are ordered by their influence on the agent state
    //from most influencial to least influencial.
    TRANSFERRING(AgentState.ONCALL),
    ACTIVE(AgentState.ONCALL),
    RINGING(AgentState.ONCALL),
    HOLD(AgentState.HOLD),
    PREVIEW(AgentState.PREVIEW),
    WRAP(AgentState.WRAP),
    DONE(AgentState.WRAP);

    private final AgentState impliedAgentState;

    private CallState(AgentState impliedAgentState) {
        this.impliedAgentState = impliedAgentState;
    }

    public AgentState getImpliedAgentState() {
        return impliedAgentState;
    }

    public static void write(ObjectDataOutput out, CallState state) throws IOException {
        if (state == null) {
            out.writeByte(-1);
        } else {
            out.writeByte(state.ordinal());
        }
    }

    public static CallState read(ObjectDataInput in) throws IOException {
        byte ordinal = in.readByte();
        if (ordinal == -1) {
            return null;
        }
        return CallState.values()[ordinal];
    }
}
