/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.websocket.message.outbound;

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
 * @author hsleiman
 */
@Embeddable
public class PhoneToType implements Serializable, DataSerializable {

    @Expose
    @JsonProperty("phone_number")
    private Long phoneNumber;
    
    @Expose
    @JsonProperty("phone_type")
    private String phoneType;

    @Expose
    @JsonProperty("last_name")
    private String lastName;

    @Expose
    @JsonProperty("first_name")
    private String firstName;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(phoneNumber);
        out.writeUTF(phoneType);
        out.writeUTF(lastName);
        out.writeUTF(firstName);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        phoneNumber = in.readObject();
        phoneType = in.readUTF();
        lastName = in.readUTF();
        firstName = in.readUTF();
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phone_number) {
        this.phoneNumber = phone_number;
    }

    public String getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(String phone_type) {
        this.phoneType = phone_type;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String last_name) {
        this.lastName = last_name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String first_name) {
        this.firstName = first_name;
    }

}
