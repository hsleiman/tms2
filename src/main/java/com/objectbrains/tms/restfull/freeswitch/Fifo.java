/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull.freeswitch;

/**
 *
 * @author hsleiman
 */
import com.objectbrains.tms.db.entity.freeswitch.CDR;
import com.objectbrains.tms.exception.DialplanNotFoundException;
import com.objectbrains.tms.service.freeswitch.FifoService;
import com.objectbrains.tms.freeswitch.pojo.ConfigurationVariable;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
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
 * @author hsleiman
 */
@Path("/fifo")
@Produces(MediaType.TEXT_XML)
public class Fifo {

    private static final Logger log = LoggerFactory.getLogger(Fifo.class);

    @Autowired
    private FifoService fifoService;

    @Path("/config")
    @POST
    public String Configuration(@BeanParam ConfigurationVariable configurationVariable) {
        return fifoService.Configuration(configurationVariable);
    }

    @Path("/dialplan")
    @POST
    public String DialLookup(@BeanParam DialplanVariable dialplanVariable) throws DialplanNotFoundException {
        return fifoService.DialLookup(dialplanVariable);

    }

    @Path("/cdr")
    @POST
    public void CDRDump(CDR cdr, @Context HttpServletRequest request) {
        fifoService.CDRDump(request, cdr);
    }

}
