/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.account;

import com.amp.crm.db.entity.superentity.SuperEntity;
import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 *
 * @author David
 */
@NamedQueries({
    @NamedQuery(
            name = "GetNextAccountCriteria.locateByPk",
            query = "SELECT s FROM GetNextAccountCriteria s WHERE s.pk = :pk"
    ),
    @NamedQuery(
            name = "GetNextAccountCriteria.locateByQueuePk",
            query = "SELECT s FROM GetNextAccountCriteria s WHERE s.queuePk = :queuePk"
    ),
    @NamedQuery(
            name = "GetNextAccountCriteria.locateByAccountPk",
            query = "SELECT s FROM GetNextAccountCriteria s WHERE s.accountPk = :accountPk"
    ),
    @NamedQuery(
            name = "GetNextAccountCriteria.locateAll",
            query = "SELECT s FROM GetNextAccountCriteria s"
    )

})
@Entity
@Table(schema = "sti")
public class GetNextAccountCriteria extends SuperEntity {

    private Long accountPk;
    private Boolean pendingBk;
    private Integer accountType;
    private LocalDateTime lastContactDateTime;
    private LocalDateTime lastWorkedByQueueOwner;
    private BigDecimal accountBalance;
    private LocalDateTime callbackDateTime;
    //private LocalDate brokenPtpDate;
    //@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")

    private Long queuePk;
    private String queueName;
    private Long portfolioType;
    private Long bucketType;

    private LocalDate ptpDate;
    private Integer ptpStatus;
    private String ptpSetByAgent;

    private Long agentPk;
    private String agentUsername;
    private String agentEmailAddress;

    private Long primaryAgentPk;
    private String primaryAgentUsername;
    private String primaryAgentEmailAddress;

    private LocalDateTime workLogTimestamp;
    private Long workLogType;

    private Integer achBatchType;
    private Long achStatus;
    private LocalDate postingDate;

    private LocalDate earliestDue;
    private LocalDate delinquencyStatus;

    private String assigned_to_agent_email_address;

    public Long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(Long accountPk) {
        this.accountPk = accountPk;
    }

    public Boolean isPendingBk() {
        return pendingBk;
    }

    public void setPendingBk(Boolean pendingBk) {
        this.pendingBk = pendingBk;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public LocalDateTime getLastContactDateTime() {
        return lastContactDateTime;
    }

    public void setLastContactDateTime(LocalDateTime lastContactDateTime) {
        this.lastContactDateTime = lastContactDateTime;
    }

    public LocalDateTime getLastWorkedByQueueOwner() {
        return lastWorkedByQueueOwner;
    }

    public void setLastWorkedByQueueOwner(LocalDateTime lastWorkedByQueueOwner) {
        this.lastWorkedByQueueOwner = lastWorkedByQueueOwner;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public Long getQueuePk() {
        return queuePk;
    }

    public void setQueuePk(Long queuePk) {
        this.queuePk = queuePk;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Long getPortfolioType() {
        return portfolioType;
    }

    public void setPortfolioType(Long portfolioType) {
        this.portfolioType = portfolioType;
    }

    public Long getBucketType() {
        return bucketType;
    }

    public void setBucketType(Long bucketType) {
        this.bucketType = bucketType;
    }

    public LocalDate getPtpDate() {
        return ptpDate;
    }

    public void setPtpDate(LocalDate ptpDate) {
        this.ptpDate = ptpDate;
    }

    public Integer getPtpStatus() {
        return ptpStatus;
    }

    public void setPtpStatus(Integer ptpStatus) {
        this.ptpStatus = ptpStatus;
    }

    public String getPtpSetByAgent() {
        return ptpSetByAgent;
    }

    public void setPtpSetByAgent(String ptpSetByAgent) {
        this.ptpSetByAgent = ptpSetByAgent;
    }

    public Long getAgentPk() {
        return agentPk;
    }

    public void setAgentPk(Long agentPk) {
        this.agentPk = agentPk;
    }

    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }

    public String getAgentEmailAddress() {
        return agentEmailAddress;
    }

    public void setAgentEmailAddress(String agentEmailAddress) {
        this.agentEmailAddress = agentEmailAddress;
    }

    public LocalDateTime getWorkLogTimestamp() {
        return workLogTimestamp;
    }

    public void setWorkLogTimestamp(LocalDateTime workLogTimestamp) {
        this.workLogTimestamp = workLogTimestamp;
    }

    public Long getWorkLogType() {
        return workLogType;
    }

    public void setWorkLogType(Long workLogType) {
        this.workLogType = workLogType;
    }

    public Integer getAchBatchType() {
        return achBatchType;
    }

    public void setAchBatchType(Integer achBatchType) {
        this.achBatchType = achBatchType;
    }

    public Long getAchStatus() {
        return achStatus;
    }

    public void setAchStatus(Long achStatus) {
        this.achStatus = achStatus;
    }

    public LocalDate getEarliestDue() {
        return earliestDue;
    }

    public void setEarliestDue(LocalDate earliestDue) {
        this.earliestDue = earliestDue;
    }

    public LocalDate getDelinquencyStatus() {
        return delinquencyStatus;
    }

    public void setDelinquencyStatus(LocalDate delinquencyStatus) {
        this.delinquencyStatus = delinquencyStatus;
    }

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(LocalDate postingDate) {
        this.postingDate = postingDate;
    }

    public LocalDateTime getCallbackDateTime() {
        return callbackDateTime;
    }

    public void setCallbackDateTime(LocalDateTime callbackDateTime) {
        this.callbackDateTime = callbackDateTime;
    }

    public Long getPrimaryAgentPk() {
        return primaryAgentPk;
    }

    public void setPrimaryAgentPk(Long primaryAgentPk) {
        this.primaryAgentPk = primaryAgentPk;
    }

    public String getPrimaryAgentUsername() {
        return primaryAgentUsername;
    }

    public void setPrimaryAgentUsername(String primaryAgentUsername) {
        this.primaryAgentUsername = primaryAgentUsername;
    }

    public String getPrimaryAgentEmailAddress() {
        return primaryAgentEmailAddress;
    }

    public void setPrimaryAgentEmailAddress(String primaryAgentEmailAddress) {
        this.primaryAgentEmailAddress = primaryAgentEmailAddress;
    }

    public String getAssigned_to_agent_email_address() {
        return assigned_to_agent_email_address;
    }

    public void setAssigned_to_agent_email_address(String assigned_to_agent_email_address) {
        this.assigned_to_agent_email_address = assigned_to_agent_email_address;
    }

}
