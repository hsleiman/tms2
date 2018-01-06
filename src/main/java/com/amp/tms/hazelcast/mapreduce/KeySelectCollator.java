/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapreduce;

import com.hazelcast.mapreduce.Collator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author connorpetty
 * @param <Key>
 */
public class KeySelectCollator<Key> implements Collator<Map.Entry<Key, Boolean>, Set<Key>> {

    @Override
    public Set<Key> collate(Iterable<Map.Entry<Key, Boolean>> values) {
        Set<Key> extensions = new HashSet<>();
        for (Map.Entry<Key, Boolean> value : values) {
            if (value.getValue()) {
                extensions.add(value.getKey());
            }
        }
        return extensions;
    }

}
