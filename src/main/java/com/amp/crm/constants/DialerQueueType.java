/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

public enum DialerQueueType {
    
    INBOUND,
    OUTBOUND;
    
    //Discriminator values
    public static final String INBOUND_TYPE = "INBOUND";
    public static final String OUTBOUND_TYPE = "OUTBOUND";
    
   
}
