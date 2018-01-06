/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message.outbound;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.crm.constants.PopupDisplayMode;
import com.amp.tms.utility.GsonUtility;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hsleiman
 */
public class PreviewDialerSend implements Serializable, DataSerializable {

    @Expose
    private String previewType;

    @Expose
    @JsonProperty("borrower_first_name")
    private String borrowerFirstName;

    @Expose
    @JsonProperty("borrower_last_name")
    private String borrowerLastName;

    @Expose
    @JsonProperty("loan_id")
    private long loanId;

    @Expose
    private List<PhoneToType> phone;

    @Expose
    private Long delay;

    @Expose
    @JsonProperty("popup_type")
    private PopupDisplayMode popupType;

    @Expose
    @JsonProperty("call_uuid")
    private String callUUID;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(previewType);
        out.writeUTF(borrowerFirstName);
        out.writeUTF(borrowerLastName);
        out.writeLong(loanId);
        List<PhoneToType> phones = getPhone();
        out.writeInt(phones.size());
        for (PhoneToType types : phones) {
            types.writeData(out);
        }
        out.writeObject(delay);
        out.writeObject(popupType);
        out.writeUTF(callUUID);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        previewType = in.readUTF();
        borrowerFirstName = in.readUTF();
        borrowerLastName = in.readUTF();
        loanId = in.readLong();
        List<PhoneToType> phones = getPhone();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            PhoneToType phoneType = new PhoneToType();
            phoneType.readData(in);
            phones.add(phoneType);
        }
        delay = in.readObject();
        popupType = in.readObject();
        callUUID = in.readUTF();
    }

    public String getPreviewType() {
        return previewType;
    }

    public void setPreviewType(String previewType) {
        this.previewType = previewType;
    }

    public String getBorrowerFirstName() {
        return borrowerFirstName;
    }

    public void setBorrowerFirstName(String borrower_first_name) {
        this.borrowerFirstName = borrower_first_name;
    }

    public String getBorrowerLastName() {
        return borrowerLastName;
    }

    public void setBorrowerLastName(String borrower_last_name) {
        this.borrowerLastName = borrower_last_name;
    }

    public long getLoanId() {
        return loanId;
    }

    public void setLoanId(long loan_id) {
        this.loanId = loan_id;
    }

    public List<PhoneToType> getPhone() {
        if (phone == null) {
            phone = new ArrayList<>();
        }
        return phone;
    }

    public void setPhone(List<PhoneToType> phone) {
        this.phone = phone;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public PopupDisplayMode getPopupType() {
        return popupType;
    }

    public void setPopupType(PopupDisplayMode popup_type) {
        this.popupType = popup_type;
    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

    public String dump() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

}
