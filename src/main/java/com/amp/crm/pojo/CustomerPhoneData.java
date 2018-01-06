/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author David
 */
public class CustomerPhoneData implements Serializable{
    
    private List<BasicPhoneData> basicPhoneData = new ArrayList<>();
    private String firstName;
    private String lastName;

    public List<BasicPhoneData> getBasicPhoneData() {
        return basicPhoneData;
    }

    public void setBasicPhoneData(List<BasicPhoneData> basicPhoneData) {
        this.basicPhoneData = basicPhoneData;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
}
