/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.pojo.BorrowerInfo;
import java.io.IOException;

/**
 *
 * @author HS
 */
public class PhoneCheckData implements DataSerializable {

    @Expose
    private Long loanId;
    @Expose
    @JsonProperty("lastName")
    private String borrowerLastName;
    @Expose
    @JsonProperty("firstName")
    private String borrowerFirstName;
    @Expose
    @JsonProperty("phone")
    private String borrowerPhoneNumber;

    @Expose
    @JsonProperty("phoneType")
    private String borrowerPhoneNumberType;

    @Expose
    @JsonProperty("dnc")
    private Boolean dnc;

    public PhoneCheckData() {
    }

    public PhoneCheckData(PhoneCheckData copy) {
        this.loanId = copy.loanId;
        this.borrowerFirstName = copy.borrowerFirstName;
        this.borrowerLastName = copy.borrowerLastName;
        this.borrowerPhoneNumber = copy.borrowerPhoneNumber;
        this.borrowerPhoneNumberType = copy.borrowerPhoneNumberType;
        this.dnc = copy.dnc;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public String getBorrowerLastName() {
        return borrowerLastName;
    }

    public void setBorrowerLastName(String borrowerLastName) {
        this.borrowerLastName = borrowerLastName;
    }

    public String getBorrowerFirstName() {
        return borrowerFirstName;
    }

    public void setBorrowerFirstName(String borrowerFirstName) {
        this.borrowerFirstName = borrowerFirstName;
    }

    public String getBorrowerPhoneNumber() {
        return borrowerPhoneNumber;
    }

    public void setBorrowerPhoneNumber(String borrowerPhoneNumber) {
        this.borrowerPhoneNumber = borrowerPhoneNumber;
    }

    public String getBorrowerPhoneNumberType() {
        return borrowerPhoneNumberType;
    }

    public void setBorrowerPhoneNumberType(String borrowerPhoneNumberType) {
        this.borrowerPhoneNumberType = borrowerPhoneNumberType;
    }

    public Boolean getDnc() {
        return dnc;
    }

    public void setDnc(Boolean dnc) {
        this.dnc = dnc;
    }

    public void clear() {
        loanId = null;
        borrowerLastName = null;
        borrowerFirstName = null;
        borrowerPhoneNumber = null;
        borrowerPhoneNumberType = null;
        dnc = null;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(loanId);
        out.writeUTF(borrowerLastName);
        out.writeUTF(borrowerFirstName);
        out.writeUTF(borrowerPhoneNumber);
        out.writeUTF(borrowerPhoneNumberType);
        out.writeObject(dnc);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        loanId = in.readObject();
        borrowerLastName = in.readUTF();
        borrowerFirstName = in.readUTF();
        borrowerPhoneNumber = in.readUTF();
        borrowerPhoneNumberType = in.readUTF();
        dnc = in.readObject();
    }

}
