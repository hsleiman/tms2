/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restful.ows;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 *
 * 
 */
@Path("/freeswitch")
public interface TmsLocal {

    @Path("/sendAsyncApiCommand/originate")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void asyncOriginate(String arg);

}
