/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.websocket;

import com.hazelcast.mapreduce.aggregation.impl.SetAdapter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;
import com.objectbrains.tms.pojo.AgentDirectory;
import com.objectbrains.tms.websocket.message.Function;
import com.objectbrains.tms.websocket.message.outbound.Send;
import com.objectbrains.tms.websocket.message.outbound.UpdateDirectory;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author hsleiman
 */
@SpringAware
class SendUpdateOfDirectoryTask implements Runnable, DataSerializable {

    @Autowired
    private Websocket websocket;

    private final SetAdapter<AgentDirectory> directories = new SetAdapter<>();

    private SendUpdateOfDirectoryTask() {
    }

    public SendUpdateOfDirectoryTask(List<AgentDirectory> directories) {
        this.directories.addAll(directories);
    }

    @Override
    public void run() {
        UpdateDirectory phoneExtensionUpdate = new UpdateDirectory();
        phoneExtensionUpdate.setDirectories(directories);
        Send send = new Send(Function.PHONE_DIRECTORY_UPDATED);
        send.setPhoneExtensionUpdate(phoneExtensionUpdate);
        for (Integer extension : websocket.getSessionDirectoryMonitor()) {
            websocket.writeMessageSend(extension, send);
        }
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
