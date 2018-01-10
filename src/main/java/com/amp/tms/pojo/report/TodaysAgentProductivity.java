/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo.report;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

/**
 *
 * 
 */
public class TodaysAgentProductivity implements Serializable {

    /*
     usr.last_name || ', ' || usr.first_name as agent_name,
     stat_sum.start_time,
     stat_sum.end_time,
     stat_sum.total_idle_time,
     stat_sum.total_on_call_time,
     stat_sum.total_hold_time,
     stat_sum.total_wrap_time,
     stat_sum.total_preview_time,
     stat_sum.total_break_time,
     stat_sum.total_meeting_time,
     stat_sum.total_ready_time,
     stat_sum.utilization_break_time,
     stat_sum.utilization_hold_time,
     stat_sum.utilization_idle_time,
     stat_sum.utilization_meeting_time,
     stat_sum.utilization_preview_time,
     stat_sum.utilization_wrap_time,
     call_sum.inboundCallCount,
     call_sum.dialerCallCount,
     call_sum.dialerContactCount,
     call_sum.manualCallCount,
     call_sum.manualContactCount,
     call_sum.contactCount,
     call_sum.dialersuccess,
     call_sum.manualsuccess,
     call_sum.dialerptpcount,
     call_sum.manualptpcount,
     call_sum.ptpcount,
     call_sum.success
     */
    @Expose
    private String agentName;
    @Expose
    private String startTime;
    @Expose
    private String endTime;
    @Expose
    private String totalIdleTime;
    @Expose
    private String totalOnCallTime;
    @Expose
    private String totalHoldTime;
    @Expose
    private String totalWrapTime;
    @Expose
    private String totalPreviewTime;
    @Expose
    private String totalBreakTime;
    @Expose
    private String totalMeetingTime;
    @Expose
    private String totalReadyTime;
    @Expose
    private String utilizationBreakTime;
    @Expose
    private String utilizationHoldTime;
    @Expose
    private String utilizationIdleTime;
    @Expose
    private String utilizationMeetingTime;
    @Expose
    private String utilizationPreviewTime;
    @Expose
    private String utilizationWrapTime;
    @Expose
    private String inboundCallCount;
    @Expose
    private String dialerCallCount;
    @Expose
    private String dialerContactCount;
    @Expose
    private String manualCallCount;
    @Expose
    private String manualContactCount;
    @Expose
    private String contactCount;
    @Expose
    private String dialerSuccess;
    @Expose
    private String manualSuccess;
    @Expose
    private String dialerPtpCount;
    @Expose
    private String manualPtpCount;
    @Expose
    private String ptpCount;
    @Expose
    private String success;

