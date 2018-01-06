/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 *
 * @author connorpetty
 */
@Embeddable
public class CustomerTMSInfo implements DataSerializable, Serializable {

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
    
    public CustomerTMSInfo() {
    }

    public CustomerTMSInfo(Long loanId, String borrowerLastName, String borrowerFirstName, String borrowerPhoneNumber, String borrowerPhoneNumberType) {
        this.loanId = loanId;
        this.borrowerLastName = borrowerLastName;
        this.borrowerFirstName = borrowerFirstName;
        this.borrowerPhoneNumber = borrowerPhoneNumber;
        this.borrowerPhoneNumberType = borrowerPhoneNumberType;
    }

    public CustomerTMSInfo(CustomerTMSInfo copy) {
        this.loanId = copy.loanId;
        this.borrowerFirstName = copy.borrowerFirstName;
        this.borrowerLastName = copy.borrowerLastName;
        this.borrowerPhoneNumber = copy.borrowerPhoneNumber;
        this.borrowerPhoneNumberType = copy.borrowerPhoneNumberType;
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

    public void clear() {
        loanId = null;
        borrowerLastName = null;
        borrowerFirstName = null;
        borrowerPhoneNumber = null;
        borrowerPhoneNumberType = null;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(loanId);
        out.writeUTF(borrowerLastName);
        out.writeUTF(borrowerFirstName);
        out.writeUTF(borrowerPhoneNumber);
        out.writeUTF(borrowerPhoneNumberType);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        loanId = in.readObject();
        borrowerLastName = in.readUTF();
        borrowerFirstName = in.readUTF();
        borrowerPhoneNumber = in.readUTF();
        borrowerPhoneNumberType = in.readUTF();
    }

}
