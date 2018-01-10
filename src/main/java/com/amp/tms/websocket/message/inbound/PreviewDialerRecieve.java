/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message.inbound;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.utility.GsonUtility;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import java.io.IOException;

/**
 *
 * 
 */
public class PreviewDialerRecieve implements DataSerializable {

    @Expose
    private boolean previewStatus;
    @Expose
    private long loan_id;
    @Expose
    private PhoneToType phone;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeBoolean(previewStatus);
        out.writeLong(loan_id);
        out.writeObject(phone);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        previewStatus = in.readBoolean();
        loan_id = in.readLong();
        phone = in.readObject();
    }

    public boolean getPreviewStatus() {
        return previewStatus;
    }

    public void setPreviewStatus(boolean previewStatus) {
        this.previewStatus = previewStatus;
    }

    public long getLoan_id() {
        return loan_id;
    }

    public void setLoan_id(long loan_id) {
        this.loan_id = loan_id;
    }

    public PhoneToType getPhone() {
        return phone;
    }

    public void setPhone(PhoneToType phone) {
        this.phone = phone;
    }

    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

}
