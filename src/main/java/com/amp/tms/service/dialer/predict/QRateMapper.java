/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import com.amp.tms.hazelcast.keys.AgentQueueKey;
import java.io.IOException;
import java.util.Map;

/**
 *
 * 
 */
public class QRateMapper implements
        Mapper<AgentQueueKey, AgentWeightedPriority, QRateKey, QSums>,
        DataSerializable {

    private Map<Long, QueueAverages> queueAverages;
    private Map<Integer, AgentWeightedPriority> agentWeightedPriorities;

    public QRateMapper() {
    }

    public QRateMapper(Map<Long, QueueAverages> queueAverages, Map<Integer, AgentWeightedPriority> agentWeightedPriorities) {
        this.queueAverages = queueAverages;
        this.agentWeightedPriorities = agentWeightedPriorities;
    }

    @Override
    public void map(AgentQueueKey key, AgentWeightedPriority value, Context<QRateKey, QSums> context) {
        int ext = key.getExtension();
        if (agentWeightedPriorities.containsKey(ext)) {
            QSums sums = new QSums();
            sums.setPriority(value.getPriority());

            QueueAverages averages = queueAverages.get(key.getQueuePk());
            if(averages == null){
                return;
            }
            sums.setResponseRateSum(1.0 / averages.getAverageTimeBetweenCallArrivalsMillis());
            sums.setWeightLengthSum(averages.getAverageCallLengthMillis() * value.getWeight());
            sums.setWeightSum(value.getWeight());
            context.emit(new QRateKey(ext, agentWeightedPriorities.get(ext)), sums);
        }
    }

    @Override
    public void writeData(ObjectDataOutput out)
            throws IOException {
        out.writeObject(queueAverages);
        out.writeObject(agentWeightedPriorities);
    }

    @Override
    public void readData(ObjectDataInput in)
            throws IOException {
        queueAverages = in.readObject();
        agentWeightedPriorities = in.readObject();
    }

}
