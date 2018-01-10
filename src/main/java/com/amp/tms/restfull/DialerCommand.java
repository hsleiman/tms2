/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull;

import com.amp.tms.service.dialer.DialerException;
import com.amp.tms.service.dialer.DialerService;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * 
 */
@Path("/dialer")
@Produces(MediaType.APPLICATION_JSON)
public class DialerCommand {

    private static final Logger LOG = LoggerFactory.getLogger(DialerCommand.class);

    @Autowired
    private DialerService dialerService;

    public DialerCommand() {
        LOG.info("created instance of DialerService");
    }

    @Path("/start/{queuePk}")
    @POST
    public void startQueue(@PathParam("queuePk") Long queuePk) throws Exception, DialerException {
        dialerService.startQueue(queuePk);
    }

    @Path("/pause/{queuePk}")
    @POST
    public void pauseQueue(@PathParam("queuePk") Long queuePk) throws DialerException {
        dialerService.pauseQueue(queuePk);
    }

    @Path("/resume/{queuePk}")
    @POST
    public void resumeQueue(@PathParam("queuePk") Long queuePk) throws DialerException {
        dialerService.resumeQueue(queuePk);
    }

    @Path("/stop/{queuePk}")
    @POST
    public void stopQueue(@PathParam("queuePk") Long queuePk) throws DialerException {
        dialerService.stopQueue(queuePk);
    }

}
