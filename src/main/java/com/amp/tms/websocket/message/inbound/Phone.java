/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message.inbound;

import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.amp.tms.enumerated.PhoneStatus;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import java.io.IOException;

/**
 *
 * 
 */
public class Phone extends PhoneToType {

    @Expose
    private PhoneStatus status;

    public PhoneStatus getStatus() {
        return status;
    }

    public void setStatus(PhoneStatus status) {
        this.status = status;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeObject(status);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        status = in.readObject();
    }

}
