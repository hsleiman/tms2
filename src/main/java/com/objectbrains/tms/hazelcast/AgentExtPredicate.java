/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast;

import com.objectbrains.tms.hazelcast.keys.AgentQueueKey;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.query.Predicate;
import com.objectbrains.tms.hazelcast.entity.WeightedPriority;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author connorpetty
 */
public class AgentExtPredicate implements Predicate<AgentQueueKey, WeightedPriority>, DataSerializable {

    private int ext;

    private AgentExtPredicate() {
    }

    public AgentExtPredicate(int ext) {
        this.ext = ext;
    }

    @Override
    public boolean apply(Map.Entry<AgentQueueKey, WeightedPriority> mapEntry) {
        return mapEntry.getKey().getExtension() == ext;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(ext);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        ext = in.readInt();
    }

}
