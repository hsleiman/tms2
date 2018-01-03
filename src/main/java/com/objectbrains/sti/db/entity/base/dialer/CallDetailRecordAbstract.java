/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.base.dialer;

import com.objectbrains.sti.constants.CallDirection;
import com.objectbrains.sti.constants.CallerIdEnum;
import com.objectbrains.sti.constants.DialPlanContext;
import com.objectbrains.sti.constants.DialerMode;
import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.xml.bind.annotation.XmlTransient;
import org.joda.time.LocalDateTime;


@MappedSuperclass
public abstract class CallDetailRecordAbstract extends SuperEntity {
    
    private String username;
    private String calleeIdNumber;
    private String callerIdNumber;
    private String calleeIdName;
    private String callerIdName;
    private String destinationNumber;

    private String effectiveCallerIdNumber;
    private CallerIdEnum callerId;
    private String networkAddr;
    
    @Column(nullable = false, name = "call_uuid")
    private String callUUID;
    @Column(name = "channel_uuid")
    private String channelUUID;
    private Integer orderPower;
    @Enumerated(EnumType.STRING)
    private DialPlanContext context;
    @Enumerated(EnumType.STRING)
    private CallDirection callDirection;

    private Long accountPk;
    private String borrowerLastName;
    private String borrowerFirstName;
    private String borrowerPhoneNumber;

    private Boolean autoAnswer;
    private Boolean dialer;
    private String dialerQueueName;
    @Enumerated(EnumType.STRING)
    private DialerMode dialerMode;
    private Long dialerQueuePk;
    private Long answerInMilliSec;
    private Long waitInMilliSec;
    private Long callDurationInMilliSec;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime answerTime;
    
    private Boolean badLanguage;
    private Boolean badBehavior;
    private String badLanguageText;
    

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCalleeIdNumber() {
        return calleeIdNumber;
    }

    public void setCalleeIdNumber(String calleeIdNumber) {
        this.calleeIdNumber = calleeIdNumber;
    }

    public String getCallerIdNumber() {
        return callerIdNumber;
    }

    public void setCallerIdNumber(String callerIdNumber) {
        this.callerIdNumber = callerIdNumber;
    }

    public String getCalleeIdName() {
        return calleeIdName;
    }

    public void setCalleeIdName(String calleeIdName) {
        this.calleeIdName = calleeIdName;
    }

    public String getCallerIdName() {
        return callerIdName;
    }

    public void setCallerIdName(String callerIdName) {
        this.callerIdName = callerIdName;
    }

    public String getDestinationNumber() {
        return destinationNumber;
    }

    public void setDestinationNumber(String destinationNumber) {
        this.destinationNumber = destinationNumber;
    }

    public String getEffectiveCallerIdNumber() {
        return effectiveCallerIdNumber;
    }

    public void setEffectiveCallerIdNumber(String effectiveCallerIdNumber) {
        this.effectiveCallerIdNumber = effectiveCallerIdNumber;
    }

    public CallerIdEnum getCallerId() {
        return callerId;
    }

    public void setCallerId(CallerIdEnum callerId) {
        this.callerId = callerId;
    }

    public String getNetworkAddr() {
        return networkAddr;
    }

    public void setNetworkAddr(String networkAddr) {
        this.networkAddr = networkAddr;
    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

    public String getChannelUUID() {
        return channelUUID;
    }

    public void setChannelUUID(String channelUUID) {
        this.channelUUID = channelUUID;
    }

    public Integer getOrderPower() {
        return orderPower;
    }

    public void setOrderPower(Integer orderPower) {
        this.orderPower = orderPower;
    }

    public DialPlanContext getContext() {
        return context;
    }

    public void setContext(DialPlanContext context) {
        this.context = context;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }

    public void setCallDirection(CallDirection callDirection) {
        this.callDirection = callDirection;
    }

    public Long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(Long accountPk) {
        this.accountPk = accountPk;
    }

    public String getBorrowerLastName() {
        return borrowerLastName;
    }

    public void setBorrowerLastName(String borrowerLastName) {
        this.borrowerLastName = borrowerLastName;
    }

    public String getBorrowerFirstName() {
        return borrowerFirstName;
    }

    public void setBorrowerFirstName(String borrowerFirstName) {
        this.borrowerFirstName = borrowerFirstName;
    }

    public Boolean isAutoAnswer() {
        return autoAnswer;
    }

    public void setAutoAnswer(Boolean autoAnswer) {
        this.autoAnswer = autoAnswer;
    }

    public Boolean isDialer() {
        return dialer;
    }

    public void setDialer(Boolean dialer) {
        this.dialer = dialer;
    }

    public Long getDialerQueuePk() {
        return dialerQueuePk;
    }

    public void setDialerQueuePk(Long dialerQueuePk) {
        this.dialerQueuePk = dialerQueuePk;
    }

    public Long getAnswerInMilliSec() {
        return answerInMilliSec;
    }

    public void setAnswerInMilliSec(Long answerInMilliSec) {
        this.answerInMilliSec = answerInMilliSec;
    }

    public Long getWaitInMilliSec() {
        return waitInMilliSec;
    }

    public void setWaitInMilliSec(Long waitInMilliSec) {
        this.waitInMilliSec = waitInMilliSec;
    }

    public Long getCallDurationInMilliSec() {
        return callDurationInMilliSec;
    }

    public void setCallDurationInMilliSec(Long callDurationInMilliSec) {
        this.callDurationInMilliSec = callDurationInMilliSec;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(LocalDateTime answerTime) {
        this.answerTime = answerTime;
    }

    public Boolean isBadLanguage() {
        return badLanguage;
    }

    public void setBadLanguage(Boolean badLanguage) {
        this.badLanguage = badLanguage;
    }

    public Boolean isBadBehavior() {
        return badBehavior;
    }

    public void setBadBehavior(Boolean badBehavior) {
        this.badBehavior = badBehavior;
    }

    public String getBadLanguageText() {
        return badLanguageText;
    }

    public void setBadLanguageText(String badLanguageText) {
        this.badLanguageText = badLanguageText;
    }

    public String getDialerQueueName() {
        return dialerQueueName;
    }

    public void setDialerQueueName(String dialerQueueName) {
        this.dialerQueueName = dialerQueueName;
    }

    public DialerMode getDialerMode() {
        return dialerMode;
    }

    public void setDialerMode(DialerMode dialerMode) {
        this.dialerMode = dialerMode;
    }

    public String getBorrowerPhoneNumber() {
        return borrowerPhoneNumber;
    }

    public void setBorrowerPhoneNumber(String borrowerPhoneNumber) {
        this.borrowerPhoneNumber = borrowerPhoneNumber;
    }

}

