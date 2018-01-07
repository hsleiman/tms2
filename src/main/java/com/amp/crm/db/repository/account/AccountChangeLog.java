/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.account;

import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author Hoang, J, Bishistha
 */
@NamedQueries({
        @NamedQuery(
            name = "AccountChangeLog.locateByPk",
            query = "SELECT s FROM AccountChangeLog s WHERE s.pk = :pk"
        ),
        @NamedQuery(
            name = "AccountChangeLog.locateAllUnprocessedLogs",
            query = "SELECT s FROM AccountChangeLog s WHERE s.status = 0"
        ),
        @NamedQuery(
            name = "AccountChangeLog.locateAllUnprocessedAccounts",
            query = "SELECT s.accountPk FROM AccountChangeLog s WHERE s.status = 0 GROUP BY s.accountPk"
        )
})
@Entity
@Table(schema="sti")
public class AccountChangeLog extends SuperEntity{
    private Long accountPk;
    private int status = 0;
    private String changedByProcess;
    private String propertyName;
    private String value;
    private LocalDateTime creationTimestamp;
    private Long queuePk;

    public Long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(Long accountPk) {
        this.accountPk = accountPk;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getChangedByProcess() {
        return changedByProcess;
    }

    public void setChangedByProcess(String changedByProcess) {
        this.changedByProcess = changedByProcess;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Long getQueuePk() {
        return queuePk;
    }

    public void setQueuePk(Long queuePk) {
        this.queuePk = queuePk;
    }

    
    
    
}
