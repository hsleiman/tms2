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
public class AVMD extends AbstractAction{

    public AVMD() {
        super("avmd");
    }
    
    public AVMD(String data) {
        super("avmd", data);
    }
    
}
