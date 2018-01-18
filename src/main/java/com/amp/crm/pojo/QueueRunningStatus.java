/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import com.amp.crm.constants.DialerState;

public class QueueRunningStatus {

    private long queueRunningStatusId;
    private boolean running;
    private DialerState dialerState;

    public long getQueueRunningStatusId() {
        return queueRunningStatusId;
    }

    public void setQueueRunningStatusId(long queueRunningStatusId) {
        this.queueRunningStatusId = queueRunningStatusId;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public DialerState getDialerState() {
        return dialerState;
    }

    public void setDialerState(DialerState dialerState) {
        this.dialerState = dialerState;
    }
}
