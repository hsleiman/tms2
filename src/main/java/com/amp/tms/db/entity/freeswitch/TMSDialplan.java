/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity.freeswitch;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.objectbrains.enumerated.CallResponseCode;
import com.amp.crm.constants.CallerId;
import com.amp.crm.constants.PopupDisplayMode;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.DialerType;
import com.amp.tms.enumerated.refrence.BeanServices;
import com.amp.tms.freeswitch.dialplan.Dialplan;
import com.amp.tms.freeswitch.dialplan.action.AbstractAction;
import com.amp.tms.hazelcast.keys.TMSDialplanKey;
import com.amp.tms.pojo.BorrowerInfo;
import com.amp.tms.utility.GsonUtility;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
@Entity
@Table(schema = "crm")
public class TMSDialplan implements Serializable {

    private static final int MAX_LENGTH = 20000;

    @Expose
    private Boolean debugOn;

    @Expose
    @EmbeddedId
    private TMSDialplanKey key;

    @Expose
    private String cdr_uuid = UUID.randomUUID().toString();

    @Expose
    private Integer gatewayVersion;
    
    @Expose
    private String conditionField;
    
    @Expose
    private String conditionExpression;

    @Expose
    @Column(length = MAX_LENGTH)
    private String bridges;

    @Expose
    @Column(length = MAX_LENGTH)
    private String actions;

    @Expose
    @Column(length = MAX_LENGTH)
    private String originate;

    @Expose
    private String originateIP;

    @Expose
    private String call_uuid;

    @Expose
    private Boolean uploadRecodingOnCallEnd;
    
    @Expose
    private Integer originalTransferFromExt;

    @Expose
    private Boolean uploadRecodingOnTouch;

    @Expose
    private String uploadRecodingURL;

    @Expose
    private Boolean activateAgentOnCall;

    @Expose
    private Boolean conditionDefault = Boolean.TRUE;

    @Expose
    private Boolean sipCopyCustomHeaders = null;

    @Expose
    private Boolean ignore_disposition = Boolean.FALSE;

    @Expose
    private Boolean answeringMachineDetection = Boolean.FALSE;

    @Expose
    private CallResponseCode callResponseCode;

    @Expose
    private Long systemDispostionCode;
    @Expose
    private Long userDispostionCode;

    @Expose
    @Enumerated(value = EnumType.STRING)
    private CallDirection callDirection;
    
    @Expose
    private Boolean outboundBeepUseNew;

    @Expose
    private String outboundBeepOnDuration;
    
    @Expose
    private String outboundBeepOffDuration;
    
    @Expose
    private String outboundBeepHz;
    
    @Expose
    private String outboundBeepVolume;
    
    @Expose
    private String outboundBeepLapseSpace;

    @Expose
    private Boolean record = Boolean.FALSE;

    @Expose
    private Boolean outboundVendor = Boolean.FALSE;

    @Expose
    private String defaultCallerIdNumber;

    @Expose
    private Long groupPkForTransfer;
    
    @Expose
    private Long queuePkForTransfer;

    @Expose
    private String tms_type;

    @Expose
    private String effective_caller_id_number;

    @Expose
    private String UniqueID;

    @Expose
    private String FifoUniqueID;

    @Expose
    private String ChannelCallUUID;

    @Expose
    @Enumerated(value = EnumType.STRING)
    private CallerId callerId;

    @Expose
    private Long CallerIdNumberMask;

    @Expose
    private String caller;

    @Expose
    private String callee;
    
    @Expose
    private Boolean agentInline;

    @Expose
    private String threadID;

    @Expose
    private Integer maxDelayBeforeAgentAnswer;
//
//    @Expose
//    private Long loanId;
//
//    @Expose
//    private String borrowerLastName;
//
//    @Expose
//    private String borrowerFirstName;
//
//    @Expose
//    private String borrowerPhoneNumber;

