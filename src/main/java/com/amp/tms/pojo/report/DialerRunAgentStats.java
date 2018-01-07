/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo.report;

/**
 *
 * @author HS
 */
public class DialerRunAgentStats {

    private String agent_name;
    private int call_count;
    private int contact_count;
    private int ptp_count;

    public String getAgent_name() {
        return agent_name;
    }

    public void setAgent_name(String agent_name) {
        this.agent_name = agent_name;
    }

    public int getCall_count() {
        return call_count;
    }

    public void setCall_count(int call_count) {
        this.call_count = call_count;
    }

    public int getContact_count() {
        return contact_count;
    }

    public void setContact_count(int contact_count) {
        this.contact_count = contact_count;
    }

    public int getPtp_count() {
        return ptp_count;
    }

    public void setPtp_count(int ptp_count) {
        this.ptp_count = ptp_count;
    }

}
