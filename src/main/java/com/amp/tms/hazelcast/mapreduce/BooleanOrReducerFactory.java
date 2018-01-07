/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapreduce;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;

/**
 *
 * @author Hoang, J, Bishistha
 * @param <Key>
 */
public class BooleanOrReducerFactory<Key> implements ReducerFactory<Key, Boolean, Boolean>, DataSerializable {

    @Override
    public Reducer<Boolean, Boolean> newReducer(Key key) {
        return new BooleanOrReducer();
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

class BooleanOrReducer extends Reducer<Boolean, Boolean> {

    private volatile boolean result = false;

    @Override
    public void reduce(Boolean value) {
        result |= value;
    }

    @Override
    public Boolean finalizeReduce() {
        return result;
    }

}
