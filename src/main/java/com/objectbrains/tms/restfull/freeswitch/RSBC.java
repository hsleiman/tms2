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
import com.objectbrains.tms.service.freeswitch.RSBCService;
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
@Path("/rsbc")
@Produces(MediaType.TEXT_XML)
public class RSBC {

    private static final Logger log = LoggerFactory.getLogger(RSBC.class);

    @Autowired
    private RSBCService rSBCService;

    @Path("/dialplan")
    @POST
    public String DialLookup(@BeanParam DialplanVariable dialplanVariable) {
        return rSBCService.DialLookup(dialplanVariable);
    }

    @Path("/cdr")
    @POST
    public void CDRDump(CDR cdr, @Context HttpServletRequest request) {
        rSBCService.CDRDump(request, cdr);
    }

}
