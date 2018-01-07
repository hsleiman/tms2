/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author HS
 */
public class MapAdapter<K, V> extends HashMap<K, V> implements DataSerializable {

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(size());
        for (Map.Entry<K, V> entrySet : this.entrySet()) {
            K key = entrySet.getKey();
            V value = entrySet.getValue();
            out.writeObject(key);
            out.writeObject(value);
        }
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            K key = in.readObject();
            V value = in.readObject();
            this.put(key, value);
        }
    }

}
