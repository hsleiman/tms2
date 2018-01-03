/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.pojo;

import com.objectbrains.tms.enumerated.AgentState;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.DialerActiveStatus;
import com.objectbrains.tms.enumerated.SetAgentState;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.hazelcast.entity.AgentStats;
import com.objectbrains.tms.utility.DurationUtils;
import java.io.Serializable;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
public class AgentStatus extends StatusPojo implements Serializable {

    private Integer extension;
    private String username;

    private String call_uuid;
    private String currentAgentFreeswitchUUID;
    private Long currentLoanId;
    private String currentBorrowerLastName;
    private String currentBorrowerFirstName;
    private String currentOtherEndNumber;

    private LocalDateTime lastActivityTime;
    private LocalDateTime lastOutboundTime;
    private LocalDateTime lastInboundTime;
    private LocalDateTime lastHartbeatTime;

    private DialerActiveStatus dialerActive;
    private LocalDateTime onCallStartTimestamp;
    private LocalDateTime onCallEndTimestamp;

    private AgentState phoneActive;
    private LocalDateTime phoneCallPeriodStartTime;
    private Duration phoneCallPeriodDuration;
    private Boolean badLaguageFlag;
    private String badLaguage;
    private CallDirection callDirection;

    private Boolean screenMonitored;

    private Boolean readyState;

    public AgentStatus() {
    }

    public AgentStatus(Agent agent, AgentStats stats, AgentCall activeCall) {
        this.extension = agent.getExtension();
        this.username = agent.getUserName();
//        this.dialerActive = stats.getDialerActive() == Boolean.TRUE ? DialerActiveStatus.ACTIVE : DialerActiveStatus.INACTIVE;
        this.onCallStartTimestamp = agent.getOnCallStartTimestamp();
        this.onCallEndTimestamp = agent.getOnCallEndTimestamp();
        if (stats != null) {
            if (stats.hasStarted()) {
                SetAgentState state = agent.getStatusExt();
                LocalDateTime time = agent.getStatusExtUpdated();
                this.phoneActive = (state != null) ? state.getAgentState() : stats.getState();
                if (phoneActive != null) {
                    readyState = phoneActive.isReadyState();
                } else {
                    readyState = false;
                }
                this.phoneCallPeriodStartTime = (time != null) ? time : stats.getStateStartTime();
                if (this.phoneCallPeriodStartTime != null) {
                    this.phoneCallPeriodDuration = DurationUtils.getDuration(this.phoneCallPeriodStartTime, LocalDateTime.now());
                } else {
                    this.phoneCallPeriodDuration = Duration.ZERO;
                }
            }
            this.dialerActive = stats.isDialerActive() ? DialerActiveStatus.ACTIVE : DialerActiveStatus.INACTIVE;
        }

        if (activeCall != null) {
            this.callDirection = activeCall.getCallDirection();
            BorrowerInfo borrowerInfo = activeCall.getBorrowerInfo();
            if (borrowerInfo != null) {
                this.currentLoanId = borrowerInfo.getLoanId();
                this.currentBorrowerFirstName = borrowerInfo.getBorrowerFirstName();
                this.currentBorrowerLastName = borrowerInfo.getBorrowerLastName();
                this.currentOtherEndNumber = borrowerInfo.getBorrowerPhoneNumber();
            }
            this.badLaguage = activeCall.getBadLanguage();
            badLaguageFlag = badLaguage != null;
        } else {
            badLaguageFlag = false;
        }
        this.lastActivityTime = agent.getLastActivityTime();
        this.lastHartbeatTime = agent.getLastHartbeatTime();
        this.lastOutboundTime = agent.getLastOutboundTime();
        this.lastInboundTime = agent.getLastInboundTime();
        this.screenMonitored = agent.getScreenMonitored();
    }

    public Integer getExtension() {
        return extension;
    }

