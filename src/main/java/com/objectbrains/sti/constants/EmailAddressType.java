/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.constants;

/**
 *
 * @author jaimel
 */
public enum EmailAddressType {
    
    PRIMARY_EMAIL(1, "Primary Email"),
    NON_PRIMARY_EMAIL(2, "Non-Primary Email"),
    BUSINESS_EMAIL(3, "Business Email");
    
    private final int emailAddressType;
    private final String emailAddressDesc;
    
    private EmailAddressType(int type, String desc) {
        this.emailAddressType = type;
        this.emailAddressDesc = desc;
    }

    public int getEmailAddressType() {
        return emailAddressType;
    }

    public String getEmailAddressDesc() {
        return emailAddressDesc;
    }
       
    public static EmailAddressType getEmailAddressType(int id) {
        for (EmailAddressType eat : EmailAddressType.values()) {
            if (eat.getEmailAddressType()== id) {
                return eat;
            }
        }   
        return null;
    }
}
