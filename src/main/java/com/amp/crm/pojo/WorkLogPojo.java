/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import com.amp.crm.embeddable.WorkLogData;
import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 *
 * @author David
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkLogPojo {

    private long workLogPk;
    private boolean reviewed;
    private Boolean priority;
    private boolean clear;
    private String clearedBy;
    private LocalDate priorityLogExpirationDate;
    private long accountPk;
    private String userName;
    private String logNote;
    private LocalDateTime createTimestamp;
    private String callUUID;
            
    @Embedded
    private WorkLogData workLogData;

    public WorkLogData getWorkLogData() {
        return workLogData;
    }

    public void setWorkLogData(WorkLogData workLogData) {
        this.workLogData = workLogData;
    }
   
    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public Boolean isPriority() {
        return priority;
    }

    public void setPriority(Boolean priority) {
        this.priority = priority;
    }

    public LocalDate getPriorityLogExpirationDate() {
        return priorityLogExpirationDate;
    }

    public void setPriorityLogExpirationDate(LocalDate priorityLogExpirationDate) {
        this.priorityLogExpirationDate = priorityLogExpirationDate;
    }

    public long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(long accountPk) {
        this.accountPk = accountPk;
    }

    public long getWorkLogPk() {
        return workLogPk;
    }

    public void setWorkLogPk(long workLogPk) {
        this.workLogPk = workLogPk;
    }   

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLogNote() {
        return logNote;
    }

    public void setLogNote(String logNote) {
        this.logNote = logNote;
    }

  
    public LocalDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(LocalDateTime createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public boolean isClear() {
        return clear;
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }

    public String getClearedBy() {
        return clearedBy;
    }

    public void setClearedBy(String clearedBy) {
        this.clearedBy = clearedBy;
    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }
    
    

}

