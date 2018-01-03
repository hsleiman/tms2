/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.pojo.report;

import java.util.Date;

/**
 *
 * @author connorpetty
 */
public class DialerStatsReport {

    private String queueName;
    private Date startTime;
    private Date endTime;
    private int loans;
    private int dials;
    private int busy;
    private int answered;
    private int sitna;
    private int connects;
    private int abandonded;
    private int contacts;
    private int ptps;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getLoans() {
        return loans;
    }

    public void setLoans(int loans) {
        this.loans = loans;
    }

    public int getDials() {
        return dials;
    }

    public void setDials(int dials) {
        this.dials = dials;
    }

    public int getBusy() {
        return busy;
    }

    public void setBusy(int busy) {
        this.busy = busy;
    }

    public int getAnswered() {
        return answered;
    }

    public void setAnswered(int answered) {
        this.answered = answered;
    }

    public int getSitna() {
        return sitna;
    }

    public void setSitna(int sitna) {
        this.sitna = sitna;
    }

    public int getConnects() {
        return connects;
    }

    public void setConnects(int connects) {
        this.connects = connects;
    }

    public int getAbandonded() {
        return abandonded;
    }

    public void setAbandonded(int abandonded) {
        this.abandonded = abandonded;
    }

    public int getContacts() {
        return contacts;
    }

    public void setContacts(int contacts) {
        this.contacts = contacts;
    }

    public int getPtps() {
        return ptps;
    }

    public void setPtps(int ptps) {
        this.ptps = ptps;
    }

}
