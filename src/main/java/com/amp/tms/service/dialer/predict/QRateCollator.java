/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import com.hazelcast.mapreduce.Collator;
import java.util.Map;

/**
 *
 * 
 */
public class QRateCollator implements Collator<Map.Entry<QRateKey, Double>, Double> {

    @Override
    public Double collate(Iterable<Map.Entry<QRateKey, Double>> values) {
        double totalValue = 0.0;
        int count = 0;
        for (Map.Entry<QRateKey, Double> value : values) {
            totalValue += value.getValue();
            count++;
        }
        return totalValue / count;
    }

}
