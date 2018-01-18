/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import com.google.i18n.phonenumbers.Phonenumber;

public class PhoneNumberDetails extends NationalPhoneNumber {
       
    private String extension;
    private Boolean possibleNumber;
    private Boolean validNumber;
    private Phonenumber.PhoneNumber phoneNumber;

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Boolean isPossibleNumber() {
        return possibleNumber;
    }

    public void setIsPossibleNumber(Boolean isPossibleNumber) {
        this.possibleNumber = isPossibleNumber;
    }

    public Boolean isValidNumber() {
        return validNumber;
    }

    public void setIsValidNumber(Boolean isValidNumber) {
        this.validNumber = isValidNumber;
    }

    public Phonenumber.PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Phonenumber.PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
}