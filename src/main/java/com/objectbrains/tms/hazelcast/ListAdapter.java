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
import java.util.List;

/**
 *
 * @author connorpetty
 * @param <E>
 */
public class ListAdapter<E> extends ArrayList<E> implements DataSerializable {

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(size());
        for (E thi : this) {
            out.writeObject(thi);
        }
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        int size = in.readInt();
        List<E> tempList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            tempList.add(in.<E>readObject());
        }
        addAll(tempList);
    }

}
