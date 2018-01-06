/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message;

import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author hsleiman
 */
public class Message implements Serializable, DataSerializable {

    @Expose
    private Function function;

    @Expose
    private String status;

    @Expose
    private String confirmCode;

    @Expose
    private int confirmCount = 0;

    @Expose
    private BiMessage biMessage;

    @Expose
    private PhoneCheck phoneCheck;

    public Message() {
    }

    public Message(Function function) {
        this.function = function;
    }

    public Message(Message copy){
        this.function = copy.function;
        this.status = copy.status;
        this.confirmCode = copy.confirmCode;
        this.confirmCount = copy.confirmCount;
        this.biMessage = copy.biMessage;
        this.phoneCheck = copy.phoneCheck;
    }
    
    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConfirmCode() {
        return confirmCode;
    }

    public void setConfirmCode(String confirmCode) {
        this.confirmCode = confirmCode;
    }

    public int getConfirmCount() {
        return confirmCount;
    }

    public void setConfirmCount(int confirmCount) {
        this.confirmCount = confirmCount;
    }

    public BiMessage getBiMessage() {
        return biMessage;
    }

    public void setBiMessage(BiMessage biMessage) {
        this.biMessage = biMessage;
    }

    public PhoneCheck getPhoneCheck() {
        return phoneCheck;
    }

    public void setPhoneCheck(PhoneCheck phoneCheck) {
        this.phoneCheck = phoneCheck;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(function);
        out.writeUTF(status);
        out.writeUTF(confirmCode);
        out.writeInt(confirmCount);
        out.writeObject(biMessage);
        out.writeObject(phoneCheck);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        function = in.readObject();
        status = in.readUTF();
        confirmCode = in.readUTF();
        confirmCount = in.readInt();
        biMessage = in.readObject();
        phoneCheck = in.readObject();
    }

}
