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
public class BridgeExport extends AbstractAction{

    public BridgeExport(String data) {
        super("bridge_export", data);
    }
    
    public BridgeExport(String name, String data) {
        super("bridge_export", name, data);
    }
}