    public TodaysAgentProductivity(Object[] get) {
        agentName = String.valueOf(get[0]);
        startTime = String.valueOf(get[1]);
        endTime = String.valueOf(get[2]);
        totalIdleTime = String.valueOf(get[3]);
        totalOnCallTime = String.valueOf(get[4]);
        totalHoldTime = String.valueOf(get[5]);
        totalWrapTime = String.valueOf(get[6]);
        totalPreviewTime = String.valueOf(get[7]);
        totalBreakTime = String.valueOf(get[8]);
        totalMeetingTime = String.valueOf(get[9]);
        totalReadyTime = String.valueOf(get[10]);
        utilizationBreakTime = String.valueOf(get[11]);
        utilizationHoldTime = String.valueOf(get[12]);
        utilizationIdleTime = String.valueOf(get[13]);
        utilizationMeetingTime = String.valueOf(get[14]);
        utilizationPreviewTime = String.valueOf(get[15]);
        utilizationWrapTime = String.valueOf(get[16]);
        inboundCallCount = String.valueOf(get[17]);
        dialerCallCount = String.valueOf(get[18]);
        dialerContactCount = String.valueOf(get[19]);
        manualCallCount = String.valueOf(get[20]);
        manualContactCount = String.valueOf(get[21]);
        contactCount = String.valueOf(get[22]);
        dialerSuccess = String.valueOf(get[23]);
        manualSuccess = String.valueOf(get[24]);
        dialerPtpCount = String.valueOf(get[25]);
        manualPtpCount = String.valueOf(get[26]);
        ptpCount = String.valueOf(get[27]);
        success = String.valueOf(get[28]);
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTotalIdleTime() {
        return totalIdleTime;
    }

    public void setTotalIdleTime(String totalIdleTime) {
        this.totalIdleTime = totalIdleTime;
    }

    public String getTotalOnCallTime() {
        return totalOnCallTime;
    }

    public void setTotalOnCallTime(String totalOnCallTime) {
        this.totalOnCallTime = totalOnCallTime;
    }

    public String getTotalHoldTime() {
        return totalHoldTime;
    }

    public void setTotalHoldTime(String totalHoldTime) {
        this.totalHoldTime = totalHoldTime;
    }

    public String getTotalWrapTime() {
        return totalWrapTime;
    }

    public void setTotalWrapTime(String totalWrapTime) {
        this.totalWrapTime = totalWrapTime;
    }

    public String getTotalPreviewTime() {
        return totalPreviewTime;
    }

    public void setTotalPreviewTime(String totalPreviewTime) {
        this.totalPreviewTime = totalPreviewTime;
    }

    public String getTotalBreakTime() {
        return totalBreakTime;
    }

    public void setTotalBreakTime(String totalBreakTime) {
        this.totalBreakTime = totalBreakTime;
    }

    public String getTotalMeetingTime() {
        return totalMeetingTime;
    }

    public void setTotalMeetingTime(String totalMeetingTime) {
        this.totalMeetingTime = totalMeetingTime;
    }

    public String getTotalReadyTime() {
        return totalReadyTime;
    }

    public void setTotalReadyTime(String totalReadyTime) {
        this.totalReadyTime = totalReadyTime;
    }

    public String getUtilizationBreakTime() {
        return utilizationBreakTime;
    }

    public void setUtilizationBreakTime(String utilizationBreakTime) {
        this.utilizationBreakTime = utilizationBreakTime;
    }

    public String getUtilizationHoldTime() {
        return utilizationHoldTime;
    }

    public void setUtilizationHoldTime(String utilizationHoldTime) {
        this.utilizationHoldTime = utilizationHoldTime;
    }

    public String getUtilizationIdleTime() {
        return utilizationIdleTime;
    }

    public void setUtilizationIdleTime(String utilizationIdleTime) {
        this.utilizationIdleTime = utilizationIdleTime;
    }

    public String getUtilizationMeetingTime() {
        return utilizationMeetingTime;
    }

    public void setUtilizationMeetingTime(String utilizationMeetingTime) {
        this.utilizationMeetingTime = utilizationMeetingTime;
    }

    public String getUtilizationPreviewTime() {
        return utilizationPreviewTime;
    }

    public void setUtilizationPreviewTime(String utilizationPreviewTime) {
        this.utilizationPreviewTime = utilizationPreviewTime;
    }

    public String getUtilizationWrapTime() {
        return utilizationWrapTime;
    }

    public void setUtilizationWrapTime(String utilizationWrapTime) {
        this.utilizationWrapTime = utilizationWrapTime;
    }

    public String getInboundCallCount() {
        return inboundCallCount;
    }

    public void setInboundCallCount(String inboundCallCount) {
        this.inboundCallCount = inboundCallCount;
    }

    public String getDialerCallCount() {
        return dialerCallCount;
    }

    public void setDialerCallCount(String dialerCallCount) {
        this.dialerCallCount = dialerCallCount;
    }

    public String getManualCallCount() {
        return manualCallCount;
    }

    public void setManualCallCount(String manualCallCount) {
        this.manualCallCount = manualCallCount;
    }

    public String getContactCount() {
        return contactCount;
    }

    public void setContactCount(String contactCount) {
        this.contactCount = contactCount;
    }

    public String getPtpCount() {
        return ptpCount;
    }

    public void setPtpCount(String ptpCount) {
        this.ptpCount = ptpCount;
    }

    public String getDialerContactCount() {
        return dialerContactCount;
    }

    public void setDialerContactCount(String dialerContactCount) {
        this.dialerContactCount = dialerContactCount;
    }

    public String getManualContactCount() {
        return manualContactCount;
    }

    public void setManualContactCount(String manualContactCount) {
        this.manualContactCount = manualContactCount;
    }

    public String getDialerSuccess() {
        return dialerSuccess;
    }

    public void setDialerSuccess(String dialerSuccess) {
        this.dialerSuccess = dialerSuccess;
    }

    public String getManualSuccess() {
        return manualSuccess;
    }

    public void setManualSuccess(String manualSuccess) {
        this.manualSuccess = manualSuccess;
    }

    public String getDialerPtpCount() {
        return dialerPtpCount;
    }

    public void setDialerPtpCount(String dialerPtpCount) {
        this.dialerPtpCount = dialerPtpCount;
    }

    public String getManualPtpCount() {
        return manualPtpCount;
    }

    public void setManualPtpCount(String manualPtpCount) {
        this.manualPtpCount = manualPtpCount;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

}
