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
public class BridgeToSofiaContact extends AbstractAction {

    public BridgeToSofiaContact(Integer ext, String domain) {
        super("bridge", "${sofia_contact(agent/" + ext + "@" + domain + ")}");
    }
    
    public BridgeToSofiaContact(String ext, String domain) {
        super("bridge", "${sofia_contact(agent/" + ext + "@" + domain + ")}");
    }
}
