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
public class SchedApi extends AbstractAction{

    public SchedApi(String data) {
        super("sched_api", data);
    }
    
    
    public SchedApi(Long startTimeSecond, String data, String leg) {
        super("sched_api", "+"+startTimeSecond+" "+data+" "+leg);
    }
    
}
