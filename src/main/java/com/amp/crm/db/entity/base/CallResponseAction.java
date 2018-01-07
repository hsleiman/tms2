/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.base.account.Account;
import com.objectbrains.enumerated.CallResponseCode;
import com.amp.crm.db.entity.base.dialer.DialerQueue;
import com.amp.crm.db.entity.superentity.SuperEntity;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author Hoang, J, Bishistha
 */
@NamedQueries({
    @NamedQuery(
            name = "CallResponseAction.LocateByDialerQueuePk",
            query = "SELECT s FROM CallResponseAction s WHERE s.dialerQueue.pk = :dqPk"
    ), 
    @NamedQuery(
            name = "CallResponseAction.LocateByAccountPk",
            query = "SELECT s FROM CallResponseAction s WHERE s.account.pk = :accountPk"
    ), 
    @NamedQuery(
            name = "CallResponseAction.LocateByDialerQueueAndCode",
            query = "SELECT s FROM CallResponseAction s WHERE s.dialerQueue.pk = :dqPk and s.callResponseCode = :code"
    ),
    @NamedQuery(
            name = "CallResponseAction.LocateByAccountAndCode",
            query = "SELECT s FROM CallResponseAction s WHERE s.account.pk = :accountPk and s.callResponseCode = :code"
    ),
    @NamedQuery(
            name = "CallResponseAction.GetAllDefault",
            query = "SELECT s FROM CallResponseAction s WHERE s.account.pk IS NULL and s.dialerQueue.pk IS NULL"
    ),
    @NamedQuery(
            name = "CallResponseAction.GetDefaultByCode",
            query = "SELECT s FROM CallResponseAction s WHERE s.account.pk IS NULL and s.dialerQueue.pk IS NULL and s.callResponseCode = :code")
})
@Entity
@Table(schema = "sti", 
       uniqueConstraints= {
               @UniqueConstraint(columnNames={"callResponseCode", "dialer_queue_pk"}),
               @UniqueConstraint(columnNames={"callResponseCode", "account_pk"}),
               @UniqueConstraint(columnNames={"callResponseCode", "account_pk", "dialer_queue_pk"})}
)
public class CallResponseAction extends SuperEntity {
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dialer_queue_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_call_response_action_dialer_queue")
    private DialerQueue dialerQueue;
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_call_response_action_account")
    private Account account;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallResponseCode callResponseCode;
    private String description;
    //@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDurationAsString")
    //private Duration recallTime;
    private Long recallTime;
    private Integer recallCount;
    
    public void associateToDialerQueue(DialerQueue dialerQueue){
        this.setDialerQueue(dialerQueue);
        dialerQueue.getCallResponseActions().add(this);
    }

    public void associateToAccount(Account account) {
        this.setAccount(account);
        account.getCallResponseActions().add(this);
    }
    
    public DialerQueue getDialerQueue() {
        return dialerQueue;
    }

    public void setDialerQueue(DialerQueue dialerQueue) {
        this.dialerQueue = dialerQueue;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
    
    public CallResponseCode getCallResponseCode() {
        return callResponseCode;
    }

    public void setCallResponseCode(CallResponseCode callResponseCode) {
        this.callResponseCode = callResponseCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRecallTime() {
        return recallTime;
    }

    public void setRecallTime(Long recallTime) {
        this.recallTime = recallTime;
    }

    public Integer getRecallCount() {
        return recallCount;
    }

    public void setRecallCount(Integer recallCount) {
        this.recallCount = recallCount;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.callResponseCode);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CallResponseAction other = (CallResponseAction) obj;
        if (this.callResponseCode != other.callResponseCode) {
            return false;
        }
        return true;
    }
    
    
}