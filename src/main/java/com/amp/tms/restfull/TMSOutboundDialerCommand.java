/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull;

import com.google.gson.Gson;
import com.objectbrains.ams.iws.UserNotFoundException;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.crm.constants.DialerMode;
import com.amp.crm.constants.PopupDisplayMode;
import com.amp.crm.embeddable.OutboundDialerQueueRecord;
import com.amp.crm.pojo.AccountCustomerName;
import com.amp.crm.pojo.CustomerPhoneData;
import com.amp.crm.pojo.DialerQueueAccountDetails;
import com.amp.crm.pojo.TMSCallDetails;
import com.amp.crm.service.dialer.DialerQueueService;
import com.amp.crm.service.dialer.OutboundDialerService;
import com.amp.crm.service.tms.TMSService;
import com.amp.tms.enumerated.DialerActiveStatus;
import com.amp.tms.freeswitch.pojo.DialerInfoPojo;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.freeswitch.premaid.outbound.CallOutWithAMD;
import com.amp.tms.hazelcast.Configs;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.hazelcast.entity.AgentCall;
import com.amp.tms.hazelcast.entity.AgentStats;
import com.amp.tms.hazelcast.entity.DialerStats;
import com.amp.tms.param.DialerActiveStatusParam;
import com.amp.tms.pojo.AgentStatus;
import com.amp.tms.pojo.DialerStatsWithAgents;
import com.amp.tms.pojo.LoanInfoRecord;
import com.amp.tms.pojo.QueueRunningSatus;
import com.amp.tms.service.AgentCallService;
import com.amp.tms.service.AgentQueueAssociationService;
import com.amp.tms.service.TMSAgentService;
import com.amp.tms.service.AgentStatsService;
import com.amp.tms.service.dialer.Dialer;
import com.amp.tms.service.dialer.DialerService;
import com.amp.tms.service.dialer.predict.AgentQueueWeightedPriority;
import com.amp.tms.utility.GsonUtility;
import com.amp.tms.websocket.Websocket;
import com.amp.tms.websocket.WebsocketService;
import com.amp.tms.websocket.message.Function;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import com.amp.tms.websocket.message.outbound.PreviewDialerSend;
import com.amp.tms.websocket.message.outbound.Send;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 *
 * 
 */
@Path("/tms-commands/dialer-outbound-control")
@Produces(MediaType.APPLICATION_JSON)
public class TMSOutboundDialerCommand {

    private static final Logger LOG = LoggerFactory.getLogger(TMSOutboundDialerCommand.class);

    @Autowired
    private TMSAgentService agentService;

    @Autowired
    private AgentStatsService statsService;

    @Autowired
    private DialerService dialerService;

    @Autowired
    private TMSService tmsIWS;
    
    @Autowired
    private OutboundDialerService outboundDialerService;
    
    @Autowired
    private DialerQueueService dialerQueueService;

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private AgentCallService agentCallService;
    
    @Autowired
    private AgentQueueAssociationService associationService;

    @Autowired
    @Lazy
    private Websocket websocket;

    @Autowired
    @Lazy
    private WebsocketService websocketService;
    
    @Path("/get-active-status/{ext}")
    @GET
    public AgentStatus getActiveStatus(@PathParam("ext") int ext) {
        return agentService.getAgentStatus(ext);
    }

    @Path("/set-active-status/{ext}/{active}")
    @GET
    public AgentStatus setActiveStatus(@PathParam("ext") int ext, @PathParam("active") DialerActiveStatusParam active) {
        statsService.setDialerActive(ext, active.getValue() == DialerActiveStatus.ACTIVE);
        websocketService.refreshDelay(ext);
        return agentService.getAgentStatus(ext);
    }

//    @Path("/set-ready-status/{ext}/{ready}")
//    @GET
//    public AgentStatus setReadyStatus(@PathParam("ext") int ext, @PathParam("ready") boolean ready) {
//        try {
//            dialerService.agentReady(ext);
//        } catch (Exception ex) {
//            log.error(ex.getMessage(), ex);
//        }
//        return agentService.setReadyStatus(ext, ready);
//    }
    @Path("/get-dialer-queue-status/{queueId}")
    @GET
    public DialerStats getDialerQueueStatus(@PathParam("queueId") long queueId) {
        Dialer dialer = dialerService.getDialer(queueId);
        if (dialer != null) {
            return dialer.getDialerStats();
        }
        return new com.amp.tms.hazelcast.entity.DialerStats();
    }

