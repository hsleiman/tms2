/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull.pojo;

import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.hazelcast.entity.AgentCall;
import com.amp.tms.hazelcast.entity.AgentStats;
import com.amp.tms.pojo.AgentStatus;

/**
 *
 * 
 */
public class QueueAgentStatus extends AgentStatus {

    private boolean leader;

    public QueueAgentStatus(AgentTMS agent, AgentStats stats, AgentCall activeCall, boolean leader) {
        super(agent, stats, activeCall);
        this.leader = leader;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

}
