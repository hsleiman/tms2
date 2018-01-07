/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import com.hazelcast.mapreduce.KeyPredicate;
import com.hazelcast.mapreduce.aggregation.impl.SetAdapter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.hazelcast.keys.AgentQueueKey;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class AgentQueueKeyPredicate implements KeyPredicate<AgentQueueKey>, DataSerializable {

    private Set<Integer> agentExtensions;

    public AgentQueueKeyPredicate() {
    }

    public AgentQueueKeyPredicate(Set<Integer> agentExtensions) {
        this.agentExtensions = new SetAdapter<>();
        this.agentExtensions.addAll(agentExtensions);
    }

    @Override
    public boolean evaluate(AgentQueueKey key) {
        return agentExtensions.contains(key.getExtension());
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(agentExtensions);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        agentExtensions = in.readObject();
    }

}
