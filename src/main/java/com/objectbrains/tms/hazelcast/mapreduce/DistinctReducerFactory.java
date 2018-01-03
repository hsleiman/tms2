/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.mapreduce;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;
import com.hazelcast.mapreduce.aggregation.impl.SetAdapter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author connorpetty
 * @param <Key>
 * @param <Value>
 */
public class DistinctReducerFactory<Key, Value> implements ReducerFactory<Key, Set<Value>, Set<Value>>, DataSerializable {

    @Override
    public Reducer<Set<Value>, Set<Value>> newReducer(Key key) {
        return new DistinctReducer<>();
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

class DistinctReducer<Value> extends Reducer<Set<Value>, Set<Value>> {

    private final Set<Value> values = new ConcurrentSkipListSet<>();

    @Override
    public void reduce(Set<Value> value) {
        values.addAll(value);
    }

    @Override
    public Set<Value> finalizeReduce() {
        Set<Value> results = new SetAdapter<>();
        results.addAll(values);
        return results;
    }

}
