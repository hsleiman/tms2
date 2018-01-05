/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.tms.db.entity.Chat;
import com.objectbrains.tms.hazelcast.entity.AgentStats;
import com.objectbrains.tms.pojo.AgentChat;
import com.objectbrains.tms.pojo.PostChatBody;
import com.objectbrains.tms.service.TMSAgentService;
import com.objectbrains.tms.service.AgentStatsService;
import com.objectbrains.tms.websocket.WebsocketService;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author farzadaziminia
 */
@Path("/chat")
@Produces(MediaType.APPLICATION_JSON)
public class ChatRest {
    
    @Autowired
    AgentStatsService agentCallService;
    
    @ConfigContext  
    private ConfigurationUtility config;
    
    @Autowired
    TMSAgentService agentService;
    
    @Autowired
    WebsocketService ws;
    
    private static final Logger log = LoggerFactory.getLogger(TMSPhoneCommand.class);
    
    @Path("/start/{loanPk}")
    @GET
    public AgentChat startChat(@PathParam("loanPk") String loanPk) {
        log.info("start chat for : " + loanPk);
        Map<Integer, AgentStats> statsMaps = agentCallService.getAllAgentStats();
        log.info("agent count: " + statsMaps.size());
        
        AgentChat ac = new AgentChat();
        
        String allowedAgentForChat = config.getString("allowed.agent.for.chat", "8967.4510");

        for (Map.Entry<Integer, AgentStats> entry : statsMaps.entrySet()) {
            if (allowedAgentForChat.contains(agentService.getAgent(entry.getKey()).getExtension()+"")) {
                log.info("Found");
                log.info(agentService.getAgent(entry.getKey()).getExtension().toString());
                log.info(entry.getKey().toString());
                if (entry.getValue().isIdle()) {
                    log.info(agentService.getAgent(entry.getKey()).getUserName());
                    ac.setUsername(agentService.getAgent(entry.getKey()).getUserName());
                    ac.setFirstname(agentService.getAgent(entry.getKey()).getFirstName());
                    ac.setLastname(agentService.getAgent(entry.getKey()).getLastName());
                    ac.setExt(agentService.getAgent(entry.getKey()).getExtension());
                    ac.setUuid(UUID.randomUUID().toString());
                }
            }
        }
        
        return ac;
    }
    
    @Path("/send/{ext}")
    @POST
    public Boolean postChat(@PathParam("ext") int ext, @RequestBody PostChatBody postChatBody) {
        log.info("message for chat for : " + ext);
        log.info("message to  chat for : " + postChatBody.getFromName());
        Chat chat = postChatBody.borrowerToagentChatBuilder();
        ws.sendChatMessage(ext, chat);
        return true;
    }
}
