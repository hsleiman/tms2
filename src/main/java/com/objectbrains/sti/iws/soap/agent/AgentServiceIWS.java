/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.iws.soap.agent;

import com.objectbrains.sti.db.entity.agent.Agent;
import com.objectbrains.sti.db.entity.agent.DialerGroup;
import com.objectbrains.sti.embeddable.WeightedPriority;
import com.objectbrains.sti.exception.StiException;
import com.objectbrains.sti.service.agent.StiAgentService;
import com.objectbrains.sti.service.tms.DialerGroupService;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * 
 * @author Hoang
 */
@WebService(serviceName = "AgentServiceIWS")
public class AgentServiceIWS {
    
    @PostConstruct
    @WebMethod(exclude = true)
    public void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
    
    @Autowired
    StiAgentService agentService;
    @Autowired
    DialerGroupService dialerGroupService;
    
    public void syncAllUsersFromAMS(){
            agentService.syncAllUsersFromAMS();
    }
    
    public List<Agent> getAllAgents(){
        return agentService.getAllAgents();
    }

    public List<DialerGroup> getAllDialerGroups(){
        return dialerGroupService.getAllDialerGroups();
    }
    
   /*
    public Long createOrUpdatePortfolioQueue(@WebParam(name = "userData") UserData userData,
            @WebParam(name = "svColQueue") SvCollectionQueue svColQueue)
            throws CollectionQueueNotFoundException, CollectionQueueAlreadyExistsException, SvcException {
        ThreadAttributes.set("agent.username", userData);
        return colQueueService.setPortfolioQueue(svColQueue);
    }
    */ 
    
    public DialerGroup createOrUpdateDialerGroup(
        @WebParam(name = "dialerGroup") DialerGroup dialerGroup) throws StiException{
        return dialerGroupService.createOrUpdateDialerGroup(dialerGroup);
    }
    
    public void setAgentToDialerGroup(
            @WebParam(name = "agentUsername") String agentUsername,
            @WebParam(name = "dialerGroupPk") Long dialerGroupPk,
            @WebParam(name = "weightedPriority") WeightedPriority weightedPriority,
            @WebParam(name = "isLeader") Boolean isLeader,
            @WebParam(name = "allowedAfterHours") Boolean allowedAfterHours) throws StiException{
        dialerGroupService.setAgentToDialerGroup(agentUsername, dialerGroupPk, weightedPriority, isLeader, allowedAfterHours);
    }
}
