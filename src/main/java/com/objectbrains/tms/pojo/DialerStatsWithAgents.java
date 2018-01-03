/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.pojo;

import com.objectbrains.tms.hazelcast.entity.DialerStats;
import com.objectbrains.tms.pojo.AgentStatus;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author connorpetty
 */
public class DialerStatsWithAgents {

    private long queuePk;

    private DialerStats dialerStats;

    private final List<AgentStatus> agentStatuses = new ArrayList<>();

    public DialerStatsWithAgents() {
    }
 
    public DialerStatsWithAgents(long queuePk, DialerStats dialerStats) {
        this.queuePk = queuePk;
        this.dialerStats = dialerStats;
    }
    
    public DialerStats getDailerStatus() {
        return dialerStats;
    }

    public List<AgentStatus> getAgentStatuses() {
        return agentStatuses;
    }

    public long getQueuePk() {
        return queuePk;
    }

}
