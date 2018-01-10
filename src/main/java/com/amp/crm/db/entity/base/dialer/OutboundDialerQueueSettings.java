/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.dialer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.amp.crm.common.LocalDateTimeDeserializer;
import com.amp.crm.common.LocalDateTimeSerializer;
import com.amp.crm.constants.CallerId;
import com.amp.crm.constants.DialerMode;
import com.amp.crm.constants.LeaveVoiceMailAtOptions;
import com.amp.crm.constants.PreviewDialerType;
import com.amp.crm.constants.VoiceMailOption;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
@Entity
@Table(schema = "crm")
//@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
//@AuditTable(value = "dialer_ob_setting_history", schema = "svc")
public class OutboundDialerQueueSettings extends DialerQueueSettings {
    
    @Enumerated(EnumType.STRING)
    private DialerMode dialerMode;
    @Enumerated(EnumType.STRING)
    private PreviewDialerType previewDialerType;
    @Enumerated(EnumType.STRING)
    private CallerId callerId;
    private Long callerIdNumber;
    private String voiceMailName;
    private String holdMusicName;
    @XmlElement(required = true)
    private Boolean bestTimeToCall;
    private Long maxDelayCallTime;
    private String dispositionPlanName;
    private Boolean ignoreRecallTimesForSwitchedNumbers;
    private Long maxWaitForResult;
    @Column(precision=10, scale=2)
    private Double predictiveMaxCallDropPercent;
    private Double progressiveCallsPerAgent;
    private Boolean answeringMachineDetection;
    private Boolean interactiveVoiceResponse;
    @Enumerated(EnumType.STRING)
    private VoiceMailOption voiceMailOption;
    private Boolean spillOverActive;    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime scheduledRunTime;
    private Boolean oneTimeUse;
    //comma-delimited phone types
    private String phoneTypesAllowed;
    //comma-delimited phone types to be called in order
    private String dialOrder = "1,0,3,2";
    //comma-delimited enum DialerOrderOptions 
    private String orderByList = "1";
    @Enumerated(EnumType.STRING)
    private LeaveVoiceMailAtOptions leaveVoiceMailAt;
    
    public DialerMode getDialerMode() {
        return dialerMode;
    }
    
    public void setDialerMode(DialerMode dialerMode) {
        this.dialerMode = dialerMode;
    }
    
    public PreviewDialerType getPreviewDialerType() {
        return previewDialerType;
    }
    
    public void setPreviewDialerType(PreviewDialerType previewDialerType) {
        this.previewDialerType = previewDialerType;
    }
    
    public CallerId getCallerId() {
        return callerId;
    }
    
    public void setCallerId(CallerId callerId) {
        this.callerId = callerId;
    }
    
    public Long getCallerIdNumber() {
        return callerIdNumber;
    }
    
    public void setCallerIdNumber(Long callerIdNumber) {
        this.callerIdNumber = callerIdNumber;
    }
    
    public String getVoiceMailName() {
        return voiceMailName;
    }
    
    public void setVoiceMailName(String voiceMailName) {
        this.voiceMailName = voiceMailName;
    }
    
    public String getHoldMusicName() {
        return holdMusicName;
    }
    
    public void setHoldMusicName(String holdMusicName) {
        this.holdMusicName = holdMusicName;
    }
    
    public Boolean isBestTimeToCall() {
        return bestTimeToCall;
    }
    
    public void setBestTimeToCall(Boolean bestTimeToCall) {
        this.bestTimeToCall = bestTimeToCall;
    }
    
    public String getDispositionPlanName() {
        return dispositionPlanName;
    }
    
    public void setDispositionPlanName(String dispositionPlanName) {
        this.dispositionPlanName = dispositionPlanName;
    }
    
    public Boolean isIgnoreRecallTimesForSwitchedNumbers() {
        return ignoreRecallTimesForSwitchedNumbers;
    }
    
    public void setIgnoreRecallTimesForSwitchedNumbers(Boolean ignoreRecallTimesForSwitchedNumbers) {
        this.ignoreRecallTimesForSwitchedNumbers = ignoreRecallTimesForSwitchedNumbers;
    }
    
