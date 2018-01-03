/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.entity;

import com.objectbrains.tms.hazelcast.entity.DialerStats;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author connorpetty
 */
@Entity(name = "DialerStats")
@Table(schema = "tms")
public class DialerStatsEntity extends DialerStats {

    @ManyToOne
    @JoinColumn(name = "queue_pk", referencedColumnName = "pk")
    private DialerQueue dialerQueue;

    private LocalDateTime createDate;

    @PrePersist
    protected void onCreate() {
        createDate = new LocalDateTime();
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public DialerQueue getDialerQueue() {
        return dialerQueue;
    }

    public void setDialerQueue(DialerQueue dialerQueue) {
        this.dialerQueue = dialerQueue;
    }
}