    @Expose
    @Embedded
    private BorrowerInfo borrowerInfo = new BorrowerInfo();

    @Expose
    private Boolean autoAswer;

    @Expose
    @Enumerated(value = EnumType.STRING)
    private PopupDisplayMode popupType;

    @Expose
    private Boolean dialer;

    @Expose
    @Enumerated(value = EnumType.STRING)
    private DialerType dialerType;

    @Expose
    private Long dialerQueueId;
    
    @Expose
    private Long agentGroupId;

    @Expose
    @Column(length = MAX_LENGTH)
    private String variables;

    @Expose
    @Column(length = MAX_LENGTH)
    private String xml;

    @Expose
    @Column(length = MAX_LENGTH)
    private String backupXml;

    @Expose
    private long createLife;

    @Expose
    private long elapseLife;

    @Expose
    private LocalDateTime retreived;

    @Expose
    private LocalDateTime cdrDateTime;

    @Expose
    private Boolean completed = Boolean.FALSE;

    @Expose
    private Boolean once = Boolean.TRUE;

    @Expose
    private Integer counter = 0;

    @Expose
    private Boolean ignore_early_media;

    @Expose
    private LocalDateTime createDateTime;

    @Expose
    private String functionCall;

    @Expose
    @Enumerated(EnumType.STRING)
    private BeanServices bean;

    @Expose
    private Integer ivrStepCount = -1;
    
    @Expose
    private Boolean ivrAuthorized;
    
    @Expose
    private Boolean dnc;

    @PrePersist
    public void createTime() {
        createDateTime = LocalDateTime.now();
        createLife = System.currentTimeMillis();
        ivrStepCount = -1;
//        setCreateDateTime(LocalDateTime.now());
//        setCreateLife(System.currentTimeMillis());
    }

    public Boolean getOnce() {
        return once;
    }

    public void setOnce(Boolean once) {
        this.once = once;
    }

    public Boolean getIgnore_early_media() {
        return ignore_early_media;
    }

