/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import com.amp.crm.constants.WorkQueueType;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.joda.time.LocalDateTime;

/**
 *
 * @author David
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkQueueData {

    private String queueName;
    private String queueGroup;
    @XmlTransient
    private Integer queuetype = WorkQueueType.WORK_BY_AGENT_PORTFOLIO_QUEUE;
    private long portfolioType;
    private String portfolioDesc;
    private long queueCount;
    private Boolean defaultPortfolioQueue;
    private Boolean active = Boolean.TRUE;
    private Boolean manual;
    private Boolean doNotCall;
    private String servicingCompany;
    private String createdBy;
    private LocalDateTime lastSortedTimestamp;
    private int nextAccountSequenceNumber;
    private LocalDateTime lastAccountAssignmentTimestamp;
    private Integer lastReturnedAccountSortNumber = 0;
    private Integer lastReturnedSkipTraceNumber = 0;
    
    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueGroup() {
        return queueGroup;
    }

    public void setQueueGroup(String queueGroup) {
        this.queueGroup = queueGroup;
    }

    public long getPortfolioType() {
        return portfolioType;
    }
    
    public void setPortfolioType(long portfolioType) {
        this.portfolioType = portfolioType;
    }

    public String getPortfolioDesc() {
        return portfolioDesc;
    }

    public void setPortfolioDesc(String portfolioDesc) {
        this.portfolioDesc = portfolioDesc;
    }

    public long getQueueCount() {
        return queueCount;
    }

    public void setQueueCount(long queueCount) {
        this.queueCount = queueCount;
    }

    public Boolean getDefaultPortfolioQueue() {
        return defaultPortfolioQueue;
    }

    public void setDefaultPortfolioQueue(Boolean defaultPortfolioQueue) {
        this.defaultPortfolioQueue = defaultPortfolioQueue;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean isManual() {
        return manual;
    }

    public void setManual(Boolean manual) {
        this.manual = manual;
    }

    public Boolean isDoNotCall() {
        return doNotCall;
    }

    public void setDoNotCall(Boolean doNotCall) {
        this.doNotCall = doNotCall;
    }

    public String getServicingCompany() {
        return servicingCompany;
    }

    public void setServicingCompany(String servicingCompany) {
        this.servicingCompany = servicingCompany;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getLastSortedTimestamp() {
        return lastSortedTimestamp;
    }

    public void setLastSortedTimestamp(LocalDateTime lastSortedTimestamp) {
        this.lastSortedTimestamp = lastSortedTimestamp;
    }

    public int getNextAccountSequenceNumber() {
        return nextAccountSequenceNumber;
    }

    public void setNextAccountSequenceNumber(int nextAccountSequenceNumber) {
        this.nextAccountSequenceNumber = nextAccountSequenceNumber;
    }

    public LocalDateTime getLastAccountAssignmentTimestamp() {
        return lastAccountAssignmentTimestamp;
    }

    public void setLastAccountAssignmentTimestamp(LocalDateTime lastAccountAssignmentTimestamp) {
        this.lastAccountAssignmentTimestamp = lastAccountAssignmentTimestamp;
    }

    public Integer getLastReturnedAccountSortNumber() {
        return lastReturnedAccountSortNumber;
    }

    public void setLastReturnedAccountSortNumber(Integer lastReturnedAccountSortNumber) {
        this.lastReturnedAccountSortNumber = lastReturnedAccountSortNumber;
    }

    public Integer getQueuetype() {
        return queuetype;
    }

    public void setQueuetype(Integer queuetype) {
        this.queuetype = queuetype;
    }

    public Integer getLastReturnedSkipTraceNumber() {
        return lastReturnedSkipTraceNumber;
    }

    public void setLastReturnedSkipTraceNumber(Integer lastReturnedSkipTraceNumber) {
        this.lastReturnedSkipTraceNumber = lastReturnedSkipTraceNumber;
    }

    
}
