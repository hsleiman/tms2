/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.websocket.message.outbound;

import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.pojo.BorrowerInfo;
import java.io.IOException;

/**
 *
 * @author hsleiman
 */
public class CallSipHeader implements DataSerializable {

    @Expose
    private String call_uuid;
    
    @Expose
    private CallDirection callDirection;

    @Expose
    private BorrowerInfo borrowerInfo;

    @Expose
    private Boolean ignore_disposition;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(call_uuid);
        out.writeObject(callDirection);
        getBorrowerInfo().writeData(out);
        out.writeObject(ignore_disposition);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        call_uuid = in.readUTF();
        callDirection = in.readObject();
        getBorrowerInfo().readData(in);
        ignore_disposition = in.readObject();
    }

    public String getCall_uuid() {
        return call_uuid;
    }

    public void setCall_uuid(String call_uuid) {
        this.call_uuid = call_uuid;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }

    public void setCallDirection(CallDirection callDirection) {
        this.callDirection = callDirection;
    }

    public BorrowerInfo getBorrowerInfo() {
        if (borrowerInfo == null) {
            borrowerInfo = new BorrowerInfo();
        }
        return borrowerInfo;
    }

    public void setBorrowerInfo(BorrowerInfo borrowerInfo) {
        this.borrowerInfo = borrowerInfo;
    }

    public Boolean getIgnore_disposition() {
        return ignore_disposition;
    }

    public void setIgnore_disposition(Boolean ignore_disposition) {
        this.ignore_disposition = ignore_disposition;
    }

}
