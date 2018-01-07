/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull;

import com.amp.crm.embeddable.BIPlaybackData;
import com.amp.tms.service.BiStoreService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Path("/calls")
@Produces(MediaType.APPLICATION_JSON)
public class CallRest {
    
    @Autowired
    private BiStoreService biStoreService;

    @GET
    @Path("/get-playback-data/{callUUID}")
    public BIPlaybackData getBiPlaybackData(@PathParam("callUUID") String callUUID) {
        return biStoreService.getBiPlaybackData(callUUID);
    }

}
