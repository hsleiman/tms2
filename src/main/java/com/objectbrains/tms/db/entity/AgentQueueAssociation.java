/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.entity;

import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.hazelcast.keys.AgentQueueKey;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 *
 * @author connorpetty
 */
@Entity
@Table(schema = "sti")
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
    private DialerQueue dialerQueue;

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

    public DialerQueue getDialerQueue() {
        return dialerQueue;
    }

    public void setDialerQueue(DialerQueue dialerQueue) {
        this.dialerQueue = dialerQueue;
    }

}
