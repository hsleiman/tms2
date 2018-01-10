/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity;

import com.amp.tms.pojo.DialerSchedule;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * 
 */
@Entity(name = "DialerScheduleTms")
@Table(schema = "crm")
public class DialerScheduleEntity extends DialerSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @ManyToOne
    @JoinColumn(name = "queue_pk", referencedColumnName = "pk")
    private DialerQueueTms dialerQueue;

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public DialerQueueTms getDialerQueue() {
        return dialerQueue;
    }

    public void setDialerQueue(DialerQueueTms dialerQueue) {
        this.dialerQueue = dialerQueue;
    }

}
