/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.dialer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.CallResponseAction;
import com.amp.crm.db.entity.base.WorkQueue;
import com.amp.crm.db.entity.disposition.CallDispositionGroup;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.embeddable.DialerQueueDetails;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.ForeignKey;
import org.joda.time.LocalDateTime;

@NamedQueries({
    @NamedQuery(
            name = "DialerQueue.LocateByNameAndType",
            query = "SELECT s FROM DialerQueue s where "
            + "LOWER(TRIM(dialerQueueDetails.queueName)) = LOWER(TRIM(:queueName)) "
            + "and s.dialerQueueDetails.dialerQueueType = :dqType"
    ),
    @NamedQuery(
            name = "DialerQueue.LocateAll",
            query = "SELECT s FROM DialerQueue s ORDER BY s.dialerQueueDetails.queueName"
    ),
    @NamedQuery(
            name = "DialerQueue.LocateAllOrderedByDate",
            query = "SELECT s FROM DialerQueue s ORDER BY coalesce(s.updatedTime, s.createdTime)"
    ),

    @NamedQuery(
            name = "DialerQueue.GetAllDialerQueuesByType",
            query = "SELECT s FROM DialerQueue s where s.dialerQueueDetails.dialerQueueType = :dqType ORDER BY s.dialerQueueDetails.queueName"
    ),
    @NamedQuery(
            name = "DialerQueue.LocateAccountInDialerQueue",
            query = "SELECT account FROM Account account where account.pk = :accountPk and (account.inboundDialerQueue.pk = :dialerQueuePk or account.outboundDialerQueue.pk = :dialerQueuePk)"
    ),
    @NamedQuery(
            name = "DialerQueue.GetAllAccounts",
            //            query = "SELECT SIZE(s.Accounts) FROM DialerQueue s where s = :dialerQueue"
            query = "SELECT account.pk " + DialerQueue.GET_ALL_ACCOUNTS_QUERY + " ORDER BY account.pk"
    ),
    @NamedQuery(
            name = "DialerQueue.AccountCount",
            query = "SELECT COUNT(account) " + DialerQueue.GET_ALL_ACCOUNTS_QUERY
    ),
//    @NamedQuery(
//            name = "DialerQueue.LocateByQueryPk",
//            query = "SELECT s FROM DialerQueue s where s.query.pk = :queryPk and s.dialerQueueDetails.dialerQueueType = :dqType"
//    ),
//    @NamedQuery(
//            name = "DialerQueue.LocateWithQueryPk",
//            query = "SELECT s FROM DialerQueue s where s.query.pk = :queryPk"
//    ),
    @NamedQuery(
            name = "DialerQueue.LocateByWorkQueuePk",
            query = "SELECT s FROM DialerQueue s where s.workQueue.pk = :workQueuePk and s.dialerQueueDetails.dialerQueueType = :dqType"
    ),
    @NamedQuery(
            name = "InboundDialerQueue.RemoveAccounts",
            query = "UPDATE Account set inboundDialerQueue.pk = null where inboundDialerQueue.pk = :dialerQueuePk and (pk in (:accounts) or (:accounts) is null)"
    ),
    @NamedQuery(
            name = "OutboundDialerQueue.RemoveAccounts",
            query = "UPDATE Account set outboundDialerQueue.pk = null where outboundDialerQueue.pk = :dialerQueuePk and (pk in (:accounts) or (:accounts) is null)"
    ),
    @NamedQuery(
            name = "InboundDialerQueue.AddAccounts",
            query = "UPDATE Account set inboundDialerQueue.pk = :dialerQueuePk where pk in (:accounts)"
    ),
    @NamedQuery(
            name = "OutboundDialerQueue.AddAccounts",
            query = "UPDATE Account set outboundDialerQueue.pk = :dialerQueuePk where pk in (:accounts)"
    ),
    @NamedQuery(
            name = "InboundDialerQueue.LocateDQForAccounts",
            query = "SELECT DISTINCT inboundDialerQueue.pk FROM Account where pk in (:accounts) and inboundDialerQueue.pk is not null"
    ),
    @NamedQuery(
            name = "OutboundDialerQueue.LocateDQForAccounts",
            query = "SELECT DISTINCT outboundDialerQueue.pk FROM Account where pk in (:accounts) and outboundDialerQueue.pk is not null"
    )
})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dialer_queue_type", discriminatorType = DiscriminatorType.STRING)
@Table(schema = "crm", name = "dialer_queue",
        uniqueConstraints = @UniqueConstraint(columnNames = {"queueName", "dialer_queue_type"}))
@DiscriminatorOptions(force = true)
public abstract class DialerQueue extends SuperEntity {

    static final String GET_ALL_ACCOUNTS_QUERY = " FROM Account account WHERE account.inboundDialerQueue.pk = :dialerQueuePk or account.outboundDialerQueue.pk = :dialerQueuePk";

    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "dialerQueue", cascade = CascadeType.ALL)
    private Set<CallResponseAction> callResponseActions = new HashSet<>(0);

    @XmlTransient
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "work_queue_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_dialer_queue_work")
    private WorkQueue workQueue;

    @XmlTransient
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "disposition_group_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_call_disposition_group")
    private CallDispositionGroup callDispositionGroup;

    /*@XmlTransient
     @OneToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "query_pk", referencedColumnName = "pk")
     @ForeignKey(name = "fk_dialer_queue_query")
     private Query query;*/
    @Embedded
    private DialerQueueDetails dialerQueueDetails;

    public abstract void setDialerQueueForAccount(Account account, DialerQueue queue);

    public abstract DialerQueueSettings getDialerQueueSettings();

    @Transient
    private Boolean outbound;

    public boolean isOutbound() {
        if (outbound == null) {
            outbound = this instanceof OutboundDialerQueue;
        }
        return outbound;
    }

    public void associateAccountToDQ(Account account) {
        setDialerQueueForAccount(account, this);
        this.getDialerQueueDetails().setLastAccountAssignmentTimestamp(LocalDateTime.now());
    }

    public void disassociateAccountFromDQ(Account account) {
        setDialerQueueForAccount(account, null);
    }

    public void associateToDispositionGroup(CallDispositionGroup dispositionGroup) {
        setCallDispositionGroup(dispositionGroup);
        dispositionGroup.getDialerQueues().add(this);
    }

    public void disassociateFromDispositionGroup() {
        if (callDispositionGroup != null) {
            getCallDispositionGroup().getDialerQueues().remove(this);
            setCallDispositionGroup(null);
        }
    }

    public Set<CallResponseAction> getCallResponseActions() {
        return callResponseActions;
    }

    public void setDqCallResponseActions(Set<CallResponseAction> callResponseActions) {
        this.callResponseActions = callResponseActions;
    }

    public WorkQueue getWorkQueue() {
        return workQueue;
    }

    public void setWorkQueue(WorkQueue workQueue) {
        this.workQueue = workQueue;
    }

    public DialerQueueDetails getDialerQueueDetails() {
        if (dialerQueueDetails == null) {
            dialerQueueDetails = new DialerQueueDetails();
        }
        dialerQueueDetails.setPk(this.getPk());
        return dialerQueueDetails;
    }

    public void setDialerQueueDetails(DialerQueueDetails dialerQueueDetails) {
        this.dialerQueueDetails = dialerQueueDetails;
    }

    public CallDispositionGroup getCallDispositionGroup() {
        return callDispositionGroup;
    }

    public void setCallDispositionGroup(CallDispositionGroup callDispositionGroup) {
        this.callDispositionGroup = callDispositionGroup;
    }

}
