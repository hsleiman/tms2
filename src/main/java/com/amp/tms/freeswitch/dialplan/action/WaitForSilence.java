/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.dialplan.action;

/**
 *
 * @author hsleiman
 */
public class WaitForSilence extends AbstractAction{

    public WaitForSilence(String data) {
        super("wait_for_silence", data);
    }
    
}
