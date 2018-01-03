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
public class BridgeAgentToFifo extends AbstractAction {

    public BridgeAgentToFifo(String ip) {
        super("bridge", "sofia/agent/sip:" + 1000 + "@" + ip + ":"+FreeswitchContext.fifo_dp.getPort()+";transport=tcp");
    }
}
