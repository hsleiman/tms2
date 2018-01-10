/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.hazelcast.keys.AgentQueueKey;
import com.hazelcast.query.Predicate;
import com.amp.tms.hazelcast.entity.WeightedPriority;
import java.io.IOException;
import java.util.Map;

/**
 *
 * 
 */
public class AgentsInQueuePredicate implements Predicate<AgentQueueKey, WeightedPriority>, DataSerializable {

    private long queuePk;

    private AgentsInQueuePredicate() {
    }

    public AgentsInQueuePredicate(long queuePk) {
        this.queuePk = queuePk;
    }

    @Override
    public boolean apply(Map.Entry<AgentQueueKey, WeightedPriority> mapEntry) {
        return queuePk == mapEntry.getKey().getQueuePk();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(queuePk);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        queuePk = in.readLong();
    }
}
