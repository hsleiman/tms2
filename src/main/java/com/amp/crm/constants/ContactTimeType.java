/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

/**
 *
 * @author Hoang, J, Bishistha
 */
public enum ContactTimeType {
    LAST_CONTACT_TIME ("lastContactTime"),
    LAST_LEFT_MESSAGE_TIME("leftMessageTime"),
    DIALER_LEFT_MESSAGE_TIME("dialerLeftMessageTime");
    
    private String desc;
    private ContactTimeType(String desc){
        this.desc = desc;
    }
    
    public String getDesc(ContactTimeType type){
        for(ContactTimeType c : ContactTimeType.values()){
            if(c == type) return c.desc;
        }
        return "None";
    }
}
