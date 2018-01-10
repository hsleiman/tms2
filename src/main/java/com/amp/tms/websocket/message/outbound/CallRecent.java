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
import com.amp.tms.pojo.CallHistory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 
 */
public class CallRecent implements DataSerializable {

    @Expose
    private List<CallHistory> callHistorys;

    private CallRecent() {
    }

    public CallRecent(List<CallHistory> list) {
        callHistorys = list;
    }

    public List<CallHistory> getCallHistorys() {
        return callHistorys;
    }

    public void setCallHistorys(List<CallHistory> callHistorys) {
        this.callHistorys = callHistorys;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        int size = callHistorys.size();
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            out.writeObject(callHistorys.get(i));
        }
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        int size = in.readInt();
        callHistorys = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            callHistorys.add(in.<CallHistory>readObject());
        }
    }

}