    public void setExtension(Integer extension) {
        this.extension = extension;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCall_uuid() {
        return call_uuid;
    }

    public void setCall_uuid(String call_uuid) {
        this.call_uuid = call_uuid;
    }

    public String getCurrentAgentFreeswitchUUID() {
        return currentAgentFreeswitchUUID;
    }

    public void setCurrentAgentFreeswitchUUID(String currentAgentFreeswitchUUID) {
        this.currentAgentFreeswitchUUID = currentAgentFreeswitchUUID;
    }

    public Long getCurrentLoanId() {
        return currentLoanId;
    }

    public void setCurrentLoanId(Long currentLoanId) {
        this.currentLoanId = currentLoanId;
    }

    public String getCurrentBorrowerLastName() {
        return currentBorrowerLastName;
    }

    public void setCurrentBorrowerLastName(String currentBorrowerLastName) {
        this.currentBorrowerLastName = currentBorrowerLastName;
    }

    public String getCurrentBorrowerFirstName() {
        return currentBorrowerFirstName;
    }

    public void setCurrentBorrowerFirstName(String currentBorrowerFirstName) {
        this.currentBorrowerFirstName = currentBorrowerFirstName;
    }

    public String getCurrentOtherEndNumber() {
        return currentOtherEndNumber;
    }

    public void setCurrentOtherEndNumber(String currentOtherEndNumber) {
        this.currentOtherEndNumber = currentOtherEndNumber;
    }

    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public LocalDateTime getLastOutboundTime() {
        return lastOutboundTime;
    }

    public void setLastOutboundTime(LocalDateTime lastOutboundTime) {
        this.lastOutboundTime = lastOutboundTime;
    }

    public LocalDateTime getLastInboundTime() {
        return lastInboundTime;
    }

    public void setLastInboundTime(LocalDateTime lastInboundTime) {
        this.lastInboundTime = lastInboundTime;
    }

    public LocalDateTime getLastHartbeatTime() {
        return lastHartbeatTime;
    }

    public void setLastHartbeatTime(LocalDateTime lastHartbeatTime) {
        this.lastHartbeatTime = lastHartbeatTime;
    }

    public DialerActiveStatus getDialerActive() {
        return dialerActive;
    }

    public void setDialerActive(DialerActiveStatus dialerActive) {
        this.dialerActive = dialerActive;
    }

    public LocalDateTime getOnCallStartTimestamp() {
        return onCallStartTimestamp;
    }

    public void setOnCallStartTimestamp(LocalDateTime onCallStartTimestamp) {
        this.onCallStartTimestamp = onCallStartTimestamp;
    }

    public LocalDateTime getOnCallEndTimestamp() {
        return onCallEndTimestamp;
    }

    public void setOnCallEndTimestamp(LocalDateTime onCallEndTimestamp) {
        this.onCallEndTimestamp = onCallEndTimestamp;
    }

    public AgentState getPhoneActive() {
        return phoneActive;
    }

    public void setPhoneActive(AgentState phoneActiveStatus) {
        this.phoneActive = phoneActiveStatus;
    }

    public LocalDateTime getPhoneCallPeriodStartTime() {
        return phoneCallPeriodStartTime;
    }

    public void setPhoneCallPeriodStartTime(LocalDateTime phoneCallPeriodStartTime) {
        this.phoneCallPeriodStartTime = phoneCallPeriodStartTime;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }

    public void setCallDirection(CallDirection callDirection) {
        this.callDirection = callDirection;
    }

    public Boolean getScreenMonitored() {
        return screenMonitored;
    }

    public void setScreenMonitored(Boolean screenMonitored) {
        this.screenMonitored = screenMonitored;
    }

    public Boolean getBadLaguageFlag() {
        return badLaguageFlag;
    }

    public void setBadLaguageFlag(Boolean badLaguageFlag) {
        this.badLaguageFlag = badLaguageFlag;
    }

    public Duration getPhoneCallPeriodDuration() {
        return phoneCallPeriodDuration;
    }

    public void setPhoneCallPeriodDuration(Duration phoneCallPeriodDuration) {
        this.phoneCallPeriodDuration = phoneCallPeriodDuration;
    }

    public String getBadLaguage() {
        return badLaguage;
    }

    public void setBadLaguage(String badLaguage) {
        this.badLaguage = badLaguage;
    }

    public Boolean getReadyState() {
        return readyState;
    }

    public void setReadyState(Boolean readyState) {
        this.readyState = readyState;
    }

}
