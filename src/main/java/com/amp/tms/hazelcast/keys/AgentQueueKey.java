/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.keys;

import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 *
 * 
 */
@Embeddable
public class AgentQueueKey implements PartitionAware<Integer>, Serializable, DataSerializable {

    private int extension;
    private long queuePk;

    public AgentQueueKey() {
    }

    public AgentQueueKey(int extension, long queuePk) {
        this.extension = extension;
        this.queuePk = queuePk;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + this.extension;
        hash = 61 * hash + (int) (this.queuePk ^ (this.queuePk >>> 32));
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
        final AgentQueueKey other = (AgentQueueKey) obj;
        if (this.extension != other.extension) {
            return false;
        }
        if (this.queuePk != other.queuePk) {
            return false;
        }
        return true;
    }

    @Override
    public Integer getPartitionKey() {
        return extension;
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

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(extension);
        out.writeLong(queuePk);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        extension = in.readInt();
        queuePk = in.readLong();
    }

    @Override
    public String toString() {
        return "AgentQueueKey{" + "extension=" + extension + ", queuePk=" + queuePk + '}';
    }

}
