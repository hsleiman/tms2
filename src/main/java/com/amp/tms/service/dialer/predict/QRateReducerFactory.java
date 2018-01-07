/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class QRateReducerFactory implements ReducerFactory<QRateKey, List<QSums>, Double> {

    @Override
    public Reducer<List<QSums>, Double> newReducer(QRateKey key) {
        return new QRateReducer(key);
    }

}

class QRateReducer extends Reducer<List<QSums>, Double> {

    private final QRateKey qRateKey;
    private final ConcurrentMap<Integer, QSums> sumsMap = new ConcurrentHashMap<>();

    public QRateReducer(QRateKey agentWeightedPriority) {
        this.qRateKey = agentWeightedPriority;
    }

    @Override
    public void reduce(List<QSums> partialSums) {
        for (QSums partialSum : partialSums) {
            QSums sums = sumsMap.putIfAbsent(partialSum.getPriority(), partialSum);
            if(sums != null){
                synchronized(sums){
                    sums.add(partialSum);
                }
            }
        }
    }

    @Override
    public Double finalizeReduce() {
        int keyPriority = qRateKey.getPriority();
        double keyWeight = qRateKey.getWeight();

        double numerator = 0.0;
        double denom = 0.0;

        for (Map.Entry<Integer, QSums> entrySet : sumsMap.entrySet()) {
            Integer key = entrySet.getKey();
            QSums value = entrySet.getValue();

            Integer priority = key;
            if (priority <= keyPriority) {
                double weightNumerator = priority < keyPriority
                        ? value.getWeightSum() : keyWeight;
                numerator += weightNumerator * value.getResponseRateSum() / value.getWeightLengthSum();
                denom += value.getResponseRateSum();
            }
        }

        return numerator / denom;
    }

}
