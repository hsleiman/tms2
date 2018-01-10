/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import com.amp.crm.constants.CallDirection;
import com.amp.crm.constants.CallDisposition;
import java.math.BigDecimal;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
public class CallDispositionLogData {
    
      private long accountPk;
    private long dispositionId;
    private String note;
    private String phoneNumber;    
    private String callUUID;
    private boolean priority;
    private LocalDate logExpiryDate;
    private LocalDate ptpDate;
    private BigDecimal ptpAmount;
    private CallDisposition callDisposition;
    private CallDirection callDirection;
    private LocalDateTime callBackTime;
    
    public long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(long accountPk) {
        this.accountPk = accountPk;
    }

    public long getDispositionId() {
        return dispositionId;
    }

    public void setDispositionId(long dispositionId) {
        this.dispositionId = dispositionId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public LocalDate getLogExpiryDate() {
        return logExpiryDate;
    }

    public void setLogExpiryDate(LocalDate logExpiryDate) {
        this.logExpiryDate = logExpiryDate;
    }

    public LocalDate getPtpDate() {
        return ptpDate;
    }

    public void setPtpDate(LocalDate ptpDate) {
        this.ptpDate = ptpDate;
    }

    public BigDecimal getPtpAmount() {
        return ptpAmount;
    }

    public void setPtpAmount(BigDecimal ptpAmount) {
        this.ptpAmount = ptpAmount;
    }

    public CallDisposition getCallDisposition() {
        return callDisposition;
    }

    public void setCallDisposition(CallDisposition callDisposition) {
        this.callDisposition = callDisposition;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }

    public void setCallDirection(CallDirection callDirection) {
        this.callDirection = callDirection;
    }

    public LocalDateTime getCallBackTime() {
        return callBackTime;
    }

    public void setCallBackTime(LocalDateTime callBackTime) {
        this.callBackTime = callBackTime;
    }
}
