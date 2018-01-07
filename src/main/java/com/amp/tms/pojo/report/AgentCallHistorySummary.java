/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo.report;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class AgentCallHistorySummary {

    private int extension;
    private int inboundCallCount;
    private int manualCallCount;
    private int dialerCallCount;
    private int contactCount;
    private int ptpCount;

    public int getExtension() {
        return extension;
    }

    public void setExtension(int extension) {
        this.extension = extension;
    }

    public int getInboundCallCount() {
        return inboundCallCount;
    }

    public void setInboundCallCount(int inboundCallCount) {
        this.inboundCallCount = inboundCallCount;
    }

    public int getManualCallCount() {
        return manualCallCount;
    }

    public void setManualCallCount(int manualCallCount) {
        this.manualCallCount = manualCallCount;
    }

    public int getDialerCallCount() {
        return dialerCallCount;
    }

    public void setDialerCallCount(int dialerCallCount) {
        this.dialerCallCount = dialerCallCount;
    }

    public int getContactCount() {
        return contactCount;
    }

    public void setContactCount(int contactCount) {
        this.contactCount = contactCount;
    }

    public int getPtpCount() {
        return ptpCount;
    }

    public void setPtpCount(int ptpCount) {
        this.ptpCount = ptpCount;
    }

}
