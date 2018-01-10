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
public class AMD_VoiceStop extends AbstractAction{

    public AMD_VoiceStop() {
        super("voice_stop");
    }
    
    public AMD_VoiceStop(String data) {
        super("voice_stop", data);
    }
    
}
