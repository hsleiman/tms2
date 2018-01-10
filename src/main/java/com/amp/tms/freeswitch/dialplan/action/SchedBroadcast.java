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
public class SchedBroadcast extends AbstractAction{

    public SchedBroadcast(String data) {
        super("sched_broadcast", data);
    }
    
    
    public SchedBroadcast(Long startTimeSecond, String data, String leg) {
        super("sched_broadcast", "+"+startTimeSecond+" "+data+" "+leg);
    }
    
}
