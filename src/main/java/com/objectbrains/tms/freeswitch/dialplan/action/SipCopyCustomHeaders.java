/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.dialplan.action;

/**
 *
 * @author hsleiman
 */
public class SipCopyCustomHeaders extends AbstractAction{

    public SipCopyCustomHeaders(Boolean value) {
        super("set", "sip_copy_custom_headers="+value);
    }
    
}
