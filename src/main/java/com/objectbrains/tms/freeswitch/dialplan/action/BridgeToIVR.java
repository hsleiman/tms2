/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.dialplan.action;

import com.objectbrains.tms.enumerated.FreeswitchContext;

/**
 *
 * @author hsleiman
 */
public class BridgeToIVR extends AbstractAction {

    public BridgeToIVR(String ip) {
        super("bridge", "sofia/ivr/sip:" + 1000 + "@" + ip + ":"+FreeswitchContext.ivr_dp.getPort()+";transport=tcp");
    }
}