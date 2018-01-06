/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import com.hazelcast.mapreduce.KeyPredicate;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.hazelcast.keys.AgentQueueKey;
import java.io.IOException;

/**
 *
 * @author connorpetty
 */
public class AgentsInQueueKeyPredicate implements KeyPredicate<AgentQueueKey>, DataSerializable {

    private long queuePk;

    public AgentsInQueueKeyPredicate() {
    }

    public AgentsInQueueKeyPredicate(long queuePk) {
        this.queuePk = queuePk;
    }

    @Override
    public boolean evaluate(AgentQueueKey key) {
        return key.getQueuePk() == queuePk;
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
