/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

public class NationalPhoneNumber {
    
    private Long areaCode;
    private Long localNumber;

    public Long getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Long areaCode) {
        this.areaCode = areaCode;
    }

    public Long getLocalNumber() {
        return localNumber;
    }

    public void setLocalNumber(Long localNumber) {
        this.localNumber = localNumber;
    }

    public String toLog() {
        return "NationalPhoneNumber{" + "areaCode=" + areaCode + ", localNumber=" + localNumber + '}';
    }
    
    
}

