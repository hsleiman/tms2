/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.enumerated.refrence;

/**
 *
 * 
 */
public enum HOLDOrder {
    PLACE_ON_HOLD_CUSTOMER_SERVICE("PlaceOnHoldForCustomerService"),
    PLACE_ON_HOLD("PlaceOnHold"),
    PLACE_ON_HOLD_TRANSFER("PlaceOnHold"),
    PLACE_IN_VOICEMAIL("PlaceInVoicemail"),
    PLACE_OFF_HOLD("PlaceOffHold");

    
    
    private final String method;

    private HOLDOrder(String method) {
        this.method = method;
    }

    public String getMethodName() {
        return method;
    }
}
