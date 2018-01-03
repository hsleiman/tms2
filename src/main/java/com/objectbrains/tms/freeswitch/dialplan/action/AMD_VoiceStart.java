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
public class AMD_VoiceStart extends AbstractAction{

    public AMD_VoiceStart() {
        super("voice_start");
    }
    
    public AMD_VoiceStart(String data) {
        super("voice_start", data);
    }
    
}
