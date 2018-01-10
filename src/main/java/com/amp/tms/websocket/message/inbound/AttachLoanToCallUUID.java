/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message.inbound;

import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;

/**
 *
 * 
 */
public class AttachLoanToCallUUID implements DataSerializable {

    @Expose
    private Long loanId;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(loanId);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        loanId = in.readObject();
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

}
