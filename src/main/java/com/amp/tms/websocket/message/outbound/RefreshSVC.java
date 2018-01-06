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
import com.amp.tms.enumerated.RefreshSVCEnum;
import java.io.IOException;

/**
 *
 * @author hsleiman
 */
public class RefreshSVC implements DataSerializable {

    @Expose
    private RefreshSVCEnum key;

    public RefreshSVCEnum getKey() {
        return key;
    }

    public void setKey(RefreshSVCEnum key) {
        this.key = key;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(key);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        key = in.readObject();
    }

}
