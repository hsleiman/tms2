/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.pojo;

import com.objectbrains.tms.service.dialer.Dialer;

/**
 *
 * @author hsleiman
 */
public class QueueRunningSatus {

    private long queue;
    private boolean running;
    private Dialer.State dialerState;

    public long getQueue() {
        return queue;
    }

    public void setQueue(long queue) {
        this.queue = queue;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Dialer.State getDialerState() {
        return dialerState;
    }

    public void setDialerState(Dialer.State dialerState) {
        this.dialerState = dialerState;
    }

}