    public Long getMaxWaitForResult() {
        return maxWaitForResult;
    }
    
    public void setMaxWaitForResult(Long maxWaitForResult) {
        this.maxWaitForResult = maxWaitForResult;
    }    
    
    public Long getMaxDelayCallTime() {
        return maxDelayCallTime;
    }
    
    public void setMaxDelayCallTime(Long maxDelayCallTime) {
        this.maxDelayCallTime = maxDelayCallTime;
    }

    public Double getPredictiveMaxCallDropPercent() {
        return predictiveMaxCallDropPercent;
    }

    public void setPredictiveDropPercent(Double predictiveMaxCallDropPercent) {
        this.predictiveMaxCallDropPercent = predictiveMaxCallDropPercent;
    }

    public Double getProgressiveCallsPerAgent() {
        return progressiveCallsPerAgent;
    }

    public void setProgressiveCallsPerAgent(Double progressiveCallsPerAgent) {
        this.progressiveCallsPerAgent = progressiveCallsPerAgent;
    }
    
    public Boolean isAnsweringMachineDetection() {
        return answeringMachineDetection;
    }

    public void setAnsweringMachineDetection(Boolean answeringMachineDetection) {
        this.answeringMachineDetection = answeringMachineDetection;
    }

    public Boolean isInteractiveVoiceResponse() {
        return interactiveVoiceResponse;
    }

    public void setInteractiveVoiceResponse(Boolean interactiveVoiceResponse) {
        this.interactiveVoiceResponse = interactiveVoiceResponse;
    }

    public VoiceMailOption getVoiceMailOption() {
        return voiceMailOption;
    }

    public void setVoiceMailOption(VoiceMailOption voiceMailOption) {
        this.voiceMailOption = voiceMailOption;
    }

    public Boolean getSpillOverActive() {
        return spillOverActive;
    }

    public void setSpillOverActive(Boolean spillOverActive) {
        this.spillOverActive = spillOverActive;
    }

    public LocalDateTime getScheduledRunTime() {
        return scheduledRunTime;
    }

    public void setScheduledRunTime(LocalDateTime scheduledRunTime) {
        this.scheduledRunTime = scheduledRunTime;
    }

    public Boolean isOneTimeUse() {
        return oneTimeUse;
    }

    public void setOneTimeUse(Boolean oneTimeUse) {
        this.oneTimeUse = oneTimeUse;
    }
    
    public String getPhoneTypesAllowed() {
        return phoneTypesAllowed;
    }

    public void setPhoneTypesAllowed(String phoneTypesAllowed) {
        this.phoneTypesAllowed = phoneTypesAllowed;
    }
    
    @Override
    public void setDialerQueueForSettings(DialerQueue queue) {
        ((OutboundDialerQueue) queue).setDialerQueueSettings(this);
    }

    public String getDialOrder() {
        return dialOrder;
    }

    public void setDialOrder(String dialOrder) {
        while(dialOrder.length() < 7){
             dialOrder = dialOrder+",100";
        }
        this.dialOrder = dialOrder;
    }

    public String getOrderByList() {
        return orderByList;
    }

    public void setOrderByList(String orderByList) {
        this.orderByList = orderByList;
    }

    public LeaveVoiceMailAtOptions getLeaveVoiceMailAt() {
        return leaveVoiceMailAt;
    }

    public void setLeaveVoiceMailAt(LeaveVoiceMailAtOptions leaveVoiceMailAt) {
        this.leaveVoiceMailAt = leaveVoiceMailAt;
    }

