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
 * 
 */
public class PushNotification implements DataSerializable {

    @Expose
    private String msg;
    
    @Override
    public void writeData(ObjectDataOutput odo) throws IOException {
        odo.writeObject(msg);
    }
    
    @Override
    public void readData(ObjectDataInput odi) throws IOException {
        msg = odi.readObject();
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    
    
}
