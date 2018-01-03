/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.websocket.message.outbound;

import com.google.gson.annotations.Expose;
import com.hazelcast.mapreduce.aggregation.impl.SetAdapter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.objectbrains.tms.pojo.AgentDirectory;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author hsleiman
 */
public class UpdateDirectory implements DataSerializable {
    
    @Expose
    private SetAdapter<AgentDirectory> directories = new SetAdapter<>();
    
    public Set<AgentDirectory> getDirectories() {
        return directories;
    }
    
    public void setDirectories(Set<AgentDirectory> directories) {
        this.directories.clear();
        this.directories.addAll(directories);
    }
    
    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        directories.writeData(out);
    }
    
    @Override
    public void readData(ObjectDataInput in) throws IOException {
        directories.readData(in);
    }
    
}
