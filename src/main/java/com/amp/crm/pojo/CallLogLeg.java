/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import java.util.Objects;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDateTime;

public class CallLogLeg implements Comparable<CallLogLeg> {

    @XmlTransient
    @JsonIgnore
    private Long callDetailRecordPk;

    private String toPhoneNumber;

    private String fromPhoneNumber;

    private String callee;

    private String caller;

    private LocalDateTime startCallTime;

    private double callDuration;
    
    private String callDisposition;
    
    private String userDisposition;
    
    private String agentUserName;

    public CallLogLeg(){
    }
    
    public CallLogLeg(Long callDetailRecordPk, Long callDuration, LocalDateTime startCallTime, String fromPhoneNumber, String callerName, String toPhoneNumber, String calleeName) {
        this.callDetailRecordPk = callDetailRecordPk;
        this.callDuration = callDuration;
        this.startCallTime = startCallTime;
        this.fromPhoneNumber = fromPhoneNumber;
        this.caller = callerName;
        this.toPhoneNumber = toPhoneNumber;
        this.callee = calleeName;
    }

    public String getCallDisposition() {
        return callDisposition;
    }

    public void setCallDisposition(String callDisposition) {
        this.callDisposition = callDisposition;
    }

    public String getUserDisposition() {
        return userDisposition;
    }

    public void setUserDisposition(String userDisposition) {
        this.userDisposition = userDisposition;
    }

    public String getAgentUserName() {
        return agentUserName;
    }

    public void setAgentUserName(String agentUserName) {
        this.agentUserName = agentUserName;
    }

    public Long getCallDetailRecordPk() {
        return callDetailRecordPk;
    }

    public void setCallDetailRecordPk(Long callDetailRecordPk) {
        this.callDetailRecordPk = callDetailRecordPk;
    }

    public String getToPhoneNumber() {
        return toPhoneNumber;
    }

    public void setToPhoneNumber(String toPhoneNumber) {
        this.toPhoneNumber = toPhoneNumber;
    }

    public String getFromPhoneNumber() {
        return fromPhoneNumber;
    }

    public void setFromPhoneNumber(String fromPhoneNumber) {
        this.fromPhoneNumber = fromPhoneNumber;
    }

    public LocalDateTime getStartCallTime() {
        return startCallTime;
    }

    public void setStartCallTime(LocalDateTime startCallTime) {
        this.startCallTime = startCallTime;
    }

    public double getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(double callDuration) {
        this.callDuration = callDuration;
    }

    public String getCalleeName() {
        return callee;
    }

    public void setCalleeName(String callee) {
        this.callee = callee;
    }

    public String getCallerName() {
        return caller;
    }

    public void setCallerName(String caller) {
        this.caller = caller;
    }

    @Override
    public int compareTo(CallLogLeg otherLeg) {
        LocalDateTime otherCallTime = otherLeg.getStartCallTime();
        if (this.startCallTime == null && otherCallTime == null) {
            return 0;
        }
        if (this.startCallTime == null) {
            return -1;
        }
        if (otherCallTime == null) {
            return 1;
        }
        return this.startCallTime.compareTo(otherCallTime);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.toPhoneNumber);
        hash = 97 * hash + Objects.hashCode(this.fromPhoneNumber);
        hash = 97 * hash + Objects.hashCode(this.callee);
        hash = 97 * hash + Objects.hashCode(this.caller);
        hash = 97 * hash + Objects.hashCode(this.startCallTime);
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.callDuration) ^ (Double.doubleToLongBits(this.callDuration) >>> 32));
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
        final CallLogLeg other = (CallLogLeg) obj;
        if (!Objects.equals(this.toPhoneNumber, other.toPhoneNumber)) {
            return false;
        }
        if (!Objects.equals(this.fromPhoneNumber, other.fromPhoneNumber)) {
            return false;
        }
        if (!Objects.equals(this.callee, other.callee)) {
            return false;
        }
        if (!Objects.equals(this.caller, other.caller)) {
            return false;
        }
        if (!Objects.equals(this.startCallTime, other.startCallTime)) {
            return false;
        }
        if (Double.doubleToLongBits(this.callDuration) != Double.doubleToLongBits(other.callDuration)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
