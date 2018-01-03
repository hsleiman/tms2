/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer.predict;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class QueueVariables {

    public final double serviceRate;
    public final double regensRate;
    public final double responseRate;
    public final int nServers;

    public QueueVariables(double serviceRate, double regensRate, double responseRate, int nServers) {
        this.serviceRate = serviceRate;
        this.regensRate = regensRate;
        this.responseRate = responseRate;
        this.nServers = nServers;
    }

}
