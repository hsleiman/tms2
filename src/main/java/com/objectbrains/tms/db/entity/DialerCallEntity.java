/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.entity;

import com.objectbrains.tms.hazelcast.entity.DialerCall;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.annotations.Index;
import org.joda.time.LocalDateTime;

/**
 *
 * @author connorpetty
 */
@Entity(name = "DialerCall")
@Table(schema = "sti")
public class DialerCallEntity extends DialerCall {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stats_pk", referencedColumnName = "pk", insertable = false, updatable = false)
    private DialerStatsEntity dialerStats;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "queue_pk", referencedColumnName = "pk")
    @Index(name = "tms_idx_dialer_call_queue_pk", columnNames = {"queue_pk"})
    private DialerQueue dialerQueue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name = "loan_pk", referencedColumnName = "loan_pk"),
        @JoinColumn(name = "stats_pk", referencedColumnName = "stats_pk")
    }
    )
    @Index(name = "tms_idx_dialer_call_dialer_loan_pk", columnNames = {"loan_pk", "stats_pk"})
    private DialerLoanEntity dialerLoan;

    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = new LocalDateTime();
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public DialerStatsEntity getDialerStats() {
        return dialerStats;
    }

    public void setDialerStats(DialerStatsEntity dialerStats) {
        this.dialerStats = dialerStats;
    }

    public DialerQueue getDialerQueue() {
        return dialerQueue;
    }

    public void setDialerQueue(DialerQueue dialerQueue) {
        this.dialerQueue = dialerQueue;
    }

    public DialerLoanEntity getDialerLoan() {
        return dialerLoan;
    }

    public void setDialerLoan(DialerLoanEntity dialerLoan) {
        this.dialerLoan = dialerLoan;
    }

    @Override
    public Long getLoanPk() {
        return dialerLoan.getLoanPk();
    }

    @Override
    public Long getQueuePk() {
        return dialerQueue.getPk();
    }
}
