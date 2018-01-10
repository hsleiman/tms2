/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;

/**
 *
 * 
 */
public class QSums implements DataSerializable {

    private int priority;
    private double responseRateSum = 0;
    private double weightLengthSum = 0;
    private double weightSum = 0;

    public QSums() {
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getResponseRateSum() {
        return responseRateSum;
    }

    public void setResponseRateSum(double responseRateSum) {
        this.responseRateSum = responseRateSum;
    }

    public double getWeightLengthSum() {
        return weightLengthSum;
    }

    public void setWeightLengthSum(double weightLengthSum) {
        this.weightLengthSum = weightLengthSum;
    }

    public double getWeightSum() {
        return weightSum;
    }

    public void setWeightSum(double weightSum) {
        this.weightSum = weightSum;
    }

    public void add(QSums sums) {
        if (priority != sums.priority) {
            throw new IllegalArgumentException("QSum must have same priority");
        }
        responseRateSum += sums.responseRateSum;
        weightLengthSum += sums.responseRateSum;
        weightSum += sums.weightSum;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(priority);
        out.writeDouble(responseRateSum);
        out.writeDouble(weightLengthSum);
        out.writeDouble(weightSum);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        priority = in.readInt();
        responseRateSum = in.readDouble();
        weightLengthSum = in.readDouble();
        weightSum = in.readDouble();
    }
}
