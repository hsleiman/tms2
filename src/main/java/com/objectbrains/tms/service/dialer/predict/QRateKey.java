/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer.predict;

import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.objectbrains.tms.hazelcast.entity.WeightedPriority;
import java.io.IOException;

/**
 *
 * @author connorpetty
 */
public class QRateKey implements PartitionAware<Integer>, DataSerializable {

    private int agentExtension;
    private int priority;
    private double weight;

    public QRateKey() {
    }

    public QRateKey(int agentExtension, WeightedPriority weightedPriority) {
        this.agentExtension = agentExtension;
        this.priority = weightedPriority.getPriority();
        this.weight = weightedPriority.getWeight();
    }

    public int getAgentExtension() {
        return agentExtension;
    }

    public void setAgentExtension(int agentExtension) {
        this.agentExtension = agentExtension;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public Integer getPartitionKey() {
        return agentExtension;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(agentExtension);
        out.writeInt(priority);
        out.writeDouble(weight);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        agentExtension = in.readInt();
        priority = in.readInt();
        weight = in.readDouble();
    }

}
