/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo.report;

import com.amp.tms.hazelcast.entity.AgentStats;

/**
 *
 * 
 */
public class AgentProductivity {

    private String userName;
    private String agentFirstName;
    private String agentLastName;
    private AgentStats.Report statsReport;
    private AgentCallHistorySummary callsReport;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAgentFirstName() {
        return agentFirstName;
    }

    public void setAgentFirstName(String agentFirstName) {
        this.agentFirstName = agentFirstName;
    }

    public String getAgentLastName() {
        return agentLastName;
    }

    public void setAgentLastName(String agentLastName) {
        this.agentLastName = agentLastName;
    }

    public AgentStats.Report getStatsReport() {
        return statsReport;
    }

    public void setStatsReport(AgentStats.Report statsReport) {
        this.statsReport = statsReport;
    }

    public AgentCallHistorySummary getCallsReport() {
        return callsReport;
    }

    public void setCallsReport(AgentCallHistorySummary callsReport) {
        this.callsReport = callsReport;
    }

}
