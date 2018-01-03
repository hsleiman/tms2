/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.pojo;

import com.objectbrains.sti.constants.PhoneNumberType;
import com.objectbrains.sti.service.dialer.PhoneNumberCallable;
import java.io.Serializable;

/**
 *
 * @author David
 */
public class BasicPhoneData extends PhoneNumberCallable implements Serializable{
 
    private Long phoneNumber;
    private PhoneNumberType phoneNumberType;

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public PhoneNumberType getPhoneNumberType() {
        return phoneNumberType;
    }

    public void setPhoneNumberType(PhoneNumberType phoneNumberType) {
        this.phoneNumberType = phoneNumberType;
    }
    
}
