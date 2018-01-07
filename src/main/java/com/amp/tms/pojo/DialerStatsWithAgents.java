/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo;

import com.amp.tms.hazelcast.entity.DialerStats;
import com.amp.tms.pojo.AgentStatus;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hoang, J, Bishistha
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
