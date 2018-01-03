/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.entity;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.util.Objects;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author connorpetty
 */
@MappedSuperclass
public class WeightedPriority implements DataSerializable {

    private Integer priority;

    private Double weight;

    public WeightedPriority() {
    }

    public WeightedPriority(Integer priority, Double weight) {
        this.priority = priority;
        this.weight = weight;
    }

    public WeightedPriority(com.objectbrains.svc.iws.WeightedPriority weightedPriority) {
        copyFrom(weightedPriority);
    }

    public WeightedPriority(WeightedPriority copy) {
        copyFrom(copy);
    }

    public final void copyFrom(com.objectbrains.svc.iws.WeightedPriority copy) {
        priority = copy.getPriority();
        if (copy.getWeight() != null) {
            this.weight = copy.getWeight().doubleValue();
        } else {
            this.weight = null;
        }
    }

    public final void copyFrom(WeightedPriority copy) {
        priority = copy.priority;
        weight = copy.weight;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public boolean valueEquals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WeightedPriority other = (WeightedPriority) obj;
        if (!Objects.equals(this.priority, other.priority)) {
            return false;
        }
        if (!Objects.equals(this.weight, other.weight)) {
            return false;
        }
        return true;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        int nulls = 0;
        if (priority != null) {
            nulls |= 1;
        }
        if (weight != null) {
            nulls |= 2;
        }
        out.writeByte(nulls);
        if (priority != null) {
            out.writeInt(priority);
        }
        if (weight != null) {
            out.writeDouble(weight);
        }
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        int nulls = in.readByte();
        priority = (nulls & 1) != 0 ? in.readInt() : null;
        weight = (nulls & 2) != 0 ? in.readDouble() : null;
    }

}
