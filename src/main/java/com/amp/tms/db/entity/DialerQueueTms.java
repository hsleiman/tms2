/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity;

import com.amp.tms.enumerated.DialerType;
import com.amp.tms.hazelcast.entity.WeightedPriority;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author connorpetty
 */
@Entity
@Table(schema = "sti")
public class DialerQueueTms extends WeightedPriority{

    @Id
    private long pk;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_stats_pk", referencedColumnName = "pk")
    private DialerStatsEntity currentQueueStats;

    @Enumerated(value = EnumType.STRING)
    private DialerType type;

    public long getPk() {
        return pk;
    }

    public void setPk(long queuePk) {
        this.pk = queuePk;
    }

    public DialerStatsEntity getCurrentQueueStats() {
        return currentQueueStats;
    }

    public void setCurrentQueueStats(DialerStatsEntity currentQueueStats) {
        this.currentQueueStats = currentQueueStats;
    }

    public DialerType getType() {
        return type;
    }

    public void setType(DialerType type) {
        this.type = type;
    }

}
