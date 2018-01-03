/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.embeddable;

import com.objectbrains.sti.constants.CallerId;
import com.objectbrains.sti.constants.ContactTimeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

/**
 *
 * @author David
 */
@Embeddable
public class AccountData {

    @Transient
    private long accountPk;
    private Boolean firstPaymentMade;
    private Boolean isCurrent;
    private LocalDateTime lastReviewedDateTime;
    private LocalDateTime lastWorkedDateTime;
    private LocalDateTime lastWorkedDateTimeByQueueOwner;
    private LocalDateTime lastSavedDateTime;
    private LocalDateTime callbackDateTime;
    private String callBackDateTimeSetBy;
    private LocalDateTime workActivityTimestamp;
    private LocalDateTime lastContactTimestamp;
    private LocalDateTime myQueuelastContactTimestamp;
    private Long phoneNumberUsedLastContact;
    private Integer currentPortfolio;
    private LocalDateTime dateLastSkipWork;
    private LocalDateTime welcomeCallTimestamp;
    private String assignedAgent;
    @Enumerated(EnumType.STRING)
    private CallerId callerIdStatus;
    private LocalDateTime callBackShownDateTime;
    private LocalDate cotractCreatedDate;
    private LocalDate contractSignDate;
    private Integer sortNumberInQueue;
    private LocalDateTime importTime;
    private Boolean verbalCeaseAndDesist = Boolean.FALSE;
    private LocalTime bestTimeToCall;
    private Integer sortNumberForAgent = 0;
    private String assignedToAgentByGetNextLoan;
    @Column(length = 1000000)
    private String originalApplicationData;
    private LocalDateTime dialerLeftMessageTime;
    private Boolean dialerInactive;
    private LocalDateTime dialerInactiveExpiryTime;
    private Boolean remoteChecking;
    private LocalDateTime lastLeftMessageTime;
    private Boolean pendingBK;
    
    @Transient
    private ContactTimeType contactType;
    @Transient
    private LocalDateTime contactTime;

    public long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(long accountPk) {
        this.accountPk = accountPk;
    }

    public Boolean isFirstPaymentMade() {
        return firstPaymentMade;
    }

    public void setFirstPaymentMade(Boolean firstPaymentMade) {
        this.firstPaymentMade = firstPaymentMade;
    }

    public Boolean isCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public LocalDateTime getLastReviewedDateTime() {
        return lastReviewedDateTime;
    }

    public void setLastReviewedDateTime(LocalDateTime lastReviewedDateTime) {
        this.lastReviewedDateTime = lastReviewedDateTime;
    }

    public LocalDateTime getLastWorkedDateTime() {
        return lastWorkedDateTime;
    }

    public void setLastWorkedDateTime(LocalDateTime lastWorkedDateTime) {
        this.lastWorkedDateTime = lastWorkedDateTime;
    }

    public LocalDateTime getLastWorkedDateTimeByQueueOwner() {
        return lastWorkedDateTimeByQueueOwner;
    }

    public void setLastWorkedDateTimeByQueueOwner(LocalDateTime lastWorkedDateTimeByQueueOwner) {
        this.lastWorkedDateTimeByQueueOwner = lastWorkedDateTimeByQueueOwner;
    }

    public LocalDateTime getLastSavedDateTime() {
        return lastSavedDateTime;
    }

    public void setLastSavedDateTime(LocalDateTime lastSavedDateTime) {
        this.lastSavedDateTime = lastSavedDateTime;
    }

    public LocalDateTime getCallbackDateTime() {
        return callbackDateTime;
    }

    public void setCallbackDateTime(LocalDateTime callbackDateTime) {
        this.callbackDateTime = callbackDateTime;
    }

    public String getCallBackDateTimeSetBy() {
        return callBackDateTimeSetBy;
    }

    public void setCallBackDateTimeSetBy(String callBackDateTimeSetBy) {
        this.callBackDateTimeSetBy = callBackDateTimeSetBy;
    }

    public LocalDateTime getWorkActivityTimestamp() {
        return workActivityTimestamp;
    }

    public void setWorkActivityTimestamp(LocalDateTime workActivityTimestamp) {
        this.workActivityTimestamp = workActivityTimestamp;
    }

    public LocalDateTime getLastContactTimestamp() {
        return lastContactTimestamp;
    }

    public void setLastContactTimestamp(LocalDateTime lastContactTimestamp) {
        this.lastContactTimestamp = lastContactTimestamp;
    }

    public LocalDateTime getMyQueuelastContactTimestamp() {
        return myQueuelastContactTimestamp;
    }

    public void setMyQueuelastContactTimestamp(LocalDateTime myQueuelastContactTimestamp) {
        this.myQueuelastContactTimestamp = myQueuelastContactTimestamp;
    }

    public Long getPhoneNumberUsedLastContact() {
        return phoneNumberUsedLastContact;
    }

    public void setPhoneNumberUsedLastContact(Long phoneNumberUsedLastContact) {
        this.phoneNumberUsedLastContact = phoneNumberUsedLastContact;
    }

