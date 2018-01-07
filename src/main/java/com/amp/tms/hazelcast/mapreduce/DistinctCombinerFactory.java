/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapreduce;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;
import com.hazelcast.mapreduce.aggregation.impl.SetAdapter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author Hoang, J, Bishistha
 * @param <Key>
 * @param <Value>
 */
public class DistinctCombinerFactory<Key, Value> implements CombinerFactory<Key, Value, Set<Value>>, DataSerializable {

    @Override
    public Combiner<Value, Set<Value>> newCombiner(Key key) {
        return new DistinctCombiner<>();
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

class DistinctCombiner<Value> extends Combiner<Value, Set<Value>> {

    private final Set<Value> values = new SetAdapter<>();

    @Override
    public void combine(Value value) {
        values.add(value);
    }

    @Override
    public Set<Value> finalizeChunk() {
        return values;
    }

    @Override
    public void reset() {
        values.clear();
    }

}