    @Path("/get-all-dialer-queue-status")
    @GET
    public List<QueueRunningSatus> getAllDialerQueueStatus() {
        List<QueueRunningSatus> list = new ArrayList<>();

        List<Dialer> dialers = dialerService.getDialers();
        for (Dialer dialer : dialers) {
            QueueRunningSatus qrs = new QueueRunningSatus();
            qrs.setQueue(dialer.getRecord().getDqPk());
//            try {
                qrs.setRunning(dialer.isRunning());
//            } catch (DialerException ex) {
//                qrs.setRunning(true);
//                LOG.error("Error occurred in dialer {}", ex.getQueuePk(), ex);
//            }
            list.add(qrs);
        }
        return list;
    }

    @Path("/get-all-agents-status-in-queue/{queueId}")
    @GET
    public List<AgentStatus> getAllAgentStatusInQueue(@PathParam("queueId") int queueId) throws Exception {
        List<AgentStatus> retList = new ArrayList<>();
        List<AgentTMS> agents = agentService.getAgents(dialerQueueService.getAgentWeightPriorityListForDq(queueId), null, null);
        Map<Integer, AgentStats> stats = statsService.getAgentStats(agents);
        Map<Integer, AgentCall> callMap = agentCallService.getActiveCalls(agents);
        for (AgentTMS agent : agents) {
            int extension = agent.getExtension();
            retList.add(new AgentStatus(agent, stats.get(extension), callMap.get(extension)));
        }
        return retList;
    }

    @Path("/get-all-running-dialer-status")
    @GET
    public List<DialerStatsWithAgents> getAllDialerStats() {
        long startTime = System.currentTimeMillis();
        Set<AgentQueueWeightedPriority> agentQueueWeightedPriorities = associationService.getAllParticipatingAgents(false, null, false);
        long getAllParticipatingAgentsTime = System.currentTimeMillis();
//        LOG.info("getAllParticipatingAgents: {}", getAllParticipatingAgentsTime-startTime);
        Set<Integer> extensions = new HashSet<>();

        Map<Long, List<Integer>> queueToAgentsMap = new HashMap<>();

        for (AgentQueueWeightedPriority agentQueueWeightedPriority : agentQueueWeightedPriorities) {
            int ext = agentQueueWeightedPriority.getExtension();
            long queuePk = agentQueueWeightedPriority.getQueuePk();
            extensions.add(ext);
            List<Integer> agents = queueToAgentsMap.get(queuePk);
            if (agents == null) {
                agents = new ArrayList<>();
                queueToAgentsMap.put(queuePk, agents);
            }
            agents.add(ext);
        }
        Map<Integer, AgentTMS> agentMap = hazelcastService.getMap(Configs.AGENT_MAP).getAll(extensions);
        Map<Integer, AgentStats> agentStatsMap = statsService.getAgentStats(extensions);
        Map<Integer, AgentCall> agentCallMap = agentCallService.getActiveCalls(extensions);

        List<DialerStatsWithAgents> statsList = new ArrayList<>();

        for (Map.Entry<Long, List<Integer>> entrySet : queueToAgentsMap.entrySet()) {
            long queuePk = entrySet.getKey();
            Dialer dialer = dialerService.getDialer(queuePk);
            if (dialer == null) {
                continue;
            }
            DialerStatsWithAgents stats = new DialerStatsWithAgents(queuePk, dialer.getDialerStats());
            List<Integer> value = entrySet.getValue();
            for (int ext : value) {
                stats.getAgentStatuses().add(new AgentStatus(agentMap.get(ext),
                        agentStatsMap.get(ext),
                        agentCallMap.get(ext)));
            }
            statsList.add(stats);
        }
//        LOG.info("dialerStats: {}", System.currentTimeMillis()-getAllParticipatingAgentsTime);
        return statsList;
//        return dialerService.getAllDialerStatuses();
    }

//    @RequestMapping(value = "/get-all-agents-status-in-queue", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.APPLICATION_JSON)
//    public List<AgentStatus> getAllAgentStatusInQueue() {
//        Set<Agent> agents = new HashSet<>();
//        List<Dialer> dialers = dialerService.getDialers();
//        for (Dialer dialer : dialers) {
//            if (dialer.isRunning()) {
//                agents.addAll(agentRepository.getAgents(dialer.getRecord().getAgentWeightPriorityList(), null, null));
//            }
//        }
//        List<AgentStatus> retList = new ArrayList<>();
//        for (AgentTMS agent : agents) {
//            retList.add(new AgentStatus(agent));
//        }
//        return retList;
//    }
    @Path("/get-loan-list-for-queue/{queueId}")
    @GET
    public List<LoanInfoRecord> getLoanListForQueue(@PathParam("queueId") long queuePk) throws Exception, UserNotFoundException {
        List<LoanInfoRecord> retList = new ArrayList<>();
        Dialer dialer = dialerService.getDialer(queuePk);
        OutboundDialerQueueRecord record;
        if (dialer != null) {
            record = dialer.getRecord();
        } else {
            record = outboundDialerService.getOutboundDialerQueueRecord(queuePk);
        }
        for (DialerQueueAccountDetails details : record.getAccountDetails()) {
            List<CustomerPhoneData> data = details.getCustomerPhoneData();
            if (data.isEmpty()) {
                continue;
            }
            CustomerPhoneData borrower = data.get(0);
            LoanInfoRecord loanRecord = new LoanInfoRecord();
            loanRecord.setFirstName(borrower.getFirstName());
            loanRecord.setLastName(borrower.getLastName());
            loanRecord.setLoanPk(details.getAccountPk());
            loanRecord.setCompleted(dialer != null && dialer.isLoanComplete(details.getAccountPk()));
            retList.add(loanRecord);
        }
        return retList;
    }

