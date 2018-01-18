/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import com.amp.crm.db.entity.base.dialer.CallDetailRecord;

public class BestTimeToCallCluster {
    
    
    private CallDetailRecord centerNode;
    
    private double goodCalls =0;
    
    private double badCalls =0;
    
    private double timeRangeInMinutes;

    public double getGoodCalls() {
        return goodCalls;
    }

    public void setGoodCalls(int goodCalls) {
        this.goodCalls = goodCalls;
    }

    public void goodCallsInc(){
        ++this.goodCalls;
    }
    
    public void badCallsInc(){
        ++this.badCalls;
    }
    
    public double getBadCalls() {
        return badCalls;
    }

    public void setBadCalls(int badCalls) {
        this.badCalls = badCalls;
    }

    public CallDetailRecord getCenterNode() {
        return centerNode;
    }

    public void setCenterNode(CallDetailRecord centerNode) {
        this.centerNode = centerNode;
    }

    public double getTimeRangeInMinutes() {
        return timeRangeInMinutes;
    }

    public void setTimeRangeInMinutes(double timeRangeInHours) {
        this.timeRangeInMinutes = timeRangeInHours;
    }
    

    
}

