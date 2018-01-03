/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull.pojo;

import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.hazelcast.entity.AgentStats;
import com.objectbrains.tms.pojo.AgentStatus;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class QueueAgentStatus extends AgentStatus {

    private boolean leader;

    public QueueAgentStatus(Agent agent, AgentStats stats, AgentCall activeCall, boolean leader) {
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
