/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity;

import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import com.amp.tms.hazelcast.keys.AgentQueueKey;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 *
 * 
 */
@Entity
@Table(schema = "crm")
public class AgentQueueAssociation extends AgentWeightedPriority{

    @EmbeddedId
    private AgentQueueKey pk;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("extension")
    @JoinColumn(name = "agent_extension", referencedColumnName = "extension")
    private AgentRecord agent;

    @ManyToOne
    @MapsId("queuePk")
    @JoinColumn(name = "queue_pk", referencedColumnName = "pk")
    private DialerQueueTms dialerQueue;

    public AgentQueueKey getPk() {
        return pk;
    }

    public void setPk(AgentQueueKey pk) {
        this.pk = pk;
    }

    public AgentRecord getAgent() {
        return agent;
    }

    public void setAgent(AgentRecord agent) {
        this.agent = agent;
    }

    public DialerQueueTms getDialerQueue() {
        return dialerQueue;
    }

    public void setDialerQueue(DialerQueueTms dialerQueue) {
        this.dialerQueue = dialerQueue;
    }

}
