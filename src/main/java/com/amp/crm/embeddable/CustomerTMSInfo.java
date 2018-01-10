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

@Embeddable
public class CustomerTMSInfo implements DataSerializable, Serializable {

    @Expose
    private Long accountId;
    @Expose
    @JsonProperty("lastName")
    private String customerLastName;
    @Expose
    @JsonProperty("firstName")
    private String customerFirstName;
    @Expose
    @JsonProperty("phone")
    private String customerPhoneNumber;
    
    @Expose
    @JsonProperty("phoneType")
    private String customerPhoneNumberType;
    
    public CustomerTMSInfo() {
    }

    public CustomerTMSInfo(Long accountId, String customerLastName, String customerFirstName, String customerPhoneNumber, String customerPhoneNumberType) {
        this.accountId = accountId;
        this.customerLastName = customerLastName;
        this.customerFirstName = customerFirstName;
        this.customerPhoneNumber = customerPhoneNumber;
        this.customerPhoneNumberType = customerPhoneNumberType;
    }

    public CustomerTMSInfo(CustomerTMSInfo copy) {
        this.accountId = copy.accountId;
        this.customerFirstName = copy.customerFirstName;
        this.customerLastName = copy.customerLastName;
        this.customerPhoneNumber = copy.customerPhoneNumber;
        this.customerPhoneNumberType = copy.customerPhoneNumberType;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public String getCustomerPhoneNumberType() {
        return customerPhoneNumberType;
    }

    public void setCustomerPhoneNumberType(String customerPhoneNumberType) {
        this.customerPhoneNumberType = customerPhoneNumberType;
    }

    public void clear() {
        accountId = null;
        customerLastName = null;
        customerFirstName = null;
        customerPhoneNumber = null;
        customerPhoneNumberType = null;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(accountId);
        out.writeUTF(customerLastName);
        out.writeUTF(customerFirstName);
        out.writeUTF(customerPhoneNumber);
        out.writeUTF(customerPhoneNumberType);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        accountId = in.readObject();
        customerLastName = in.readUTF();
        customerFirstName = in.readUTF();
        customerPhoneNumber = in.readUTF();
        customerPhoneNumberType = in.readUTF();
    }

}
