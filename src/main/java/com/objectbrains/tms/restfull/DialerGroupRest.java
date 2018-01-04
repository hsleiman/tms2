/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull;

import com.objectbrains.sti.embeddable.AgentWeightPriority;
import com.objectbrains.sti.exception.StiException;
import com.objectbrains.sti.service.tms.TMSService;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.hazelcast.entity.AgentStats;
import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.restfull.pojo.QueueAgentStatus;
import com.objectbrains.tms.service.AgentCallService;
import com.objectbrains.tms.service.AgentService;
import com.objectbrains.tms.service.AgentStatsService;
import com.objectbrains.tms.service.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
@Path("/group")
@Produces(MediaType.APPLICATION_JSON)
public class DialerGroupRest {

    @Autowired
    private TMSService tmsIws;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentStatsService statsService;

    @Autowired
    private AgentCallService agentCallService;

    @Path("/{groupPk}/agent/status")
    @GET
    public List<QueueAgentStatus> getAllAgentStatusInQueue(@PathParam("groupPk") int groupPk) throws StiException {
        List<QueueAgentStatus> retList = new ArrayList<>();
        List<AgentWeightPriority> awps = tmsIws.getAgentWeightPriorityListForGroup(groupPk);
        Map<String, AgentWeightedPriority> weightedPriorities = Utils.convertToMap(awps);

        List<Agent> agents = agentService.getAgents(weightedPriorities, null, null);
        Map<Integer, AgentStats> stats = statsService.getAgentStats(agents);
        Map<Integer, AgentCall> callMap = agentCallService.getActiveCalls(agents);
        for (Agent agent : agents) {
            int extension = agent.getExtension();
            boolean leader = weightedPriorities.get(agent.getUserName()).getLeader();
            retList.add(new QueueAgentStatus(agent, stats.get(extension), callMap.get(extension), leader));
        }
        return retList;
    }
}
