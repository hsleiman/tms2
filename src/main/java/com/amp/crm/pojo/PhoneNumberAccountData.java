/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import com.amp.crm.constants.PhoneNumberType;
import java.math.BigInteger;

/**
 *
 * 
 */
public class PhoneNumberAccountData {
    
    private long accountPk;
    private long customerPk;
    private String firstName;
    private String lastName;
    private Boolean doNotCall;
    private PhoneNumberType phoneNumberType; 
    
    public PhoneNumberAccountData() {
    }

    public PhoneNumberAccountData(long accountPk, long customerPk, String firstName, String lastName, Boolean doNotCall, PhoneNumberType phoneNumberType) {
        this.accountPk = accountPk;
        this.customerPk = customerPk;
        this.firstName = firstName;
        this.lastName = lastName;
        this.doNotCall = doNotCall;
        this.phoneNumberType = phoneNumberType;
    }
    
    public PhoneNumberAccountData(long accountPk, long customerPk, String firstName, String lastName, Boolean doNotCall, int phoneType) {
        this.accountPk = accountPk;
        this.customerPk = customerPk;
        this.firstName = firstName;
        this.lastName = lastName;
        this.doNotCall = doNotCall;
        this.phoneNumberType = PhoneNumberType.getPhoneNumberType(phoneType);
    }  

    public long getLoanPk() {
        return accountPk;
    }

    public void setLoanPk(long accountPk) {
        this.accountPk = accountPk;
    }
    
     public void setLoanPkk(BigInteger loanPk) {
        this.accountPk = loanPk != null ? loanPk.longValue() : null;
    }

    public long getCustomerPk() {
        return customerPk;
    }

    public void setCustomerPk(long customerPk) {
        this.customerPk = customerPk;
    }
    
    public void setCustomerPkk(BigInteger customerPk) {
        this.customerPk = customerPk != null ? customerPk.longValue() : null;
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

    public Boolean getDoNotCall() {
        return doNotCall;
    }

    public void setDoNotCall(Boolean doNotCall) {
        this.doNotCall = doNotCall;
    }
    
    public PhoneNumberType getPhoneNumberType() {
        return phoneNumberType;
    }

    public void setPhoneNumberType(PhoneNumberType phoneNumberType) {
        this.phoneNumberType = phoneNumberType;
    }

    public void setPhoneNumberTypee(Integer phoneNumberType) {
        this.phoneNumberType = PhoneNumberType.getPhoneNumberType(phoneNumberType);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (int) (this.accountPk ^ (this.accountPk >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhoneNumberAccountData other = (PhoneNumberAccountData) obj;
        if (this.accountPk != other.accountPk) {
            return false;
        }
        return true;
    }
       
    
}
