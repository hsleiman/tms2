/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.entity;

import com.google.gson.annotations.Expose;
import com.objectbrains.ams.iws.User;
import com.objectbrains.tms.enumerated.SetAgentState;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.NotAudited;
import org.joda.time.LocalDateTime;

/**
 *
 * @author connorpetty
 */
@Embeddable
public class AgentTMS implements Serializable {

    @Expose
    @Column(insertable = false, updatable = false)
    private Integer extension;

    @Expose
    @Column(insertable = false, updatable = false)
    private String userName;
    
    @Expose
    private String firstName;
    @Expose
    private String lastName;
    
    @Expose
    private String userIP;
    
    @Expose
    private String effectiveCallerId;
    @Expose
    private String phoneNumber;

    @Expose
    @NotAudited
    private String freeswitchDomain;
    @NotAudited
    private String freeswitchIP;
    @NotAudited
    private Integer freeswitchPort;
    @NotAudited
    private String tmsIP;
    @NotAudited
    private Integer tmsPort;
    
    @Expose
    @Enumerated(EnumType.STRING)
    private SetAgentState statusExt;
    @Expose
    private LocalDateTime statusExtUpdated;
    
    @Expose 
    private String lastHangupCause;

    @Expose
    @NotAudited
    private LocalDateTime lastActivityTime;//last save time of any sort.
    @NotAudited
    private LocalDateTime lastOutboundTime;// last outbound call excluding dialer 
    @NotAudited
    private LocalDateTime lastInboundTime; // last inbound call
    @Expose
    @NotAudited
    private LocalDateTime lastHartbeatTime; // from the websocket

    private LocalDateTime onCallStartTimestamp;
    private LocalDateTime onCallEndTimestamp;

    @Expose
    @NotAudited
    private Boolean screenMonitored;

    public AgentTMS() {
    }

    public AgentTMS(User user) {
        this.extension = user.getExtension();
        this.userName = user.getUserName();
        this.freeswitchIP = user.getFreeswitchIP();
        this.freeswitchPort = user.getFreeswitchPort();
        this.tmsIP = user.getTmsIP();
        this.tmsPort = user.getTmsPort();
        this.phoneNumber = user.getPhoneNumber();
        this.effectiveCallerId = user.getEffectiveCallerId();
    }

    public Integer getExtension() {
        return extension;
    }

    public void setExtension(Integer extension) {
        this.extension = extension;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getFreeswitchIP() {
        return freeswitchIP;
    }

    public void setFreeswitchIP(String freeswitchIP) {
        this.freeswitchIP = freeswitchIP;
    }

    public String getFreeswitchDomain() {
        return freeswitchDomain;
    }

    public void setFreeswitchDomain(String freeswitchDomain) {
        this.freeswitchDomain = freeswitchDomain;
    }

    public Integer getFreeswitchPort() {
        return freeswitchPort;
    }

    public void setFreeswitchPort(Integer freeswitchPort) {
        this.freeswitchPort = freeswitchPort;
    }

    public String getTmsIP() {
        return tmsIP;
    }

    public void setTmsIP(String tmsIP) {
        this.tmsIP = tmsIP;
    }

    public Integer getTmsPort() {
        return tmsPort;
    }

    public void setTmsPort(Integer tmsPort) {
        this.tmsPort = tmsPort;
    }

    public Boolean getScreenMonitored() {
        return screenMonitored;
    }

    public void setScreenMonitored(Boolean screenMonitored) {
        this.screenMonitored = screenMonitored;
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

    public String getEffectiveCallerId() {
        return effectiveCallerId;
    }

    public void setEffectiveCallerId(String effectiveCallerId) {
        this.effectiveCallerId = effectiveCallerId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getCallerIdForOutboundCalls(){
        if(StringUtils.isBlank(this.effectiveCallerId)){
            return null;
        }
        return this.effectiveCallerId;
    }

    public SetAgentState getStatusExt() {
        return statusExt;
    }

    public void setStatusExt(SetAgentState statusExt) {
        this.statusExt = statusExt;
    }

    public LocalDateTime getStatusExtUpdated() {
        return statusExtUpdated;
    }

    public void setStatusExtUpdated(LocalDateTime statusExtUpdated) {
        this.statusExtUpdated = statusExtUpdated;
    }

    public String getLastHangupCause() {
        return lastHangupCause;
    }

    public void setLastHangupCause(String lastHangupCause) {
        this.lastHangupCause = lastHangupCause;
    }

    public String getUserIP() {
        return userIP;
    }

    public void setUserIP(String userIP) {
        this.userIP = userIP;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + java.util.Objects.hashCode(this.extension);
        hash = 17 * hash + java.util.Objects.hashCode(this.userName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AgentTMS other = (AgentTMS) obj;
        if (!java.util.Objects.equals(this.extension, other.extension)) {
            return false;
        }
        if (!java.util.Objects.equals(this.userName, other.userName)) {
            return false;
        }
        return true;
    }

}
