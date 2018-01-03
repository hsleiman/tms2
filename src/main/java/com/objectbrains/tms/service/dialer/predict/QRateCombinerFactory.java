/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer.predict;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author connorpetty
 */
public class QRateCombinerFactory implements CombinerFactory<QRateKey, QSums, List<QSums>> {

    @Override
    public Combiner<QSums, List<QSums>> newCombiner(QRateKey key) {
        return new QRateCombinerCombiner();
    }

}

class QRateCombinerCombiner extends Combiner<QSums, List<QSums>> {

    private final Map<Integer, QSums> sumsMap = new HashMap<>();

    @Override
    public void combine(QSums value) {
        QSums sums = sumsMap.get(value.getPriority());
        if (sums != null) {
            sums.add(value);
        } else {
            sumsMap.put(value.getPriority(), value);
        }
    }

    @Override
    public List<QSums> finalizeChunk() {
        return new ArrayList<>(sumsMap.values());
    }

    @Override
    public void reset() {
        sumsMap.clear();
    }

}