    @Override
    public String toStringForHistory() {
        return "dialerQueuePk=" + getDialerQueuePk() 
                + ", popupDisplayMode=" + getPopupDisplayMode() 
                + ", autoAnswerEnabled=" + isAutoAnswerEnabled() 
                + ", priority=" + getWeightedPriority().getPriority() 
                + ", weight=" + getWeightedPriority().getWeight() 
                + ", idleMaxMinutes=" + getIdleMaxMinutes() 
                + ", wrapMaxMinutes=" + getWrapMaxMinutes() 
                + ", startTime=" + getStartTime() 
                + ", endTime=" + getEndTime() 
                + ", dialerSchedule=" + getDialerSchedule().toString() //not being audited
                + ", dialerMode=" + dialerMode 
                + ", previewDialerType=" + previewDialerType 
                + ", callerId=" + callerId 
                + ", callerIdNumber=" + callerIdNumber 
                + ", voiceMailName=" + voiceMailName 
                + ", holdMusicName=" + holdMusicName 
                + ", bestTimeToCall=" + bestTimeToCall 
                + ", maxDelayCallTime=" + maxDelayCallTime 
                + ", progressiveCallsPerAgent=" + progressiveCallsPerAgent 
                + ", answeringMachineDetection=" + answeringMachineDetection 
                + ", interactiveVoiceResponse=" + interactiveVoiceResponse 
                + ", voiceMailOption=" + voiceMailOption 
                + ", phoneTypesAllowed=" + phoneTypesAllowed 
                + ", dialOrder=" + dialOrder 
                + ", orderByList=" + orderByList 
                + ", leaveVoiceMailAt=" + leaveVoiceMailAt;
    }

