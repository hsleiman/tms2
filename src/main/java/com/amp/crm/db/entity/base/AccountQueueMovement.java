/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.embeddable.QueueMovementPojo;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Entity
@Table(schema = "crm")
@NamedQueries({
        @NamedQuery(
            name = "AccountQueueMovement.LatestActivityForQueue",
            query = "SELECT max(s.queueMovementPojo.createTimestamp) FROM AccountQueueMovement s WHERE s.queueMovementPojo.newQueuePk = :pk"
        ),
        @NamedQuery(
            name = "AccountQueueMovement.LatestQueueMovementForAccount",
            query = "SELECT s FROM AccountQueueMovement s WHERE s.account.pk = :accountPk and s.queueMovementPojo.newPortfolio = :newPortfolio order by s.queueMovementPojo.createTimestamp desc"
        )
})
public class AccountQueueMovement extends SuperEntity{
    private QueueMovementPojo queueMovementPojo;
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_queue_movement_account")
    private Account account;

    @Transient
    private long accountPk;
    
    public QueueMovementPojo getQueueMovementPojo() {
        return queueMovementPojo;
    }

    public void setQueueMovementPojo(QueueMovementPojo queueMovementPojo) {
        this.queueMovementPojo = queueMovementPojo;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(long accountPk) {
        this.accountPk = accountPk;
    }
          
}
