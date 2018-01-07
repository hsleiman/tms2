/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.entity;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.util.Objects;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class PrimaryCall implements DataSerializable{

    private long queuePk;
    private String callUUID;

    private PrimaryCall() {
    }

    public PrimaryCall(long queuePk, String callUUID) {
        this.queuePk = queuePk;
        this.callUUID = callUUID;
    }

    public long getQueuePk() {
        return queuePk;
    }

    public String getCallUUID() {
        return callUUID;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (int) (this.queuePk ^ (this.queuePk >>> 32));
        hash = 29 * hash + Objects.hashCode(this.callUUID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PrimaryCall other = (PrimaryCall) obj;
        if (this.queuePk != other.queuePk) {
            return false;
        }
        if (!Objects.equals(this.callUUID, other.callUUID)) {
            return false;
        }
        return true;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(queuePk);
        out.writeUTF(callUUID);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        queuePk = in.readLong();
        callUUID = in.readUTF();
    }

}
