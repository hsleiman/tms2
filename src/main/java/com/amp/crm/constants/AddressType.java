/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

public enum AddressType {
    CURRENT(1),
    MAILING(2),
    PRIOR(3),
    EMPLOYER(4),
    PROPERTY(5);
    
    private final int id;

    private AddressType(int id) {
        this.id = id;
    }

    public int getAddressTypeId() {
        return id;
    }

    public static AddressType getAddressTypeById(int id) {
        for (AddressType addressType : AddressType.values()) {
            if (addressType.getAddressTypeId() == id) {
                return addressType;
            }
        }
        return null;
    }  
}