    @Path("/get-loan-list-for-queue/{queueId}/{page}/{size}")
    @GET
    public List<LoanInfoRecord> getLoanListForQueue(@PathParam("queueId") long queuePk,
            @PathParam("page") int page, @PathParam("size") int size) throws Exception, UserNotFoundException {
        List<LoanInfoRecord> retList = new ArrayList<>();
        Dialer dialer = dialerService.getDialer(queuePk);
        if (dialer != null) {
            OutboundDialerQueueRecord record = dialer.getRecord();
            int index = 0;
            for (DialerQueueAccountDetails details : record.getAccountDetails()) {
                if (retList.size() >= size) {
                    break;
                }
                List<CustomerPhoneData> data = details.getCustomerPhoneData();
                if (data.isEmpty()) {
                    continue;
                }
                if (index < page * size) {
                    index++;
                    continue;
                }

                CustomerPhoneData borrower = data.get(0);
                LoanInfoRecord loanRecord = new LoanInfoRecord();
                loanRecord.setFirstName(borrower.getFirstName());
                loanRecord.setLastName(borrower.getLastName());
                loanRecord.setLoanPk(details.getAccountPk());
                loanRecord.setCompleted(dialer.isLoanComplete(details.getAccountPk()));
                retList.add(loanRecord);
            }
        } else {
            List<AccountCustomerName> borrowerNames = dialerQueueService.getBasicAccountDataForQueue(queuePk, page, size);
            for (AccountCustomerName borrowerName : borrowerNames) {
                LoanInfoRecord loanRecord = new LoanInfoRecord();
                loanRecord.setFirstName(borrowerName.getFirstName());
                loanRecord.setLastName(borrowerName.getLastName());
                loanRecord.setLoanPk(borrowerName.getAccountPk());
                loanRecord.setCompleted(false);
                retList.add(loanRecord);
            }
        }
        return retList;
    }

    @Path("/send-preview-dialer-test/{ext}/{type}/{popup}")
    @GET
    public void sendPreviewDialerTest(@PathParam("ext") int ext, @PathParam("type") String type,
            @Context HttpServletResponse response) throws Exception, UserNotFoundException {

        LOG.info("Sending Test Preview.");
        Gson gson = GsonUtility.getGson(true);

        PreviewDialerSend dialerSend = new PreviewDialerSend();
        dialerSend.setBorrowerFirstName("Bob");
        dialerSend.setBorrowerLastName("Smith");
        dialerSend.setDelay(10l);
        dialerSend.setLoanId(1);

        ArrayList<PhoneToType> phoneToTypes = new ArrayList<>();
        String[] phones = "7147182832,5627778888".split(",");
        String[] types = "work,home".split(",");
        for (int i = 0; i < types.length; i++) {
            String type1 = types[i];
            long phone1 = Long.parseLong(phones[i]);
            PhoneToType phoneToType = new PhoneToType();
            phoneToType.setPhoneNumber(phone1);
            phoneToType.setPhoneType(type1);
            phoneToTypes.add(phoneToType);
        }
        dialerSend.setPhone(phoneToTypes);
        dialerSend.setPopupType(PopupDisplayMode.NEW_TAB);
        dialerSend.setPreviewType(type);

        Send send = new Send(Function.PreviewDialer);
        send.setPreviewDialer(dialerSend);
        LOG.info("Sending PreviewDialer JSON: " + gson.toJson(send));
        websocket.sendWithRetry(ext, send);
    }

