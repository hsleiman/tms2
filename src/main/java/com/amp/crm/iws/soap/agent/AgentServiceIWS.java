/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.iws.soap.agent;

import com.amp.crm.db.entity.agent.Agent;
import com.amp.crm.db.entity.agent.DialerGroup;
import com.amp.crm.embeddable.WeightedPriority;
import com.amp.crm.exception.CrmException;
import com.amp.crm.service.agent.CrmAgentService;
import com.amp.crm.service.tms.DialerGroupService;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * 
 * 
 */
@WebService(serviceName = "AgentServiceIWS")
public class AgentServiceIWS {
    
    @PostConstruct
    @WebMethod(exclude = true)
    public void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
    
    @Autowired
    CrmAgentService agentService;
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
    
    public DialerGroup createOrUpdateDialerGroup(
        @WebParam(name = "dialerGroup") DialerGroup dialerGroup) throws CrmException{
        return dialerGroupService.createOrUpdateDialerGroup(dialerGroup);
    }
    
    public void setAgentToDialerGroup(
            @WebParam(name = "agentUsername") String agentUsername,
            @WebParam(name = "dialerGroupPk") Long dialerGroupPk,
            @WebParam(name = "weightedPriority") WeightedPriority weightedPriority,
            @WebParam(name = "isLeader") Boolean isLeader,
            @WebParam(name = "allowedAfterHours") Boolean allowedAfterHours) throws CrmException{
        dialerGroupService.setAgentToDialerGroup(agentUsername, dialerGroupPk, weightedPriority, isLeader, allowedAfterHours);
    }
}
