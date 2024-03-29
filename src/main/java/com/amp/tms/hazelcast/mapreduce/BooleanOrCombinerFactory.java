/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapreduce;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;

/**
 *
 * 
 * @param <Key>
 */
public class BooleanOrCombinerFactory<Key> implements CombinerFactory<Key, Boolean, Boolean>, DataSerializable {

    @Override
    public Combiner<Boolean, Boolean> newCombiner(Key key) {
        return new BooleanOrCombiner();
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

class BooleanOrCombiner extends Combiner<Boolean, Boolean> {

    private volatile boolean result = false;

    @Override
    public void combine(Boolean value) {
        result |= value;
    }

    @Override
    public Boolean finalizeChunk() {
        return result;
    }

    @Override
    public void reset() {
        result = false;
    }

}
