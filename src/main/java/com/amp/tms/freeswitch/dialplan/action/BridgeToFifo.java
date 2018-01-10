/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.dialplan.action;

import com.amp.tms.enumerated.FreeswitchContext;

/**
 *
 * 
 */
public class BridgeToFifo extends AbstractAction {

    public BridgeToFifo(FreeswitchContext from, String ip) {
        super("bridge", "sofia/"+from.getProfile()+"/sip:" + 1000 + "@" + ip + ":"+FreeswitchContext.fifo_dp.getPort()+";transport=tcp");
    }
}
