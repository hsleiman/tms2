/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.db.entity.base.dialer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amp.crm.db.entity.log.WorkCallLog;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import org.joda.time.LocalDateTime;


//@NamedQueries({
//
//})
@Entity
@Table(schema = "crm", uniqueConstraints = {@UniqueConstraint(columnNames = "call_uuid")})
public class CallDetailRecord extends CallDetailRecordAbstract {

    @XmlTransient
    @JsonIgnore
    @OneToMany(mappedBy = "CallDetailRecord")
    private Set<WorkCallLog> workCallLogs = new HashSet<>();

    private String callDisposition;
    private Long callDispositionId;
    private Boolean complete;
    private String callRecordingUrl;
    private String systemDisposition;
    private String userDisposition;
    private Boolean agentHangup;
    private String amdStatus;
    private Boolean dnc;
    private Boolean callerHangup;
    private Integer transfer;
    private Boolean wrapped;
    private Integer wrapCount;
    private Boolean isVoicemail;
    private LocalDateTime dialerLeftMessage;
    private Boolean speechToTextRequested;
    private Boolean speechToTextCompleted;
//    private Boolean badLanguageDetected;
//    private String keyword;
//    private Long callPriority;

    //@Transient
    @Column(name = "count_of_q_m_forms")
    private long countOfQMForms=0;

    @Transient
    @Column(name = "has_sub_calls")
    private boolean hasSubCalls;

    @Transient
    @Column(name = "sub_calls")
    private long subCalls;

    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "CDR")
    private VoiceMail voiceMail;

    public Set<WorkCallLog> getWorkCallLogs() {
        return workCallLogs;
    }

    public void setWorkCallLogs(Set<WorkCallLog> workCallLogs) {
        this.workCallLogs = workCallLogs;
    }

    public Boolean isComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public String getCallRecordingUrl() {
        return callRecordingUrl;
    }

    public void setCallRecordingUrl(String callRecordingUrl) {
        this.callRecordingUrl = callRecordingUrl;
    }

    public String getCallDisposition() {
        return callDisposition;
    }

    public void setCallDisposition(String callDisposition) {
        this.callDisposition = callDisposition;
    }

    public Long getCallDispositionId() {
        return callDispositionId;
    }

    public void setCallDispositionId(Long callDispositionId) {
        this.callDispositionId = callDispositionId;
    }

    public String getSystemDisposition() {
        return systemDisposition;
    }

    public void setSystemDisposition(String systemDisposition) {
        this.systemDisposition = systemDisposition;
    }

    public String getUserDisposition() {
        return userDisposition;
    }

    public void setUserDisposition(String userDisposition) {
        this.userDisposition = userDisposition;
    }

    public Boolean isAgentHangup() {
        return agentHangup;
    }

    public void setAgentHangup(Boolean agentHangup) {
        this.agentHangup = agentHangup;
    }

    public String getAmdStatus() {
        return amdStatus;
    }

    public void setAmdStatus(String amdStatus) {
        this.amdStatus = amdStatus;
    }

    public Boolean isDnc() {
        return dnc;
    }

    public void setDnc(Boolean dnc) {
        this.dnc = dnc;
    }

    public Boolean isCallerHangup() {
        return callerHangup;
    }

    public void setCallerHangup(Boolean callerHangup) {
        this.callerHangup = callerHangup;
    }

    public Integer getTransfer() {
        return transfer;
    }

    public void setTransfer(Integer transfer) {
        this.transfer = transfer;
    }

    public Boolean isWrapped() {
        return wrapped;
    }

    public void setWrapped(Boolean wrapped) {
        this.wrapped = wrapped;
    }

    public Integer getWrapCount() {
        return wrapCount;
    }

    public void setWrapCount(Integer wrapCount) {
        this.wrapCount = wrapCount;
    }

    public Boolean getIsVoicemail() {
        return isVoicemail;
    }

    public void setIsVoicemail(Boolean isVoicemail) {
        this.isVoicemail = isVoicemail;
    }

    public LocalDateTime getDialerLeftMessage() {
        return dialerLeftMessage;
    }

    public void setDialerLeftMessage(LocalDateTime dialerLeftMessage) {
        this.dialerLeftMessage = dialerLeftMessage;
    }

    public VoiceMail getVoiceMail() {
        return voiceMail;
    }

    public void setVoiceMail(VoiceMail voiceMail) {
        this.voiceMail = voiceMail;
    }

    public Boolean isSpeechToTextRequested() {
        return speechToTextRequested;
    }

    public void setSpeechToTextRequested(Boolean speechToTextRequested) {
        this.speechToTextRequested = speechToTextRequested;
    }

    public Boolean isSpeechToTextCompleted() {
        return speechToTextCompleted;
    }

    public void setSpeechToTextCompleted(Boolean speechToTextCompleted) {
        this.speechToTextCompleted = speechToTextCompleted;
    }

    public long getCountOfQMForms() {
        return countOfQMForms;
    }

    public void setCountOfQMForms(long countOfQMForms) {
        this.countOfQMForms = countOfQMForms;
    }

    public boolean isHasSubCalls() {
        return hasSubCalls;
    }

    public void setHasSubCalls(boolean hasSubCalls) {
        this.hasSubCalls = hasSubCalls;
    }

    public long getSubCalls() {
        return subCalls;
    }

    public void setSubCalls(long subCalls) {
        this.subCalls = subCalls;
    }

//    public Boolean isbadLanguageDetected() {
//        return badLanguageDetected;
//    }
//
//    public void setbadLanguageDetected(Boolean badLanguageDetected) {
//        this.badLanguageDetected = badLanguageDetected;
//    }
//    
//    public Long getCallPriority() {
//        return callPriority;
//    }
//
//    public void setcallPriority(Long callPriority) {
//        this.callPriority = callPriority;
//    }
//    
//    public String getKeyword(){
//        return keyword;
//    }
//    
//    public void setKeyword(String keyword){
//        this.keyword = keyword;
//    }

    /*@Override
    public String toString() {
        return "CallDetailRecord{" + "callUUID=" + getCallUUID() + ", callDisposition=" + callDisposition + ", callDispositionId=" + callDispositionId + ", systemDisposition=" + systemDisposition + ", userDisposition=" + userDisposition + ", amdStatus=" + amdStatus + '}';
    }*/

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return String.valueOf(this);
        }
    }

}
