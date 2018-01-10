/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull.freeswitch;

import com.amp.tms.db.entity.freeswitch.CDR;
import com.amp.tms.service.freeswitch.FsAgentService;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.pojo.DirectoryVariable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * 
 */
@Path("/agent")
@Produces(MediaType.TEXT_XML)
public class Agent {

    private static final Logger log = LoggerFactory.getLogger(Agent.class);

    @Autowired
    private FsAgentService agentService;

    public Agent(){
        log.info("created instance of Agent");
    }
    
    @Path("/directory")
    @POST
    public String DirectoryLookup(@BeanParam DirectoryVariable directoryVariable) {
        return agentService.DirectoryLookup(directoryVariable);
    }

    @Path("/dialplan")
    @POST
    public String DialLookup(@BeanParam DialplanVariable dialplanVariable) {
        return agentService.DialLookup(dialplanVariable);
    }

    @Path("/cdr")
    @POST
    public void CDRDump(CDR cdr,@Context HttpServletRequest request){
        log.info("CDRDump lookup: " + cdr.getContext());
        agentService.CDRDump(request, cdr);
    }
}
