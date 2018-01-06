/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amp.crm.constants.DialerQueueSourceType;
import com.amp.crm.constants.DialerQueueType;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import org.joda.time.LocalDateTime;

/**
 *
 * @author David
 */
@Embeddable
public class DialerQueueDetails {

    @Transient
    private long pk;
    @Column(nullable = false)
    @XmlElement(required = true)
    private String queueName;
    private long accountCount;
    private Boolean active = Boolean.TRUE;
    private LocalDateTime lastAccountAssignmentTimestamp;
    @Enumerated(EnumType.STRING)
    @Column(name = "dialer_queue_type", nullable = false, insertable = false, updatable = false)
    @XmlElement(required = true)
    private DialerQueueType dialerQueueType;
    @Column(length = 10485760)
    private String sqlQuery;
    @Column(name = "query_pk", insertable = false, updatable = false)
    private Long queryPk;
    @Column(name = "work_queue_pk", insertable = false, updatable = false)
    private Long workQueuePk;
    private String createdBy;
    @Enumerated(EnumType.STRING)
    private DialerQueueSourceType dialerQueueSourceType;
    private String destinationNumbers;
    @Transient
    private long secondaryGroupPk;
    @Transient
    private String groupName;
    
    @Transient
    private Long tableGroupPk;
    @Transient
    private List<Long> criteriaSetPks = new ArrayList<>();

    
    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }



    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public long getAccountCount() {
        return accountCount;
    }

    public void setAccountCount(long accountCount) {
        this.accountCount = accountCount;
    }

    public LocalDateTime getLastAccountAssignmentTimestamp() {
        return lastAccountAssignmentTimestamp;
    }

    public void setLastAccountAssignmentTimestamp(LocalDateTime lastAccountAssignmentTimestamp) {
        this.lastAccountAssignmentTimestamp = lastAccountAssignmentTimestamp;
    }

    public DialerQueueType getDialerQueueType() {
        return dialerQueueType;
    }

    public void setDialerQueueType(DialerQueueType dialerQueueType) {
        this.dialerQueueType = dialerQueueType;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public Long getQueryPk() {
        return queryPk;
    }

    public void setQueryPk(Long queryPk) {
        this.queryPk = queryPk;
    }

    public Long getWorkQueuePk() {
        return workQueuePk;
    }

    public void setWorkQueuePk(Long workQueuePk) {
        this.workQueuePk = workQueuePk;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public DialerQueueSourceType getDialerQueueSourceType() {
        return dialerQueueSourceType;
    }

    public void setDialerQueueSourceType(DialerQueueSourceType dialerQueueSourceType) {
        this.dialerQueueSourceType = dialerQueueSourceType;
    }

    public Long getTableGroupPk() {
        return tableGroupPk;
    }

    public void setTableGroupPk(Long tableGroupPk) {
        this.tableGroupPk = tableGroupPk;
    }

    public List<Long> getCriteriaSetPks() {
        return criteriaSetPks;
    }

    public void setCriteriaSetPks(List<Long> criteriaSetPks) {
        this.criteriaSetPks = criteriaSetPks;
    }

    public String getDestinationNumbers() {
        return destinationNumbers;
    }

    public void setDestinationNumbers(String destinationNumbers) {
        this.destinationNumbers = destinationNumbers;
    }

    public long getSecondaryGroupPk() {
        return secondaryGroupPk;
    }

    public void setSecondaryGroupPk(long secondaryGroupPk) {
        this.secondaryGroupPk = secondaryGroupPk;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
