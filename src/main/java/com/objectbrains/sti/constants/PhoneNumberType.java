/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.constants;



public enum PhoneNumberType {

    HOME_PHONE_1(0),
    HOME_PHONE_2(3),
    MOBILE_PHONE(1),
    WORK_PHONE(2),
    PRIMARY_PHONE(5),
    NON_PRIMARY_PHONE(6),
    BUSINESS_PHONE(10),
    GOOGLE_VOICE(15),
    OTHER_PHONE(20),
    REFERENCE(99);

    private int id;

    private PhoneNumberType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static PhoneNumberType getPhoneNumberType(int id) {
        for (PhoneNumberType pnt : PhoneNumberType.values()) {
            if (pnt.getId() == id) {
                return pnt;
            }
        }
        return null;
    }
}
