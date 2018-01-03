/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author connorpetty
 * @param <T>
 */
public class QueueAdapter<T> extends LinkedList<T> implements DataSerializable {

    public QueueAdapter() {
    }

    public QueueAdapter(Collection<? extends T> c) {
        super(c);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(size());
        for (T thi : this) {
            out.writeObject(thi);
        }
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        List<T> list = new ArrayList<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            list.add(in.<T>readObject());
        }
        addAll(list);
    }

}
