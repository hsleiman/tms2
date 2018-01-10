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
import com.amp.tms.pojo.BorrowerInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 
 */
public class PhoneCheck implements DataSerializable {

    @Expose
    private String phoneNumber;
    @Expose
    private Integer count;
    @Expose
    private List<PhoneCheckData> list;
    
    @Expose
    private String callUUID;

    @Expose
    private Integer status;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(phoneNumber);
        out.writeObject(count);
        
        int size = list != null ? list.size() : 0;
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            list.get(i).writeData(out);
        }
        
        out.writeObject(status);
        out.writeObject(callUUID);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        phoneNumber = in.readUTF();
        count = in.readObject();
        
        int size = in.readInt();
        if(size != 0){
            list = new ArrayList<>(size);
            for(int i = 0; i < size; i++){
                PhoneCheckData info = new PhoneCheckData();
                info.readData(in);
                list.add(info);
            }
        }
        
        status = in.readObject();
        callUUID = in.readObject();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<PhoneCheckData> getList() {
        return list;
    }

    public void setList(List<PhoneCheckData> list) {
        this.list = list;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }
    
    

}
