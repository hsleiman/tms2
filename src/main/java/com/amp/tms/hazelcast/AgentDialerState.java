/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.amp.tms.enumerated.AgentState;
import java.io.IOException;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class AgentDialerState extends AgentCallState {

    private boolean dialerActive;
    private AgentState state;

    public AgentDialerState() {
        dialerActive = false;
        state = AgentState.OFFLINE;
    }

    public boolean isDialerActive() {
        return dialerActive;
    }

    public void setDialerActive(boolean dialerActive) {
        this.dialerActive = dialerActive;
    }

    public AgentState getState() {
        return state;
    }

    public void setState(AgentState state) {
        this.state = state;
    }

    @Override
    protected int writeBooleans() {
        int booleans = super.writeBooleans();
        booleans |= dialerActive ? 4 : 0;
        return booleans;
    }

    @Override
    protected void readBooleans(int booleans) {
        super.readBooleans(booleans);
        dialerActive = (booleans & 4) > 0;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        AgentState.write(out, state);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        state = AgentState.read(in);
    }

}
