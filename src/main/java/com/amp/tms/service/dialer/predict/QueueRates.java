/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import java.util.Map;

/**
 *
 * 
 */
public class QueueRates {

//    private Map<Long, QueueAverages> queueAveragesMap;
    private double averageCustomerDropTime;
    private double averageCustomerResponseTime;
    private double callResponseProbability;

    public QueueRates(double averageCustomerDropTime, double averageCustomerResponseTime, double callResponseProbability) {
//        this.queueAveragesMap = queueAveragesMap;
        this.averageCustomerDropTime = averageCustomerDropTime;
        this.averageCustomerResponseTime = averageCustomerResponseTime;
        this.callResponseProbability = callResponseProbability;
    }

//    public Map<Long, QueueAverages> getQueueAveragesMap() {
//        return queueAveragesMap;
//    }

    public double getAverageCustomerDropTime() {
        return averageCustomerDropTime;
    }

    public double getAverageCustomerResponseTime() {
        return averageCustomerResponseTime;
    }

    public double getCallResponseProbability() {
        return callResponseProbability;
    }

}
