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
public class ToneDetect extends AbstractAction{

    public ToneDetect(String data) {
        super("tone_detect", data);
    }
    
    public static ToneDetect DetectBusy(){
        return new ToneDetect("busy 480,620 r +15000 hangup 34 2");
    }
    
    public static ToneDetect Detect3Busy(Long mSecondTimeout){
        return new ToneDetect("busy 425 r +"+mSecondTimeout+" hangup 34 3");
    }
    
    public static ToneDetect DetectSitHigh1(){
        return new ToneDetect("sit-high-1 985.2 r +15000 hangup 41");
    }
    
    public static ToneDetect DetectSitHigh2(){
        return new ToneDetect("sit-high-2 1428.5 r +15000 hangup 41");
    }
    
    public static ToneDetect DetectSitLow1(){
        return new ToneDetect("sit-low-1 913.8 r +15000 hangup 41");
    }
    
    public static ToneDetect DetectSitLow2(){
        return new ToneDetect("sit-low-2 1370.6 r +15000 hangup 41");
    }
   
}
