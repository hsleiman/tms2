/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.entity.cdr;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.objectbrains.sti.constants.CallerId;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.pojo.BorrowerInfo;
import com.objectbrains.tms.utility.GsonUtility;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.annotations.Index;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
@Entity
@Table(schema = "sti")
public class CallDetailRecordTMS implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Expose
    @Column(updatable = false, nullable = false)
    private LocalDateTime createTimestamp;

    @Expose
    private String dialplan_type;

    @Expose
    private LocalDateTime amdStartWaitForMediaTime;

    @Expose
    private LocalDateTime amdStartTime;
    @Expose
    private LocalDateTime amdEndTime;
    @Expose
    private LocalDateTime amdStartFifoTime;
    @Expose
    private LocalDateTime amdEndFifoTime;
    
    @Expose 
    private String inboundDIDNumber;
    
    @Expose
    private Boolean isOpen;
    
    @Expose
    private Boolean amdDroppedBeforeAgentAnswer = Boolean.FALSE;
    
    @Expose
    private LocalDateTime amdDroppedBeforeAgentAnswerTimestamp;
    
    @Expose
    private Boolean amdFifoDropped = Boolean.FALSE;
    
    @Expose
    private Boolean amdBeforeFifoDropped = Boolean.FALSE;

    @Expose
    private LocalDateTime amdTransferToAgentTime;

    @Expose
    private Integer amdExtTransferTo;

    @Expose
    private Boolean amdConnectToAgentNow;

    @Expose
    private String amd_status;
    
    @Expose 
    private String amd_result;

    @Expose
    private String username;
    @Expose
    private Integer lastAgent;

    @Expose
    private Integer firstAgent;

    @Expose
    private LocalDateTime lastAgentTimestamp;

    @Expose
    private LocalDateTime firstAgentTimestamp;
    
    @Expose
    private Boolean hangupCauseBoolValue;
    @Expose
    private Boolean BridgeHangupCauseBoolValue;
    
    @Expose 
    private LocalDate nextDueDate;

    @Expose
    private String caller_id_name;
    @Expose
    private String caller_id_number;
    @Expose
    private String callee_id_name;
    @Expose
    private String callee_id_number;
    @Expose
    private String effective_caller_id_number;
    @Expose
    @Enumerated(value = EnumType.STRING)
    private CallerId callerId;
    @Expose
    @Index(name = "idx_tms_call_detail_record_call_uuid")
    private String call_uuid;
    @Expose
    @Enumerated(value = EnumType.STRING)
    private CallDirection callDirection;
    @Expose
    @Embedded
    private BorrowerInfo borrowerInfo = new BorrowerInfo();
    @Expose
    private Boolean autoAswer;
    @Expose
    private Boolean dialer = Boolean.FALSE;
    @Expose
    private Long dialerQueueId;
    @Expose
    private Long agentGroupId;
    
    @Expose
    private String dialerQueueName;
    @Expose
    private Long duration;
    @Expose
    private Long answermsec;
    @Expose
    private Long waitmsec;
    @Expose
    private Long progresssec;
    @Expose
    private Long progress_mediasec;

    @Expose
    private String ivrZipCode;
    
    @Expose
    private String ivrSSN;
    
    @Expose
    private Boolean ivrAuthorized = Boolean.FALSE;
    
    @Expose
    private Boolean inboundLeftVoicemail = Boolean.FALSE;

    @Expose
    @Column(length = 3072)
    private String optionText;

    @Expose
    private Long systemDispostionCode;
    @Expose
    private Long userDispostionCode;

    private Boolean ringed = false;
    private LocalDateTime ringedTime;
    private boolean answered;
    private boolean wrapped;
    private boolean callerHangup = false;
    private boolean agentHangup = false;
    private int transferCount = 0;

    private int wrapCount = 0;

    private LocalDateTime answerTime;
    private LocalDateTime wrapTime;
    private LocalDateTime endTimeFromWebsocket;

    @PrePersist
    private void onCreate() {
        createTimestamp = LocalDateTime.now();
        start_time = LocalDateTime.now();
    }

    @Column(updatable = false, nullable = false)
    private LocalDateTime start_time;
    private LocalDateTime end_time;
    private Boolean complete = Boolean.FALSE;
    private Boolean completeFinal = Boolean.FALSE;
    private String call_recording_url;
    
    private Integer lastIvrStep;
    private Integer lastTrasferStep;
    private LocalDateTime lastTrasferStepTimestamp;
    
    @Column(length = 100000)
    private String ado;
    
    private Integer adoAgentAvialable;
    private LocalDateTime adoUpdateDateTime;
    
    @Column(length = 100000)
    private String logCallOrder = "";
    
    private String incomingCallOrderSelected;
    private String multiLine;

    public CallDetailRecordTMS() {
    }

    public CallDetailRecordTMS(String call_uuid) {
        this.call_uuid = call_uuid;
    }

    public String getAmd_status() {
        return amd_status;
    }

    public void setAmd_status(String amd_status) {
        this.amd_status = amd_status;
    }

    public String getAmd_result() {
        return amd_result;
    }

    public void setAmd_result(String amd_result) {
        this.amd_result = amd_result;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getDialplan_type() {
        return dialplan_type;
    }

    public void setDialplan_type(String dialplan_type) {
        this.dialplan_type = dialplan_type;
    }

    public LocalDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(LocalDateTime createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public LocalDateTime getStart_time() {
        return start_time;
    }

    public LocalDateTime getAmdStartWaitForMediaTime() {
        return amdStartWaitForMediaTime;
    }

    public void setAmdStartWaitForMediaTime(LocalDateTime amdStartWaitForMediaTime) {
        this.amdStartWaitForMediaTime = amdStartWaitForMediaTime;
    }

    public LocalDateTime getAmdStartTime() {
        return amdStartTime;
    }

    public void setAmdStartTime(LocalDateTime amdStartTime) {
        this.amdStartTime = amdStartTime;
    }

    public LocalDateTime getAmdEndTime() {
        return amdEndTime;
    }

    public void setAmdEndTime(LocalDateTime amdEndTime) {
        this.amdEndTime = amdEndTime;
    }

    public LocalDateTime getAmdStartFifoTime() {
        return amdStartFifoTime;
    }

    public void setAmdStartFifoTime(LocalDateTime amdStartFifoTime) {
        this.amdStartFifoTime = amdStartFifoTime;
    }

    public LocalDateTime getAmdEndFifoTime() {
        return amdEndFifoTime;
    }

    public void setAmdEndFifoTime(LocalDateTime amdEndFifoTime) {
        this.amdEndFifoTime = amdEndFifoTime;
    }

    public Boolean getAmdFifoDropped() {
        return amdFifoDropped;
    }

    public void setAmdFifoDropped(Boolean amdFifoDropped) {
        this.amdFifoDropped = amdFifoDropped;
    }

    public Boolean getAmdBeforeFifoDropped() {
        return amdBeforeFifoDropped;
    }

    public void setAmdBeforeFifoDropped(Boolean amdBeforeFifoDropped) {
        this.amdBeforeFifoDropped = amdBeforeFifoDropped;
    }

    public Boolean getAmdDroppedBeforeAgentAnswer() {
        return amdDroppedBeforeAgentAnswer;
    }

    public void setAmdDroppedBeforeAgentAnswer(Boolean amdDroppedBeforeAgentAnswer) {
        this.amdDroppedBeforeAgentAnswer = amdDroppedBeforeAgentAnswer;
    }

    public LocalDateTime getAmdDroppedBeforeAgentAnswerTimestamp() {
        return amdDroppedBeforeAgentAnswerTimestamp;
    }

    public void setAmdDroppedBeforeAgentAnswerTimestamp(LocalDateTime amdDroppedBeforeAgentAnswerTimestamp) {
        this.amdDroppedBeforeAgentAnswerTimestamp = amdDroppedBeforeAgentAnswerTimestamp;
    }

    public LocalDateTime getAmdTransferToAgentTime() {
        return amdTransferToAgentTime;
    }

    public void setAmdTransferToAgentTime(LocalDateTime amdTransferToAgentTime) {
        this.amdTransferToAgentTime = amdTransferToAgentTime;
    }

    public Integer getAmdExtTransferTo() {
        return amdExtTransferTo;
    }

    public void setAmdExtTransferTo(Integer amdExtTransferTo) {
        this.amdExtTransferTo = amdExtTransferTo;
    }

    public Boolean getAmdConnectToAgentNow() {
        return amdConnectToAgentNow;
    }

    public void setAmdConnectToAgentNow(Boolean amdConnectToAgentNow) {
        this.amdConnectToAgentNow = amdConnectToAgentNow;
    }

    public void setStart_time(LocalDateTime start_time) {
        this.start_time = start_time;
    }

    public LocalDateTime getEnd_time() {
        return end_time;
    }

    public void setEnd_time(LocalDateTime end_time) {
        this.end_time = end_time;
    }

    public String getCall_recording_url() {
        return call_recording_url;
    }

    public void setCall_recording_url(String call_recording_url) {
        this.call_recording_url = call_recording_url;
    }

    public Boolean getComplete() {
        return complete;
    }
    
    public Boolean speechToTextRequested = false;
    public Boolean speechToTextCompleted = false;
    public Boolean speechToTextError = false;
    public Double speechToTextConfidence = 0d;
    public Double speechToTextConfidenceRight = 0d;
    public Double speechToTextConfidenceLeft = 0d;

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public Boolean getCompleteFinal() {
        return completeFinal;
    }

    public void setCompleteFinal(Boolean completeFinal) {
        this.completeFinal = completeFinal;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getLastAgent() {
        return lastAgent;
    }

    public void setLastAgent(Integer lastAgent) {
        this.lastAgent = lastAgent;
        setLastAgentTimestamp(LocalDateTime.now());
    }

    public void setLastAgentString(String lastAgentString) {
        Integer temp = null;
        try {
            temp = Integer.parseInt(lastAgentString);
        } catch (Exception ex) {
            return;
        }
        setLastAgent(temp);
    }

    public Integer getFirstAgent() {
        return firstAgent;
    }

    public void setFirstAgent(Integer firstAgent) {
        this.firstAgent = firstAgent;
        setFirstAgentTimestamp(LocalDateTime.now());
    }

    public void setFirstAgentString(String firstAgentString) {
        Integer temp = null;
        try {
            temp = Integer.parseInt(firstAgentString);
        } catch (Exception ex) {
            return;
        }
        setFirstAgent(temp);
    }

    public LocalDateTime getLastAgentTimestamp() {
        return lastAgentTimestamp;
    }

    public void setLastAgentTimestamp(LocalDateTime lastAgentTimestamp) {
        this.lastAgentTimestamp = lastAgentTimestamp;
    }

    public LocalDateTime getFirstAgentTimestamp() {
        return firstAgentTimestamp;
    }

    public void setFirstAgentTimestamp(LocalDateTime firstAgentTimestamp) {
        this.firstAgentTimestamp = firstAgentTimestamp;
    }

    public String getCaller_id_name() {
        return caller_id_name;
    }

    public void setCaller_id_name(String caller_id_name) {
        this.caller_id_name = caller_id_name;
    }

    public String getCaller_id_number() {
        return caller_id_number;
    }

    public void setCaller_id_number(String caller_id_number) {
        this.caller_id_number = caller_id_number;
    }

    public String getCallee_id_name() {
        return callee_id_name;
    }

    public void setCallee_id_name(String callee_id_name) {
        this.callee_id_name = callee_id_name;
    }

    public String getCallee_id_number() {
        return callee_id_number;
    }

    public void setCallee_id_number(String callee_id_number) {
        this.callee_id_number = callee_id_number;
    }

    public String getInboundDIDNumber() {
        return inboundDIDNumber;
    }

    public void setInboundDIDNumber(String inboundDIDNumber) {
        this.inboundDIDNumber = inboundDIDNumber;
    }

    public Boolean getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(Boolean isOpen) {
        this.isOpen = isOpen;
    }

    public String getEffective_caller_id_number() {
        return effective_caller_id_number;
    }

    public void setEffective_caller_id_number(String effective_caller_id_number) {
        this.effective_caller_id_number = effective_caller_id_number;
    }

    public CallerId getCallerId() {
        return callerId;
    }

    public void setCallerId(CallerId callerId) {
        this.callerId = callerId;
    }

    public String getCall_uuid() {
        return call_uuid;
    }

    public void setCall_uuid(String call_uuid) {
        this.call_uuid = call_uuid;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }

    public void setCallDirection(CallDirection callDirection) {
        this.callDirection = callDirection;
    }

    public BorrowerInfo getBorrowerInfo() {
        if (borrowerInfo == null) {
            return new BorrowerInfo();
        }
        return borrowerInfo;
    }

    public void setBorrowerInfo(BorrowerInfo borrowerInfo) {
        this.borrowerInfo = borrowerInfo;
    }

    public Boolean getAutoAswer() {
        return autoAswer;
    }

    public void setAutoAswer(Boolean autoAswer) {
        this.autoAswer = autoAswer;
    }

    public Boolean getDialer() {
        return dialer;
    }

    public void setDialer(Boolean dialer) {
        this.dialer = dialer;
    }

    public Long getDialerQueueId() {
        return dialerQueueId;
    }

    public void setDialerQueueId(Long dialerQueueId) {
        this.dialerQueueId = dialerQueueId;
    }

    public Long getAgentGroupId() {
        return agentGroupId;
    }

    public void setAgentGroupId(Long agentGroupId) {
        this.agentGroupId = agentGroupId;
    }

    public String getDialerQueueName() {
        return dialerQueueName;
    }

    public void setDialerQueueName(String dialerQueueName) {
        this.dialerQueueName = dialerQueueName;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getAnswermsec() {
        return answermsec;
    }

    public void setAnswermsec(Long answermsec) {
        this.answermsec = answermsec;
    }

    public Long getWaitmsec() {
        return waitmsec;
    }

    public void setWaitmsec(Long waitmsec) {
        this.waitmsec = waitmsec;
    }

    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

    public String getIvrZipCode() {
        return ivrZipCode;
    }

    public void setIvrZipCode(String ivrZipCode) {
        this.ivrZipCode = ivrZipCode;
    }

    public String getIvrSSN() {
        return ivrSSN;
    }

    public void setIvrSSN(String ivrSSN) {
        this.ivrSSN = ivrSSN;
    }

    public Boolean getIvrAuthorized() {
        return ivrAuthorized;
    }

    public void setIvrAuthorized(Boolean ivrAuthorized) {
        this.ivrAuthorized = ivrAuthorized;
    }

    public Boolean getInboundLeftVoicemail() {
        return inboundLeftVoicemail;
    }

    public void setInboundLeftVoicemail(Boolean inboundLeftVoicemail) {
        this.inboundLeftVoicemail = inboundLeftVoicemail;
    }

    public Boolean getRinged() {
        return ringed;
    }

    public void setRinged(Boolean ringed) {
        this.ringed = ringed;
    }

    public LocalDateTime getRingedTime() {
        return ringedTime;
    }

    public void setRingedTime(LocalDateTime ringedTime) {
        this.ringedTime = ringedTime;
    }
    
    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public boolean isWrapped() {
        return wrapped;
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
        incrementWrapCount();
    }

    public boolean isCallerHangup() {
        return callerHangup;
    }

    public void setCallerHangup(boolean callerHangup) {
        this.callerHangup = callerHangup;
    }

    public boolean isAgentHangup() {
        return agentHangup;
    }

    public void setAgentHangup(boolean agentHangup) {
        this.agentHangup = agentHangup;
    }

    public int getTransferCount() {
        return transferCount;
    }

    public void incrementTransferCount() {
        transferCount++;
    }

    public int getWrapCount() {
        return wrapCount;
    }

    public void incrementWrapCount() {
        wrapCount++;
    }

    public LocalDateTime getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(LocalDateTime answerTime) {
        this.answerTime = answerTime;
    }

    public LocalDateTime getWrapTime() {
        return wrapTime;
    }

    public void setWrapTime(LocalDateTime wrapTime) {
        this.wrapTime = wrapTime;
    }

    public LocalDateTime getEndTimeFromWebsocket() {
        return endTimeFromWebsocket;
    }

    public void setEndTimeFromWebsocket(LocalDateTime endTimeFromWebsocket) {
        this.endTimeFromWebsocket = endTimeFromWebsocket;
    }

    public Long getSystemDispostionCode() {
        return systemDispostionCode;
    }

    public void setSystemDispostionCode(Long systemDispostionCode) {
        this.systemDispostionCode = systemDispostionCode;
    }

    public Long getUserDispostionCode() {
        return userDispostionCode;
    }

    public void setUserDispostionCode(Long userDispostionCode) {
        this.userDispostionCode = userDispostionCode;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Long getProgresssec() {
        return progresssec;
    }

    public void setProgresssec(Long progresssec) {
        this.progresssec = progresssec;
    }

    public Long getProgress_mediasec() {
        return progress_mediasec;
    }

    public void setProgress_mediasec(Long progress_mediasec) {
        this.progress_mediasec = progress_mediasec;
    }

    public Boolean getHangupCauseBoolValue() {
        return hangupCauseBoolValue;
    }

    public void setHangupCauseBoolValue(Boolean hangupCauseBoolValue) {
        this.hangupCauseBoolValue = hangupCauseBoolValue;
    }

    public Boolean getBridgeHangupCauseBoolValue() {
        return BridgeHangupCauseBoolValue;
    }

    public void setBridgeHangupCauseBoolValue(Boolean BridgeHangupCauseBoolValue) {
        this.BridgeHangupCauseBoolValue = BridgeHangupCauseBoolValue;
    }

    public Integer getLastIvrStep() {
        return lastIvrStep;
    }

    public void setLastIvrStep(Integer lastIvrStep) {
        this.lastIvrStep = lastIvrStep;
    }

    public Integer getLastTrasferStep() {
        return lastTrasferStep;
    }

    public void setLastTrasferStep(Integer lastTrasferStep) {
        this.lastTrasferStep = lastTrasferStep;
    }

    public LocalDateTime getLastTrasferStepTimestamp() {
        return lastTrasferStepTimestamp;
    }

    public void setLastTrasferStepTimestamp(LocalDateTime lastTrasferStepTimestamp) {
        this.lastTrasferStepTimestamp = lastTrasferStepTimestamp;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public String getAdo() {
        return ado;
    }

    public void setAdo(String ado) {
        this.ado = ado;
    }

    public Integer getAdoAgentAvialable() {
        return adoAgentAvialable;
    }

    public void setAdoAgentAvialable(Integer adoAgentAvialable) {
        this.adoAgentAvialable = adoAgentAvialable;
    }

    public LocalDateTime getAdoUpdateDateTime() {
        return adoUpdateDateTime;
    }

    public void setAdoUpdateDateTime(LocalDateTime adoUpdateDateTime) {
        this.adoUpdateDateTime = adoUpdateDateTime;
    }
    
    
    
    public String getLogCallOrder() {
        return logCallOrder;
    }

    public void setLogCallOrder(String logCallOrder) {
        this.logCallOrder = logCallOrder;
    }

    public String getIncomingCallOrderSelected() {
        return incomingCallOrderSelected;
    }

    public void setIncomingCallOrderSelected(String incomingCallOrderSelected) {
        this.incomingCallOrderSelected = incomingCallOrderSelected;
    }

    public String getMultiLine() {
        return multiLine;
    }

    public void setMultiLine(String multiLine) {
        this.multiLine = multiLine;
    }

    public Boolean getSpeechToTextRequested() {
        return speechToTextRequested;
    }

    public void setSpeechToTextRequested(Boolean speechToTextRequested) {
        this.speechToTextRequested = speechToTextRequested;
    }

    public Boolean getSpeechToTextCompleted() {
        return speechToTextCompleted;
    }

    public void setSpeechToTextCompleted(Boolean speechToTextCompleted) {
        this.speechToTextCompleted = speechToTextCompleted;
    }

    public Boolean getSpeechToTextError() {
        return speechToTextError;
    }

    public void setSpeechToTextError(Boolean speechToTextError) {
        this.speechToTextError = speechToTextError;
    }

    public Double getSpeechToTextConfidence() {
        return speechToTextConfidence;
    }

    public void setSpeechToTextConfidence(Double speechToTextConfidence) {
        this.speechToTextConfidence = speechToTextConfidence;
    }  

    public Double getSpeechToTextConfidenceRight() {
        return speechToTextConfidenceRight;
    }

    public void setSpeechToTextConfidenceRight(Double speechToTextConfidenceRight) {
        this.speechToTextConfidenceRight = speechToTextConfidenceRight;
    }

    public Double getSpeechToTextConfidenceLeft() {
        return speechToTextConfidenceLeft;
    }

    public void setSpeechToTextConfidenceLeft(Double speechToTextConfidenceLeft) {
        this.speechToTextConfidenceLeft = speechToTextConfidenceLeft;
    }
    
}
