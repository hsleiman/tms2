/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull;

import com.amp.crm.embeddable.InboundDialerQueueRecord;
import com.amp.crm.pojo.TMSCallDetails;
import com.amp.crm.service.dialer.DialerQueueService;
import com.amp.crm.service.tms.TMSService;
import com.amp.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.amp.tms.service.TMSAgentService;
import com.amp.tms.service.DialerQueueRecordService;
import com.amp.tms.service.InboundCallService;
import com.amp.tms.service.dialer.DialerService;
import java.util.UUID;
import javax.ws.rs.GET;
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
@Path("/tms-commands/dialer-inbound-control")
@Produces(MediaType.APPLICATION_JSON)
public class TMSInboundDialerCommand {

    private static final Logger LOG = LoggerFactory.getLogger(TMSInboundDialerCommand.class);
    @Autowired
    private TMSService tmsIws;
    @Autowired
    private TMSAgentService agentService;
    
    @Autowired
    private DialerQueueService dialerQueueService;
    


    @Autowired
    private DialerService dialerService;

    @Autowired
    private InboundCallService inboundCallService;

    @Autowired
    private DialerQueueRecordService recordRepository;
    
//    @Path("/set-ready-status/{ext}/{ready}")
//    @GET
//    public AgentStatus setReadyStatus(@PathParam("ext") int ext, @PathParam("ready") boolean ready) {
//        try {
//            dialerService.agentReady(ext);
//        } catch (Exception ex) {
//            LOG.error(ex.getMessage(), ex);
//        }
//        return agentService.setReadyStatus(ext, ready);
//    }
    @Path("/get-incoming-distribution-order/{phone}")
    @GET
    public AgentIncomingDistributionOrder getAgentIncomingDistributionOrder(@PathParam("phone") long phone) {
        try {
            return inboundCallService.inboundCallOrder(null, phone, UUID.randomUUID().toString());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return new AgentIncomingDistributionOrder();
    }

    @Path("/get-incoming-distribution-order1/{phone}/{loanid}")
    @GET
    public AgentIncomingDistributionOrder getAgentIncomingDistributionOrder(@PathParam("phone") long phone, @PathParam("loanid") long loanid) {
        try {
            return inboundCallService.inboundCallOrder(null ,phone, UUID.randomUUID().toString(), loanid);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return new AgentIncomingDistributionOrder();
    }

    @Path("/get-loan-info-by-phone-number/{phone}")
    @GET
    public TMSCallDetails getLoanInfoByPhoneNumber(@PathParam("phone") long phone) {
        try {
            return tmsIws.getLoanInfoByPhoneNumber(phone);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return new TMSCallDetails();
    }

    @Path("/get-loan-info-by-loan-id/{loanid}")
    @GET
    public TMSCallDetails getLoanInfoByLoan(@PathParam("loanid") long loanid) {
        try {
            return tmsIws.getLoanInfoByLoanPk(loanid);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return new TMSCallDetails();
    }

//    @Path("/get-basic-loan-info/{loanid}")
//    @GET
//    public BasicLoanInformationPojo getBasicLoanInformation(@PathParam("loanid") long loanid) {
//        try {
//            return loanInformationIWS.getBasicLoanInformation(loanid, null);
//        } catch (Exception ex) {
//            LOG.error(ex.getMessage(), ex);
//        }
//        return new BasicLoanInformationPojo();
//    }

//    @Path("/get-tms-basic-loan-info/{loanid}")
//    @GET
//    public TmsBasicLoanInfo getTmsBasicLoanInfo(@PathParam("loanid") long loanid) {
//        try {
//            return tmsIWS.getBasicLoanInfoForTMS(loanid);
//        } catch (Exception ex) {
//            LOG.error(ex.getMessage(), ex);
//        }
//        return new TMSBasicAccountInfo();
//    }

    @Path("/update/{queuePk}")
    @GET
    public void updateInboundDialerQueue(@PathParam("queuePk") long queuePk) {
        try {
            InboundDialerQueueRecord record = dialerQueueService.getInboundDialerQueueRecord(queuePk);
            recordRepository.storeInboundDialerQueueRecord(record);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
    
    @Path("/getDialerQueuePkForPhoneNumber/{destinationNumber}")
    @GET
    public String getDialerQueuePkForPhoneNumber(@PathParam("destinationNumber") String destinationNumber) {
        try {
             long queuePk = dialerQueueService.getDialerQueuePkForPhoneNumber(destinationNumber);
             return "Queue: "+queuePk;
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return "";
    }
    

}
