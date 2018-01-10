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
public class QueueAverages implements DataSerializable {

    private double averageTimeBetweenCallArrivalsMillis;
    private double averageCallLengthMillis;

    public QueueAverages() {
    }

    public QueueAverages(double averageTimeBetweenCallArivals, double averageCallLength) {
        this.averageTimeBetweenCallArrivalsMillis = averageTimeBetweenCallArivals;
        this.averageCallLengthMillis = averageCallLength;
    }

    public double getAverageTimeBetweenCallArrivalsMillis() {
        return averageTimeBetweenCallArrivalsMillis;
    }

    public void setAverageTimeBetweenCallArrivalsMillis(double averageTimeBetweenCallArrivalsMillis) {
        this.averageTimeBetweenCallArrivalsMillis = averageTimeBetweenCallArrivalsMillis;
    }

    public double getAverageCallLengthMillis() {
        return averageCallLengthMillis;
    }

    public void setAverageCallLengthMillis(double averageCallLengthMillis) {
        this.averageCallLengthMillis = averageCallLengthMillis;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeDouble(averageTimeBetweenCallArrivalsMillis);
        out.writeDouble(averageCallLengthMillis);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        averageTimeBetweenCallArrivalsMillis = in.readDouble();
        averageCallLengthMillis = in.readDouble();
    }

}
