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
public class BridgeToAgent extends AbstractAction {

    public BridgeToAgent(String ip, Integer ext) {
        super("bridge", "sofia/agent/sip:" + ext + "@" + ip + ":" + FreeswitchContext.agent_dp.getPort() + ";transport=tcp");
    }

    public BridgeToAgent(FreeswitchContext from, String ip, Integer ext) {
        super("bridge", "sofia/" + from.getProfile() + "/sip:" + ext + "@" + ip + ":" + FreeswitchContext.agent_dp.getPort() + ";transport=tcp");
    }
}
