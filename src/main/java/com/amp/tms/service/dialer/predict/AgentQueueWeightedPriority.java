/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import java.io.IOException;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class AgentQueueWeightedPriority implements DataSerializable {

    private int extension;
    private long queuePk;
    private AgentWeightedPriority weightedPriority;

    public AgentQueueWeightedPriority() {
    }

    public AgentQueueWeightedPriority(int extension, long queuePk, AgentWeightedPriority weightedPriority) {
        this.extension = extension;
        this.queuePk = queuePk;
        this.weightedPriority = new AgentWeightedPriority(weightedPriority);
    }

    public int getExtension() {
        return extension;
    }

    public void setExtension(int extension) {
        this.extension = extension;
    }

    public long getQueuePk() {
        return queuePk;
    }

    public void setQueuePk(long queuePk) {
        this.queuePk = queuePk;
    }

    public AgentWeightedPriority getWeightedPriority() {
        return weightedPriority;
    }

    public void setWeightedPriority(AgentWeightedPriority weightedPriority) {
        this.weightedPriority = weightedPriority;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.extension;
        hash = 83 * hash + (int) (this.queuePk ^ (this.queuePk >>> 32));
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
        final AgentQueueWeightedPriority other = (AgentQueueWeightedPriority) obj;
        if (this.extension != other.extension) {
            return false;
        }
        return this.queuePk == other.queuePk;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(extension);
        out.writeLong(queuePk);
        weightedPriority.writeData(out);
//        out.writeObject(weightedPriority);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        extension = in.readInt();
        queuePk = in.readLong();
        weightedPriority = new AgentWeightedPriority();
        weightedPriority.readData(in);
//        weightedPriority = in.readObject();
    }

}
