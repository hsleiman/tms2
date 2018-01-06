/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull.freeswitch;

/**
 *
 * @author hsleiman
 */
import com.amp.tms.db.entity.freeswitch.CDR;
import com.amp.tms.service.freeswitch.SBCService;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
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
@Path("/sbc")
@Produces(MediaType.TEXT_XML)
public class SBC {

    private static final Logger log = LoggerFactory.getLogger(SBC.class);

    @Autowired
    private SBCService sBCService;

    @Path("/dialplan")
    @POST
    public String DialLookup(@BeanParam DialplanVariable dialplanVariable) {
        return sBCService.DialLookup(dialplanVariable);

    }

    @Path("/cdr")
    @POST
    public void CDRDump(CDR cdr,@Context HttpServletRequest request) {
          sBCService.CDRDump(request, cdr);
    }

}
