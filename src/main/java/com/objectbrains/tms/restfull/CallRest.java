/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull;

import com.objectbrains.svc.iws.BiPlaybackData;
import com.objectbrains.tms.service.BiStoreService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author connorpetty
 */
@Path("/calls")
@Produces(MediaType.APPLICATION_JSON)
public class CallRest {
    
    @Autowired
    private BiStoreService biStoreService;

    @GET
    @Path("/get-playback-data/{callUUID}")
    public BiPlaybackData getBiPlaybackData(@PathParam("callUUID") String callUUID) {
        return biStoreService.getBiPlaybackData(callUUID);
    }

}