    @Path("/send-regular-dialer-test/{ext}/{type}/{phone}")
    @GET
    public void sendRegularDialerTest(@PathParam("ext") int ext, @PathParam("type") String type, @PathParam("phone") Long phone,
            @Context HttpServletResponse response) throws Exception, UserNotFoundException {

        LOG.info("Sending PowerDialer");
        DialerInfoPojo dialerInfoPojo = new DialerInfoPojo();

        TMSCallDetails callDetails = null;
        callDetails = tmsIWS.getLoanInfoByPhoneNumber(phone);
        if (callDetails != null) {
            Dialer d = dialerService.getDialer(callDetails.getDialerQueuePk());
            dialerInfoPojo.setSettings(d.getRecord().getDialerQueueSettings());
            dialerInfoPojo.setAgentExt(ext);
            dialerInfoPojo.setBorrowerFirstName(callDetails.getFirstName());
            dialerInfoPojo.setBorrowerLastName(callDetails.getLastName());
            dialerInfoPojo.setDialerMode(DialerMode.REGULAR);
            dialerInfoPojo.setLoanId(callDetails.getAccountPk());
            PhoneToType phoneToType = new PhoneToType();
            phoneToType.setPhoneNumber(phone);
            phoneToType.setPhoneType("Home");
            ArrayList<PhoneToType> phoneToTypes = new ArrayList<>();
            phoneToTypes.add(phoneToType);
            dialerInfoPojo.setPhoneToType(phoneToTypes);
//            DialplanBuilder builder = new PowerDialer(dialerInfoPojo);
//            builder.execute();

        }
    }

    @Path("/send-progressive-dialer-step1-test/{ext}/{type}/{phone}")
    @GET
    public void sendProgressiveDialerStep1Test(@PathParam("ext") int ext, @PathParam("type") long qPK, @PathParam("phone") Long phone,
            @Context HttpServletResponse response) throws Exception, UserNotFoundException {

        LOG.info("Sending progressive 1");
        DialerInfoPojo dialerInfoPojo = new DialerInfoPojo();

        TMSCallDetails callDetails = null;
        callDetails = tmsIWS.getLoanInfoByPhoneNumber(phone);
        if (callDetails != null) {
            Dialer d = dialerService.getDialer(qPK);
            dialerInfoPojo.setSettings(d.getRecord().getDialerQueueSettings());
            dialerInfoPojo.setAgentExt(ext);
            dialerInfoPojo.setBorrowerFirstName(callDetails.getFirstName());
            dialerInfoPojo.setBorrowerLastName(callDetails.getLastName());
            dialerInfoPojo.setDialerMode(DialerMode.PROGRESSIVE);
            dialerInfoPojo.setLoanId(callDetails.getAccountPk());
            PhoneToType phoneToType = new PhoneToType();
            phoneToType.setPhoneNumber(phone);
            phoneToType.setPhoneType("Home");
            ArrayList<PhoneToType> phoneToTypes = new ArrayList<>();
            phoneToTypes.add(phoneToType);
            dialerInfoPojo.setPhoneToType(phoneToTypes);
            DialplanBuilder builder = new CallOutWithAMD(dialerInfoPojo);
            builder.execute();
        }
    }

    @Path("/send-progressive-dialer-step2-test/{ext}/{type}/{phone}")
    @GET
    public void sendProgressiveDialerStep2Test(@PathParam("ext") int ext, @PathParam("type") String type, @PathParam("phone") Long phone,
            @Context HttpServletResponse response) throws Exception, UserNotFoundException {

        LOG.info("Sending progressive 2");
        DialerInfoPojo dialerInfoPojo = new DialerInfoPojo();

        TMSCallDetails callDetails = null;
        callDetails = tmsIWS.getLoanInfoByPhoneNumber(phone);
        if (callDetails != null) {
            Dialer d = dialerService.getDialer(callDetails.getDialerQueuePk());
            dialerInfoPojo.setSettings(d.getRecord().getDialerQueueSettings());
            dialerInfoPojo.setAgentExt(ext);
            dialerInfoPojo.setBorrowerFirstName(callDetails.getFirstName());
            dialerInfoPojo.setBorrowerLastName(callDetails.getLastName());
            dialerInfoPojo.setDialerMode(DialerMode.PROGRESSIVE);
            dialerInfoPojo.setLoanId(callDetails.getAccountPk());
            PhoneToType phoneToType = new PhoneToType();
            phoneToType.setPhoneNumber(phone);
            phoneToType.setPhoneType("Home");
            ArrayList<PhoneToType> phoneToTypes = new ArrayList<>();
            phoneToTypes.add(phoneToType);
            dialerInfoPojo.setPhoneToType(phoneToTypes);
            //DialplanBuilder builder = new PridictiveDialerStepTwo(dialerInfoPojo);
            //builder.execute();
        }
    }

}
