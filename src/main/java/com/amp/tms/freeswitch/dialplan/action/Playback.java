/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.dialplan.action;

import com.amp.tms.enumerated.RecordedPhrases;
import com.amp.tms.enumerated.RecordedWords;

/**
 *
 * 
 */
public class Playback extends AbstractAction{

    public Playback(String data) {
        super("playback", data);
    }
    
    public Playback(RecordedPhrases data) {
        super("playback", data.getAudioPath());
    }
    
    public Playback(RecordedPhrases data, String complany) {
        super("playback", data.getAudioPath().replaceAll("CASHCALL", complany));
    }
    
    public Playback(RecordedWords data) {
        super("playback", data.getAudioPath());
    }
    
    public Playback(String path, String name) {
        super("playback", path+name+".wav");
    }
    
}
