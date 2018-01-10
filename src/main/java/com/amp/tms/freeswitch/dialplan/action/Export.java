/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.dialplan.action;

/**
 *
 * 
 */
public class Export extends AbstractAction{

    public Export(String data) {
        super("export", data);
    }
    
    public Export(String name, String data) {
        super("export", name, data);
    }
}
