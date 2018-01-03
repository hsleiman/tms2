/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.mapreduce;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;

/**
 *
 * @author connorpetty
 * @param <Key>
 */
public class BooleanAndCombinerFactory<Key> implements CombinerFactory<Key, Boolean, Boolean>, DataSerializable {

    @Override
    public Combiner<Boolean, Boolean> newCombiner(Key key) {
        return new BooleanAndCombiner();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        //do nothing
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        //do nothing
    }

}

class BooleanAndCombiner extends Combiner<Boolean, Boolean> {

    private volatile boolean result = true;

    @Override
    public void combine(Boolean value) {
        result &= value;
    }

    @Override
    public Boolean finalizeChunk() {
        return result;
    }

    @Override
    public void reset() {
        result = true;
    }

}
