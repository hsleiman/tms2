/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.enumerated.AgentState;
import java.io.IOException;

/**
 *
 * @author HS
 */
public class AgentCallState implements DataSerializable {

    private boolean cannotReceive;
    private boolean hasCalls;

    public AgentCallState() {
        hasCalls = false;
        cannotReceive = false;
    }
    
    public final void copyFrom(AgentCallState copy){
        this.cannotReceive = copy.cannotReceive;
        this.hasCalls = copy.hasCalls;
    }

    public boolean cannotReceive() {
        return cannotReceive;
    }

    public void setCannotReceive(boolean cannotReceive) {
        this.cannotReceive = cannotReceive;
    }

    public boolean hasCalls() {
        return hasCalls;
    }

    public void setHasCalls(boolean hasCalls) {
        this.hasCalls = hasCalls;
    }

    protected int writeBooleans() {
        int booleans = 0;
        booleans |= cannotReceive ? 1 : 0;
        booleans |= hasCalls ? 2 : 0;
        return booleans;
    }

    protected void readBooleans(int booleans) {
        cannotReceive = (booleans & 1) > 0;
        hasCalls = (booleans & 2) > 0;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(writeBooleans());
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        readBooleans(in.readInt());
    }

}