    public String difference(Object obj) {
        if (obj == null) {
            return "New settings are null";
        }
        if (getClass() != obj.getClass()) {
            return "ClassName differs";
        }
        StringBuilder sb = new StringBuilder();

        final OutboundDialerQueueSettings other = (OutboundDialerQueueSettings) obj;
        sb.append(super.difference(obj));
        if (this.dialerMode != other.dialerMode) {
            sb.append("\nDialerMode [oldValue : ").append(this.dialerMode).append("; newValue : ").append(other.dialerMode).append("]");
        }
        if (this.previewDialerType != other.previewDialerType) {
            sb.append("\nPreviewDialerType [oldValue : ").append(this.previewDialerType).append("; newValue : ").append(other.previewDialerType).append("]");
        }
        if (this.callerId != other.callerId) {
            sb.append("\nCallerId [oldValue : ").append(this.callerId).append("; newValue : ").append(other.callerId).append("]");
        }
        if (!Objects.equals(this.callerIdNumber, other.callerIdNumber)) {
            sb.append("\nCallerIdNumber [oldValue : ").append(this.callerIdNumber).append("; newValue : ").append(other.callerIdNumber).append("]");
        }
        if (!Objects.equals(this.voiceMailName, other.voiceMailName)) {
            sb.append("\nVoiceMailName [oldValue : ").append(this.voiceMailName).append("; newValue : ").append(other.voiceMailName).append("]");
        }
        if (!Objects.equals(this.holdMusicName, other.holdMusicName)) {
            sb.append("\nHoldMusicName [oldValue : ").append(this.holdMusicName).append("; newValue : ").append(other.holdMusicName).append("]");
        }
        if (!Objects.equals(this.bestTimeToCall, other.bestTimeToCall)) {
            sb.append("\nBestTimeToCall [oldValue : ").append(this.bestTimeToCall).append("; newValue : ").append(other.bestTimeToCall).append("]");
        }
        if (!Objects.equals(this.maxDelayCallTime, other.maxDelayCallTime)) {
            sb.append("\nMaxDelayCallTime [oldValue : ").append(this.maxDelayCallTime).append("; newValue : ").append(other.maxDelayCallTime).append("]");
        }
        if (!Objects.equals(this.dispositionPlanName, other.dispositionPlanName)) {
            sb.append("\nDispositionPlanName [oldValue : ").append(this.dispositionPlanName).append("; newValue : ").append(other.dispositionPlanName).append("]");
        }
        if (!Objects.equals(this.ignoreRecallTimesForSwitchedNumbers, other.ignoreRecallTimesForSwitchedNumbers)) {
            sb.append("\nIgnoreRecallTimesForSwitchedNumbers [oldValue : ").append(this.ignoreRecallTimesForSwitchedNumbers).append("; newValue : ").append(other.ignoreRecallTimesForSwitchedNumbers).append("]");
        }
        if (!Objects.equals(this.maxWaitForResult, other.maxWaitForResult)) {
            sb.append("\nMaxWaitForResult [oldValue : ").append(this.maxWaitForResult).append("; newValue : ").append(other.maxWaitForResult).append("]");
        }
        if (!Objects.equals(this.predictiveMaxCallDropPercent, other.predictiveMaxCallDropPercent)) {
            sb.append("\nPredictiveMaxCallDropPercent [oldValue : ").append(this.predictiveMaxCallDropPercent).append("; newValue : ").append(other.predictiveMaxCallDropPercent).append("]");
        }
        if (!Objects.equals(this.progressiveCallsPerAgent, other.progressiveCallsPerAgent)) {
            sb.append("\nProgressiveCallsPerAgent [oldValue : ").append(this.progressiveCallsPerAgent).append("; newValue : ").append(other.progressiveCallsPerAgent).append("]");
        }
        if (!Objects.equals(this.answeringMachineDetection, other.answeringMachineDetection)) {
            sb.append("\nAnsweringMachineDetection [oldValue : ").append(this.answeringMachineDetection).append("; newValue : ").append(other.answeringMachineDetection).append("]");
        }
        if (!Objects.equals(this.interactiveVoiceResponse, other.interactiveVoiceResponse)) {
            sb.append("\nInteractiveVoiceResponse [oldValue : ").append(this.interactiveVoiceResponse).append("; newValue : ").append(other.interactiveVoiceResponse).append("]");
        }
        if (this.voiceMailOption != other.voiceMailOption) {
            sb.append("\nVoiceMailOption [oldValue : ").append(this.voiceMailOption).append("; newValue : ").append(other.voiceMailOption).append("]");
        }
        if (!Objects.equals(this.spillOverActive, other.spillOverActive)) {
            sb.append("\nSpillOverActive [oldValue : ").append(this.spillOverActive).append("; newValue : ").append(other.spillOverActive).append("]");
        }
        if (!Objects.equals(this.scheduledRunTime, other.scheduledRunTime)) {
            sb.append("\nScheduledRunTime [oldValue : ").append(this.scheduledRunTime).append("; newValue : ").append(other.scheduledRunTime).append("]");
        }
        if (!Objects.equals(this.oneTimeUse, other.oneTimeUse)) {
            sb.append("\nOneTimeUse [oldValue : ").append(this.oneTimeUse).append("; newValue : ").append(other.oneTimeUse).append("]");
        }
        if (!Objects.equals(this.phoneTypesAllowed, other.phoneTypesAllowed)) {
            sb.append("\nPhoneTypesAllowed [oldValue : ").append(this.phoneTypesAllowed).append("; newValue : ").append(other.phoneTypesAllowed).append("]");
        }
        if (!Objects.equals(this.dialOrder, other.dialOrder)) {
            sb.append("\nDialOrder [oldValue : ").append(this.dialOrder).append("; newValue : ").append(other.dialOrder).append("]");
        }
        if (!Objects.equals(this.orderByList, other.orderByList)) {
            sb.append("\nOrderByList [oldValue : ").append(this.orderByList).append("; newValue : ").append(other.orderByList).append("]");
        }
        if (this.leaveVoiceMailAt != other.leaveVoiceMailAt) {
            sb.append("\nLeaveVoiceMailAt [oldValue : ").append(this.leaveVoiceMailAt).append("; newValue : ").append(other.leaveVoiceMailAt).append("]");
        }
        
        if (!Objects.equals(this.getStartTime(), other.getStartTime())) {
            sb.append("\nStartTime [oldValue : ").append(this.getStartTime()).append("; newValue : ").append(other.getStartTime()).append("]");
        }
        if (!Objects.equals(this.getEndTime(), other.getEndTime())) {
            sb.append("\nEndTime [oldValue : ").append(this.getEndTime()).append("; newValue : ").append(other.getEndTime()).append("]");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return String.valueOf(this);
        }
    }
    
    
    
}