    public void setIgnore_early_media(Boolean ignore_early_media) {
        this.ignore_early_media = ignore_early_media;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public Boolean isCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public TMSDialplanKey getKey() {
        return key;
    }

    public void setKey(TMSDialplanKey key) {
        this.key = key;
    }

    public LocalDateTime getRetreived() {
        return retreived;
    }

    public void setRetreived(LocalDateTime retreived) {
        this.retreived = retreived;
        setThreadID(Thread.currentThread().getName() + "-" + Thread.currentThread().getId());
    }

    public Boolean getRecord() {
        return record;
    }

    public void setRecord(Boolean record) {
        this.record = record;
    }

    public String getTms_type() {
        return tms_type;
    }

    public void setTms_type(String tms_type) {
        this.tms_type = tms_type;
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

    public String getEffective_caller_id_number() {
        return effective_caller_id_number;
    }

    public void setEffective_caller_id_number(String effective_caller_id_number) {
        this.effective_caller_id_number = effective_caller_id_number;
    }

    public String getUniqueID() {
        return UniqueID;
    }

    public void setUniqueID(String UniqueID) {
        this.UniqueID = UniqueID;
    }

    public String getFifoUniqueID() {
        return FifoUniqueID;
    }

    public void setFifoUniqueID(String FifoUniqueID) {
        this.FifoUniqueID = FifoUniqueID;
    }

    public String getChannelCallUUID() {
        return ChannelCallUUID;
    }

    public void setChannelCallUUID(String ChannelCallUUID) {
        this.ChannelCallUUID = ChannelCallUUID;
    }

    public CallerId getCallerId() {
        return callerId;
    }

    public void setCallerId(CallerId callerId) {
        this.callerId = callerId;
    }

    public Long getCallerIdNumberMask() {
        return CallerIdNumberMask;
    }

    public void setCallerIdNumberMask(Long CallerIdNumberMask) {
        this.CallerIdNumberMask = CallerIdNumberMask;
    }

    public String getCaller() {
        return caller;
    }

    public Long getCallerLong() {
        try {
            return Long.valueOf(caller);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public Integer getCallerInteger() {
        try {
            return Integer.parseInt(caller);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public String getCallee() {
        return callee;
    }

    public Long getCalleeLong() {
        try {
            return Long.valueOf(callee);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public Integer getCalleeInteger() {
        try {
            return Integer.parseInt(callee);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public void setCallee(String callee) {
        this.callee = callee;
    }

    public Boolean getAgentInline() {
        return agentInline;
    }

    public void setAgentInline(Boolean agentInline) {
        this.agentInline = agentInline;
    }

    public BorrowerInfo getBorrowerInfo() {
        if (borrowerInfo == null) {
            borrowerInfo = new BorrowerInfo();
        }
        return borrowerInfo;
    }

    public void setBorrowerInfo(BorrowerInfo borrowerInfo) {
        this.borrowerInfo = borrowerInfo;
    }

//    public Long getLoanId() {
//        return loanId;
//    }
//
//    public void setLoanId(Long loanId) {
//        this.loanId = loanId;
//    }
//
//    public String getBorrowerLastName() {
//        return borrowerLastName;
//    }
//
//    public void setBorrowerLastName(String borrowerLastName) {
//        this.borrowerLastName = borrowerLastName;
//    }
//
//    public String getBorrowerFirstName() {
//        return borrowerFirstName;
//    }
//
//    public void setBorrowerFirstName(String borrowerFirstName) {
//        this.borrowerFirstName = borrowerFirstName;
//    }
//
//    public String getBorrowerPhoneNumber() {
//        return borrowerPhoneNumber;
//    }
//
//    public void setBorrowerPhoneNumber(String borrowerPhoneNumber) {
//        this.borrowerPhoneNumber = borrowerPhoneNumber;
//    }
    public Boolean getAutoAswer() {
        if (autoAswer == null) {
            return Boolean.FALSE;
        }
        return autoAswer;
    }

    public void setAutoAswer(Boolean autoAswer) {
        this.autoAswer = autoAswer;
    }

    public PopupDisplayMode getPopupType() {
        return popupType;
    }

    public void setPopupType(PopupDisplayMode popupType) {
        this.popupType = popupType;
    }

    public boolean getDialer() {
        if (dialer == null) {
            return false;
        }
        return dialer;
    }

    public void setDialer(Boolean dialer) {
        this.dialer = dialer;
    }

    public DialerType getDialerType() {
        return dialerType;
    }

    public void setDialerType(DialerType dialerType) {
        this.dialerType = dialerType;
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
    
    

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public long getCreateLife() {
        return createLife;
    }

    public void setCreateLife(long createLife) {
        this.createLife = createLife;
    }

    public long getElapseLife() {
        return elapseLife;
    }

    public void setElapseLife(long elapseLife) {
        this.elapseLife = elapseLife;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public LocalDateTime getCdrDateTime() {
        return cdrDateTime;
    }

    public void setCdrDateTime(LocalDateTime cdrDateTime) {
        this.cdrDateTime = cdrDateTime;
    }

    public Boolean getDnc() {
        return dnc;
    }

    public void setDnc(Boolean dnc) {
        this.dnc = dnc;
    }

    public Boolean getOutboundVendor() {
        return outboundVendor;
    }

    public void setOutboundVendor(Boolean outboundVendor) {
        this.outboundVendor = outboundVendor;
    }

    public String getOriginate() {
        return originate;
    }

    public void setOriginate(String originate) {
        this.originate = originate;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String sets) {
        this.actions = sets;
    }

    public String getBridges() {
        return bridges;
    }

    public void setBridges(String bridges) {
        this.bridges = bridges;
    }

    public Boolean isDebugOn() {
        if (debugOn == null) {
            return Boolean.FALSE;
        }
        return debugOn;
    }

    public void setDebugOn(Boolean debugOn) {
        this.debugOn = debugOn;
    }

    public String getBackupXml() {
        return backupXml;
    }

    public void setBackupXml(String backupXml) {
        this.backupXml = backupXml;
    }

    public void addBridge(AbstractAction bridge) {
        if (this.bridges == null) {
            this.bridges = bridge.getXML();
        } else {
            this.bridges = this.bridges + bridge.getXML();
        }
    }

    public void addAction(AbstractAction action) {
        if (this.actions == null) {
            this.actions = action.getXML();
        } else {
            this.actions = this.actions + action.getXML();
        }
    }

    public void setXMLFromDialplan() {
        Dialplan dialplan = new Dialplan(this);
        setXml(dialplan.getXML());
    }

    public CallResponseCode getCallResponseCode() {
        return callResponseCode;
    }

    public void setCallResponseCode(CallResponseCode callResponseCode) {
        this.callResponseCode = callResponseCode;
    }

    public Boolean getAnsweringMachineDetection() {
        return answeringMachineDetection;
    }

    public void setAnsweringMachineDetection(Boolean answeringMachineDetection) {
        this.answeringMachineDetection = answeringMachineDetection;
    }

    public String getFunctionCall() {
        return functionCall;
    }

    public void setFunctionCall(String functionCall) {
        this.functionCall = functionCall;
    }

    public BeanServices getBean() {
        return bean;
    }

    public void setBean(BeanServices bean) {
        this.bean = bean;
    }

    public String getCdr_uuid() {
        return cdr_uuid;
    }

    public void setCdr_uuid(String cdr_uuid) {
        this.cdr_uuid = cdr_uuid;
    }

    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

    public Boolean getConditionDefault() {
        if (conditionDefault == null) {
            return Boolean.TRUE;
        }
        return conditionDefault;
    }

    public void setConditionDefault(Boolean conditionDefault) {
        this.conditionDefault = conditionDefault;
    }

    public Boolean getIgnore_disposition() {
        return ignore_disposition;
    }

    public void setIgnore_disposition(Boolean ignore_disposition) {
        this.ignore_disposition = ignore_disposition;
    }

    public Boolean getActivateAgentOnCall() {
        return activateAgentOnCall;
    }

    public void setActivateAgentOnCall(Boolean activateAgentOnCall) {
        this.activateAgentOnCall = activateAgentOnCall;
    }

    public String getOriginateIP() {
        return originateIP;
    }

    public void setOriginateIP(String originateIP) {
        this.originateIP = originateIP;
    }

    public Boolean getSipCopyCustomHeaders() {
        return sipCopyCustomHeaders;
    }

    public void setSipCopyCustomHeaders(Boolean sipCopyCustomHeaders) {
        this.sipCopyCustomHeaders = sipCopyCustomHeaders;
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

    public Boolean getUploadRecodingOnCallEnd() {
        return uploadRecodingOnCallEnd;
    }

    public void setUploadRecodingOnCallEnd(Boolean uploadRecodingOnCallEnd) {
        this.uploadRecodingOnCallEnd = uploadRecodingOnCallEnd;
    }

    public Boolean getUploadRecodingOnTouch() {
        return uploadRecodingOnTouch;
    }

    public void setUploadRecodingOnTouch(Boolean uploadRecodingOnTouch) {
        this.uploadRecodingOnTouch = uploadRecodingOnTouch;
    }

    public String getUploadRecodingURL() {
        return uploadRecodingURL;
    }

    public void setUploadRecodingURL(String uploadRecodingURL) {
        this.uploadRecodingURL = uploadRecodingURL;
    }

    public Integer getGatewayVersion() {
        return gatewayVersion;
    }

    public void setGatewayVersion(Integer gatewayVersion) {
        this.gatewayVersion = gatewayVersion;
    }

    public String getDefaultCallerIdNumber() {
        if (StringUtils.isNotBlank(this.effective_caller_id_number)) {
            return this.effective_caller_id_number;
        }
        return defaultCallerIdNumber;
    }

    public void setDefaultCallerIdNumber(String defaultCallerIdNumber) {
        this.defaultCallerIdNumber = defaultCallerIdNumber;
    }

    public String getOutboundBeepLapseSpace() {
        return outboundBeepLapseSpace;
    }

    public void setOutboundBeepLapseSpace(String outboundBeepLapseSpace) {
        this.outboundBeepLapseSpace = outboundBeepLapseSpace;
    }

    public String getOutboundBeepOnDuration() {
        return outboundBeepOnDuration;
    }

    public void setOutboundBeepOnDuration(String outboundBeepOnDuration) {
        this.outboundBeepOnDuration = outboundBeepOnDuration;
    }

    public String getOutboundBeepOffDuration() {
        return outboundBeepOffDuration;
    }

    public void setOutboundBeepOffDuration(String outboundBeepOffDuration) {
        this.outboundBeepOffDuration = outboundBeepOffDuration;
    }

    public String getOutboundBeepHz() {
        return outboundBeepHz;
    }

    public void setOutboundBeepHz(String outboundBeepHz) {
        this.outboundBeepHz = outboundBeepHz;
    }

    public String getOutboundBeepVolume() {
        return outboundBeepVolume;
    }

    public void setOutboundBeepVolume(String outboundBeepVolume) {
        this.outboundBeepVolume = outboundBeepVolume;
    }

    public Boolean getOutboundBeepUseNew() {
        return outboundBeepUseNew;
    }

    public void setOutboundBeepUseNew(Boolean outboundBeepUseNew) {
        this.outboundBeepUseNew = outboundBeepUseNew;
    }

    public Integer getIvrStepCount() {
        return ivrStepCount;
    }

    public void setIvrStepCount(Integer ivrStepCount) {
        this.ivrStepCount = ivrStepCount;
    }

    public String getThreadID() {
        return threadID;
    }

    public void setThreadID(String threadID) {
        this.threadID = threadID;
    }

    public Integer getMaxDelayBeforeAgentAnswer() {
        return maxDelayBeforeAgentAnswer;
    }

    public void setMaxDelayBeforeAgentAnswer(Integer maxDelayBeforeAgentAnswer) {
        this.maxDelayBeforeAgentAnswer = maxDelayBeforeAgentAnswer;
    }

    public void setMaxDelayBeforeAgentAnswer(Long maxDelayBeforeAgentAnswer) {
        if (maxDelayBeforeAgentAnswer == null) {
            this.maxDelayBeforeAgentAnswer = 30;
        } else {
            this.maxDelayBeforeAgentAnswer = maxDelayBeforeAgentAnswer.intValue();
        }
    }

    public Long getGroupPkForTransfer() {
        return groupPkForTransfer;
    }

    public void setGroupPkForTransfer(Long groupPkForTransfer) {
        this.groupPkForTransfer = groupPkForTransfer;
    }

    public Long getQueuePkForTransfer() {
        return queuePkForTransfer;
    }

    public void setQueuePkForTransfer(Long queuePkForTransfer) {
        this.queuePkForTransfer = queuePkForTransfer;
    }
    
    

    public String getConditionField() {
        return conditionField;
    }

    public void setConditionField(String conditionField) {
        this.conditionField = conditionField;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public Integer getOriginalTransferFromExt() {
        return originalTransferFromExt;
    }

    public void setOriginalTransferFromExt(Integer originalTransferFromExt) {
        this.originalTransferFromExt = originalTransferFromExt;
    }

    public Boolean getIvrAuthorized() {
        return ivrAuthorized;
    }

    public void setIvrAuthorized(Boolean ivrAuthorized) {
        this.ivrAuthorized = ivrAuthorized;
    }
    
    

}
