/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull;

import com.amp.tms.websocket.WebsocketService;
import com.amp.tms.restfull.pojo.AgentsCount;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author farzadaziminia
 */

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class Admin {
    private static final Logger log = LoggerFactory.getLogger(TMSPhoneCommand.class);
    
    @Autowired
    @Lazy
    private WebsocketService websocketService;
    
    @Path("/changeFreeswitchIp/{ip}")
    @GET
    public AgentsCount changeFreeswitchIp(@PathParam("ip") String ip) {
        log.info("Change Ip address to: " + ip);
       int count = websocketService.changeFreeswitchIp(ip);
       AgentsCount ac = new AgentsCount();
       ac.setCount(count);
       return ac;
    }
    
    @Path("/resetFreeswitchIp")
    @GET
    public AgentsCount resetFreeswitchIp() {
       log.info("Reset freeswitch address to");
       int count = websocketService.resetFreeswitchIp();
       AgentsCount ac = new AgentsCount();
       ac.setCount(count);
       return ac;
    }
    
    @Path("/restartExtension/{extension}")
    @GET
    public AgentsCount restartExtension(@PathParam("extension") int extension) {
        log.info("Restart extension: " + extension);
       int count =  websocketService.restartExtension(extension);
       AgentsCount ac = new AgentsCount();
       ac.setCount(count);
       return ac;
    }
    
}
