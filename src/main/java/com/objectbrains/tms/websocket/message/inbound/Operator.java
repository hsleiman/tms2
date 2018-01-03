/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.websocket.message.inbound;

import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.objectbrains.tms.enumerated.refrence.PhoneOperator;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author hsleiman
 */
public class Operator implements DataSerializable {

    @Expose
    private PhoneOperator operation;
    @Expose
    private Integer onCall;
    @Expose
    private Integer callee;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(operation);
        out.writeObject(onCall);
        out.writeObject(callee);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        operation = in.readObject();
        onCall = in.readObject();
        callee = in.readObject();
    }

    public PhoneOperator getOperation() {
        return operation;
    }

    public void setOperation(PhoneOperator operation) {
        this.operation = operation;
    }

    public Integer getOnCall() {
        return onCall;
    }

    public void setOnCall(Integer onCall) {
        this.onCall = onCall;
    }

    public Integer getCallee() {
        return callee;
    }

    public void setCallee(Integer callee) {
        this.callee = callee;
    }

}
