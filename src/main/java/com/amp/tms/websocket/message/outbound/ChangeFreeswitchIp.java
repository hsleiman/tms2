/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message.outbound;

import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;

/**
 *
 * @author farzadaziminia
 */
public class ChangeFreeswitchIp implements DataSerializable {

    @Expose
    String freeswitchIp;

    public String getFreeswitchIp() {
        return freeswitchIp;
    }

    public void setFreeswitchIp(String freeswitchIp) {
        this.freeswitchIp = freeswitchIp;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(freeswitchIp);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        freeswitchIp = in.readUTF();

    }

}
