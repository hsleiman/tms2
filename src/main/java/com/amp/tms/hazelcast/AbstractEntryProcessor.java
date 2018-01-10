/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast;

import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.util.Map;

/**
 *
 * 
 */
public abstract class AbstractEntryProcessor<K, V>
        implements EntryProcessor<K, V>, EntryBackupProcessor<K, V>, DataSerializable {

    private boolean applyOnBackup;

    /**
     * Creates an AbstractEntryProcessor that applies the
     * {@link #process(java.util.Map.Entry)} to primary and backups.
     */
    public AbstractEntryProcessor() {
        this(true);
    }

    /**
     * Creates an AbstractEntryProcessor.
     *
     * @param applyOnBackup true if the {@link #process(java.util.Map.Entry)}
     * should also be applied on the backup.
     */
    public AbstractEntryProcessor(boolean applyOnBackup) {
        this.applyOnBackup = applyOnBackup;
    }

    @Override
    public final EntryBackupProcessor<K, V> getBackupProcessor() {
        if (applyOnBackup) {
            return this;
        } else {
            return null;
        }
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeBoolean(applyOnBackup);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        applyOnBackup = in.readBoolean();
    }

    @Override
    public final Object process(Map.Entry<K, V> entry) {
        return process(entry, true);
    }

    @Override
    public final void processBackup(Map.Entry<K, V> entry) {
        process(entry, false);
    }

    protected abstract Object process(Map.Entry<K, V> entry, boolean isPrimary);

}