    public Integer getCurrentPortfolio() {
        return currentPortfolio;
    }

    public void setCurrentPortfolio(Integer currentPortfolio) {
        this.currentPortfolio = currentPortfolio;
    }

    public LocalDateTime getDateLastSkipWork() {
        return dateLastSkipWork;
    }

    public void setDateLastSkipWork(LocalDateTime dateLastSkipWork) {
        this.dateLastSkipWork = dateLastSkipWork;
    }

    public LocalDateTime getWelcomeCallTimestamp() {
        return welcomeCallTimestamp;
    }

    public void setWelcomeCallTimestamp(LocalDateTime welcomeCallTimestamp) {
        this.welcomeCallTimestamp = welcomeCallTimestamp;
    }

    public String getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(String assignedAgent) {
        this.assignedAgent = assignedAgent;
    }

    public CallerId getCallerIdStatus() {
        return callerIdStatus;
    }

    public void setCallerIdStatus(CallerId callerIdStatus) {
        this.callerIdStatus = callerIdStatus;
    }

    public LocalDateTime getCallBackShownDateTime() {
        return callBackShownDateTime;
    }

    public void setCallBackShownDateTime(LocalDateTime callBackShownDateTime) {
        this.callBackShownDateTime = callBackShownDateTime;
    }

    public LocalDate getCotractCreatedDate() {
        return cotractCreatedDate;
    }

    public void setCotractCreatedDate(LocalDate cotractCreatedDate) {
        this.cotractCreatedDate = cotractCreatedDate;
    }

    public LocalDate getContractSignDate() {
        return contractSignDate;
    }

    public void setContractSignDate(LocalDate contractSignDate) {
        this.contractSignDate = contractSignDate;
    }

    public Integer getSortNumberInQueue() {
        return sortNumberInQueue;
    }

    public void setSortNumberInQueue(Integer sortNumberInQueue) {
        this.sortNumberInQueue = sortNumberInQueue;
    }

    public LocalDateTime getImportTime() {
        return importTime;
    }

    public void setImportTime(LocalDateTime importTime) {
        this.importTime = importTime;
    }

    public Boolean isVerbalCeaseAndDesist() {
        return verbalCeaseAndDesist;
    }

    public void setVerbalCeaseAndDesist(Boolean verbalCeaseAndDesist) {
        this.verbalCeaseAndDesist = verbalCeaseAndDesist;
    }

    public LocalTime getBestTimeToCall() {
        return bestTimeToCall;
    }

    public void setBestTimeToCall(LocalTime bestTimeToCall) {
        this.bestTimeToCall = bestTimeToCall;
    }

    public Integer getSortNumberForAgent() {
        return sortNumberForAgent;
    }

    public void setSortNumberForAgent(Integer sortNumberForAgent) {
        this.sortNumberForAgent = sortNumberForAgent;
    }

    public String getAssignedToAgentByGetNextLoan() {
        return assignedToAgentByGetNextLoan;
    }

    public void setAssignedToAgentByGetNextLoan(String assignedToAgentByGetNextLoan) {
        this.assignedToAgentByGetNextLoan = assignedToAgentByGetNextLoan;
    }

    public String getOriginalApplicationData() {
        return originalApplicationData;
    }

    public void setOriginalApplicationData(String originalApplicationData) {
        this.originalApplicationData = originalApplicationData;
    }

    public LocalDateTime getDialerLeftMessageTime() {
        return dialerLeftMessageTime;
    }

    public void setDialerLeftMessageTime(LocalDateTime dialerLeftMessageTime) {
        this.dialerLeftMessageTime = dialerLeftMessageTime;
    }

    public Boolean isDialerInactive() {
        return dialerInactive;
    }

    public void setDialerInactive(Boolean dialerInactive) {
        this.dialerInactive = dialerInactive;
    }

    public LocalDateTime getDialerInactiveExpiryTime() {
        return dialerInactiveExpiryTime;
    }

    public void setDialerInactiveExpiryTime(LocalDateTime dialerInactiveExpiryTime) {
        this.dialerInactiveExpiryTime = dialerInactiveExpiryTime;
    }

    public Boolean isRemoteChecking() {
        return remoteChecking;
    }

    public void setRemoteChecking(Boolean remoteChecking) {
        this.remoteChecking = remoteChecking;
    }

    public LocalDateTime getLastLeftMessageTime() {
        return lastLeftMessageTime;
    }

    public void setLastLeftMessageTime(LocalDateTime lastLeftMessageTime) {
        this.lastLeftMessageTime = lastLeftMessageTime;
    }

    public Boolean isPendingBK() {
        return pendingBK;
    }

    public void setPendingBK(Boolean pendingBK) {
        this.pendingBK = pendingBK;
    }

    public ContactTimeType getContactType() {
        return contactType;
    }

    public void setContactType(ContactTimeType contactType) {
        this.contactType = contactType;
    }

    public LocalDateTime getContactTime() {
        return contactTime;
    }

    public void setContactTime(LocalDateTime contactTime) {
        this.contactTime = contactTime;
    }
    
}
