/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull;

import com.objectbrains.sti.embeddable.AgentWeightPriority;
import com.objectbrains.sti.pojo.AccountCustomerName;
import com.objectbrains.sti.service.dialer.DialerQueueService;
import com.objectbrains.sti.service.tms.TMSService;
import com.objectbrains.tms.hazelcast.entity.AgentTMS;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.hazelcast.entity.AgentStats;
import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.pojo.LoanInfoRecord;
import com.objectbrains.tms.restfull.pojo.QueueAgentStatus;
import com.objectbrains.tms.service.AgentCallService;
import com.objectbrains.tms.service.TMSAgentService;
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
@Path("/queue")
@Produces(MediaType.APPLICATION_JSON)
public class QueueRest {

    @Autowired
    private TMSService tmsIws;
    
    @Autowired
    private DialerQueueService dialerQueueService;

    @Autowired
    private TMSAgentService agentService;

    @Autowired
    private AgentStatsService statsService;

    @Autowired
    private AgentCallService agentCallService;

    @Path("/{queuePk}/loans/count")
    @GET
    public long getLoanCount(@PathParam("queuePk") long queuePk) throws Exception {
        return dialerQueueService.getDialerQueueByPk(queuePk).getAccountCount();
    }

    @Path("/{queuePk}/loans")
    @GET
    public List<LoanInfoRecord> getLoans(@PathParam("queuePk") long queuePk) throws Exception {
        return getLoans(queuePk, 0, (int) getLoanCount(queuePk));
    }

    @Path("/{queuePk}/loans/{page}/{size}")
    @GET
    public List<LoanInfoRecord> getLoans(@PathParam("queuePk") long queuePk,
            @PathParam("page") int page, @PathParam("size") int size) throws Exception {
        List<LoanInfoRecord> retList = new ArrayList<>();
        List<AccountCustomerName> borrowerNames = dialerQueueService.getBasicAccountDataForQueue(queuePk, page, size);
        for (AccountCustomerName borrowerName : borrowerNames) {
            LoanInfoRecord loanRecord = new LoanInfoRecord();
            loanRecord.setFirstName(borrowerName.getFirstName());
            loanRecord.setLastName(borrowerName.getLastName());
            loanRecord.setLoanPk(borrowerName.getAccountPk());
            loanRecord.setCompleted(false);
            retList.add(loanRecord);
        }
        return retList;
    }

    @Path("/{queuePk}/agent/status")
    @GET
    public List<QueueAgentStatus> getAllAgentStatusInQueue(@PathParam("queuePk") int queueId) throws Exception {
        List<QueueAgentStatus> retList = new ArrayList<>();
        List<AgentWeightPriority> awps = dialerQueueService.getAgentWeightPriorityListForDq(queueId);
        Map<String, AgentWeightedPriority> weightedPriorities = Utils.convertToMap(awps);

        List<AgentTMS> agents = agentService.getAgents(weightedPriorities, null, null);
        Map<Integer, AgentStats> stats = statsService.getAgentStats(agents);
        Map<Integer, AgentCall> callMap = agentCallService.getActiveCalls(agents);
        for (AgentTMS agent : agents) {
            int extension = agent.getExtension();
            boolean leader = weightedPriorities.get(agent.getUserName()).getLeader();
            retList.add(new QueueAgentStatus(agent, stats.get(extension), callMap.get(extension), leader));
        }
        return retList;
    }
}
