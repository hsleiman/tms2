/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull;

import com.objectbrains.tms.db.repository.CdrRepository;
import com.objectbrains.tms.enumerated.DialerActiveStatus;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.param.DialerActiveStatusParam;
import com.objectbrains.tms.pojo.AgentStatus;
import com.objectbrains.tms.pojo.CallHistory;
import com.objectbrains.tms.service.AgentCallService;
import com.objectbrains.tms.service.AgentService;
import com.objectbrains.tms.service.AgentStatsService;
import com.objectbrains.tms.service.AmsService;
import com.objectbrains.tms.service.DialplanService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.FreeswitchService;
import com.objectbrains.tms.service.dialer.DialerService;
import com.objectbrains.tms.service.freeswitch.PhoneOperationService;
import com.objectbrains.tms.websocket.Websocket;
import com.objectbrains.tms.websocket.message.BiMessage;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author connorpetty
 */
@Path("/agent")
@Produces(MediaType.APPLICATION_JSON)
public class AgentRest {
    
    private static final Logger LOG = LoggerFactory.getLogger(AgentRest.class);
    
    @Autowired
    private AmsService amsService;
    
    @Autowired
    private AgentStatsService agentStatsService;
    
    @Autowired
    private AgentService agentService;
    
    @Autowired
    private AgentCallService agentCallService;
    
    @Autowired
    private DialerService dialerService;
    
    @Autowired
    private CdrRepository cdrRepository;
    
    @Autowired
    @Lazy
    private Websocket websocket;
    
    @Autowired
    private FreeswitchConfiguration configuration;
    
    @Autowired
    private DialplanService dialplanRepository;
    
    @Autowired
    private PhoneOperationService phoneOperationService;
    
    @Autowired
    private FreeswitchService freeswitchService;

//    @GET
//    public List<AgentDirectory> getAgentDirectory() {
//        List<User> users = amsService.getAllUsers();
//        Map<String, User> userMap = new HashMap<>();
//        for (User user : users) {
//            userMap.put(user.getUserName(), user);
//        }
//        Collection<Agent> agents = agentService.getAgents(userMap.keySet());
//        Map<Integer, AgentStats> statsMap = agentStatsService.getAgentStats(agents);
//
//        List<AgentDirectory> agentDirectorys = new ArrayList<>();
//        for (Agent agent : agents) {
//            String userName = agent.getUserName();
//            int extension = agent.getExtension();
//            agentDirectorys.add(new AgentDirectory(agent, userMap.get(userName), statsMap.get(extension)));
//        }
//        return agentDirectorys;
//    }
    @GET
    @Path("/{ext}")
    public AgentStatus getAgentStatus(@PathParam("ext") int ext) {
        return agentService.getAgentStatus(ext);
    }
    
    @POST
    @Path("/{ext}/dialer-active/{active}")
    public AgentStatus setAgentStatus(@PathParam("ext") int ext, @PathParam("active") DialerActiveStatusParam active) {
        agentStatsService.setDialerActive(ext, active.getValue() == DialerActiveStatus.ACTIVE);
        return agentService.getAgentStatus(ext);
    }

//    @POST
//    @Path("/{ext}/ready/{ready}")
//    public AgentStatus setReadyStatus(@PathParam("ext") int ext, @PathParam("ready") boolean ready) {
//        dialerService.agentReady(ext);
//        return agentService.setReadyStatus(ext, ready);
//    }
    @Path("/{ext}/recent-calls")
    @GET
    public List<CallHistory> getAgentCallHistory(@PathParam("ext") int ext,
            @QueryParam("page") int page, @QueryParam("size") int size) {
        return cdrRepository.getAgentCallHistory(ext, page, size);
    }
    
    @Path("/{ext}/registered")
    @GET
    public Boolean getIsRegistered(@PathParam("ext") int ext) {
        return freeswitchService.isRegisteredOnFreeswitch(ext);
    }
    
    @Path("/{ext}/uploadSnapshot")
    @POST
    public void uploadSnapshot(@PathParam("ext") int ext, @RequestBody BiMessage message) {
        AgentCall activeCall = agentCallService.getActiveCall(ext);
        String callUUID = activeCall != null ? activeCall.getCallUUID() : null;
        websocket.handleBiMessage(ext, message, callUUID);
    }

//    @Path("/{callerExt}/three-way/{calleeExt}")
//    @POST
//    public SpyOnCallPojo threeWayCall(@PathParam("calleeExt") int calleeExt, @PathParam("callerExt") int callerExt) throws IOException {
//        HzAgent agent = agentService.getAgent(callerExt);
//
//        TMSDialplan agentDailplan = dialplanRepository.createTMSDialplan(UUID.randomUUID().toString(), FreeswitchContext.agent_dp);
//        agentDailplan.setTms_type("agent");
//        agentDailplan.setCall_uuid(agent.getCall_uuid());
//        agentDailplan.setAutoAswer(false);
//        agentDailplan.setDebugOn(Boolean.TRUE);
//
//        Condition agentCondition = new Condition();
//        LOG.info("Threeway on: " + callerExt + " --> " + calleeExt);
//        agentCondition.addAction(Set.create(FreeswitchVariables.is_tms_dp, Boolean.TRUE));
//        agentCondition.addAction(Set.create(FreeswitchVariables.tms_uuid, agentDailplan.getKey().getTms_uuid()));
//        agentCondition.addAction(Set.create(FreeswitchVariables.tms_order, agentDailplan.getKey().getOrderPower()));
//        agentCondition.addAction(Set.create(FreeswitchVariables.is_auto_answer, Boolean.FALSE));
//        agentCondition.addAction(Set.create(FreeswitchVariables.call_direction, agent.getCallDirection()));
//        agentCondition.addAction(Set.create(FreeswitchVariables.is_dialer, Boolean.FALSE));
//        agentCondition.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, callerExt));
//        agentCondition.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, callerExt));
//        agentCondition.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, callerExt));
//
//        if (agent.getCurrentLoanId() != null) {
//            agentCondition.addAction(Set.create(FreeswitchVariables.loan_id, agent.getCurrentLoanId()));
//            agentDailplan.setLoanId(agent.getCurrentLoanId());
//        }
//        if (agent.getCurrentBorrowerFirstName() != null) {
//            agentCondition.addAction(Set.create(FreeswitchVariables.borrower_first_name, agent.getCurrentBorrowerFirstName()));
//            agentDailplan.setBorrowerFirstName(agent.getCurrentBorrowerFirstName());
//        }
//        if (agent.getCurrentBorrowerLastName() != null) {
//            agentCondition.addAction(Set.create(FreeswitchVariables.borrower_last_name, agent.getCurrentBorrowerLastName()));
//            agentDailplan.setBorrowerFirstName(agent.getCurrentBorrowerFirstName());
//        }
//
//        agentCondition.addAction(new Bridge("${sofia_contact(agent/" + calleeExt + "@" + configuration.getFreeswitchHostName() + ")}"));
//
//        String variable = FreeswitchVariables.tms_uuid + "=" + agentDailplan.getKey().getTms_uuid();
//        variable = variable + "," + FreeswitchVariables.is_tms_dp + "=" + Boolean.TRUE;
//        variable = variable + "," + FreeswitchVariables.effective_caller_id_number + "=" + callerExt;
//        variable = variable + "," + FreeswitchVariables.origination_caller_id_number + "=" + callerExt;
//        variable = variable + "," + FreeswitchVariables.origination_caller_id_name + "=" + callerExt;
//
//        if (agent.getCurrentLoanId() != null) {
//            if (agent.getCurrentLoanId().equalsIgnoreCase("") == false) {
//                variable = variable + FreeswitchVariables.loan_id + "=" + agent.getCurrentLoanId();
//                if (agent.getCurrentBorrowerFirstName() != null) {
//                    variable = variable + "," + FreeswitchVariables.borrower_first_name + "=" + agent.getCurrentBorrowerFirstName();
//                }
//                if (agent.getCurrentBorrowerLastName() != null) {
//                    variable = variable + "," + FreeswitchVariables.borrower_last_name + "=" + agent.getCurrentBorrowerLastName();
//                }
//            }
//        }
//        if (variable.equals("") == false) {
//            variable = "{" + variable + "}";
//        }
//
//        String arg = variable + "sofia/agent/sip:" + calleeExt + "@" + configuration.getFreeswitchIP() + ":5044 'queue_dtmf:w3@500,eavesdrop:" + agent.getCurrentAgentFreeswitchUUID() + "' inline";
//        agentDailplan.setOriginate(arg);
//
//        Dialplan agentDP = new Dialplan("AgentDP", agentDailplan.getKey().getContext(), agentCondition);
//        agentDailplan.setXml(agentDP.getXML());
//        dialplanRepository.updateTMSDialplan(agentDailplan);
//
//        LOG.info("Freeswitch Orginate threeWayCall: " + arg);
//        callingOutService.PlaceCallAsyc(arg);
//
//        SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
//        spyOnCallPojo.setStatus("success");
//        spyOnCallPojo.setCalleeExt(calleeExt);
//        spyOnCallPojo.setUuid(agent.getCurrentAgentFreeswitchUUID());
//        spyOnCallPojo.setArg(arg);
//        return spyOnCallPojo;
//
//    }
//
//    @Path("/{callerExt}/eavesdrop/{calleeExt}")
//    @POST
//    public SpyOnCallPojo eavsdropOnCall(@PathParam("calleeExt") int calleeExt, @PathParam("callerExt") int callerExt) throws IOException {
//        HzAgent agent = agentService.getAgent(callerExt);
//
//        TMSDialplan agentDailplan = dialplanRepository.createTMSDialplan(UUID.randomUUID().toString(), FreeswitchContext.agent_dp);
//        agentDailplan.setTms_type("agent");
//        agentDailplan.setCall_uuid(agent.getCall_uuid());
//        agentDailplan.setAutoAswer(true);
//        agentDailplan.setDebugOn(Boolean.TRUE);
//
//        Condition agentCondition = new Condition();
//        LOG.info("eavsdropOnCall on: " + callerExt + " --> " + calleeExt);
//        agentCondition.addAction(Set.create(FreeswitchVariables.is_tms_dp, Boolean.TRUE));
//        agentCondition.addAction(Set.create(FreeswitchVariables.tms_uuid, agentDailplan.getKey().getTms_uuid()));
//        agentCondition.addAction(Set.create(FreeswitchVariables.tms_order, agentDailplan.getKey().getOrderPower()));
//        agentCondition.addAction(Set.create(FreeswitchVariables.is_auto_answer, Boolean.TRUE));
//        agentCondition.addAction(Set.create(FreeswitchVariables.call_direction, agent.getCallDirection()));
//        agentCondition.addAction(Set.create(FreeswitchVariables.is_dialer, Boolean.FALSE));
//        agentCondition.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, callerExt));
//        agentCondition.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, callerExt));
//        agentCondition.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, callerExt));
//
//        if (agent.getCurrentLoanId() != null) {
//            try {
//                agentCondition.addAction(Set.create(FreeswitchVariables.loan_id, agent.getCurrentLoanId()));
//                agentDailplan.setLoanId(Long.parseLong(agent.getCurrentLoanId()));
//            } catch (Exception ex) {
//
//            }
//        }
//        if (agent.getCurrentBorrowerFirstName() != null) {
//            agentCondition.addAction(Set.create(FreeswitchVariables.borrower_first_name, agent.getCurrentBorrowerFirstName()));
//            agentDailplan.setBorrowerFirstName(agent.getCurrentBorrowerFirstName());
//        }
//        if (agent.getCurrentBorrowerLastName() != null) {
//            agentCondition.addAction(Set.create(FreeswitchVariables.borrower_last_name, agent.getCurrentBorrowerLastName()));
//            agentDailplan.setBorrowerFirstName(agent.getCurrentBorrowerFirstName());
//        }
//
//        agentCondition.addAction(new Bridge("${sofia_contact(agent/" + calleeExt + "@" + configuration.getFreeswitchHostName() + ")}"));
//
//        String variable = FreeswitchVariables.tms_uuid + "=" + agentDailplan.getKey().getTms_uuid();
//        variable = variable + "," + FreeswitchVariables.is_tms_dp + "=" + Boolean.TRUE;
//        variable = variable + "," + FreeswitchVariables.effective_caller_id_number + "=" + callerExt;
//        variable = variable + "," + FreeswitchVariables.origination_caller_id_number + "=" + callerExt;
//        variable = variable + "," + FreeswitchVariables.origination_caller_id_name + "=" + callerExt;
//
//        if (agent.getCurrentLoanId() != null) {
//            if (agent.getCurrentLoanId().equalsIgnoreCase("") == false) {
//                variable = variable + FreeswitchVariables.loan_id + "=" + agent.getCurrentLoanId();
//                if (agent.getCurrentBorrowerFirstName() != null) {
//                    variable = variable + "," + FreeswitchVariables.borrower_first_name + "=" + agent.getCurrentBorrowerFirstName();
//                }
//                if (agent.getCurrentBorrowerLastName() != null) {
//                    variable = variable + "," + FreeswitchVariables.borrower_last_name + "=" + agent.getCurrentBorrowerLastName();
//                }
//            }
//        }
//        if (variable.equals("") == false) {
//            variable = "{" + variable + "}";
//        }
//
//        String arg = variable + "sofia/agent/sip:" + calleeExt + "@" + configuration.getFreeswitchIP() + ":5044 &eavesdrop(" + agent.getCurrentAgentFreeswitchUUID() + ")";
//        agentDailplan.setOriginate(arg);
//
//        Dialplan agentDP = new Dialplan("AgentDP", agentDailplan.getKey().getContext(), agentCondition);
//        agentDailplan.setXml(agentDP.getXML());
//        dialplanRepository.updateTMSDialplan(agentDailplan);
//        LOG.info("Freeswitch Orginate eavsdrop: " + arg);
//        callingOutService.PlaceCallAsyc(arg);
////        
////        
////        String arg = "sofia/agent/sip:" + calleeExt + "@" + configuration.getFreeswitchIP() + ":5044" + "  &eavesdrop(" + agent.getCurrentAgentFreeswitchUUID() + ")";
////        HttpClient.sendPostRequestAsText("http://127.0.0.1:7070/tms_local/freeswitch/sendAsyncApiCommand/originate", arg);
//        SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
//        spyOnCallPojo.setStatus("success");
//        spyOnCallPojo.setCalleeExt(calleeExt);
//        spyOnCallPojo.setUuid(agent.getCurrentAgentFreeswitchUUID());
//        spyOnCallPojo.setArg(arg);
//        return spyOnCallPojo;
//    }
//
//    @Path("/{callerExt}/spy/{calleeExt}/start")
//    @POST
//    public boolean startScreenEavesdrop(@PathParam("calleeExt") int calleeExt, @PathParam("callerExt") int callerExt) throws IOException {
//        HzAgent agent = agentService.getAgent(calleeExt);
//        websocket.addListener(calleeExt, callerExt);
//        if (agent != null) {
//            agent.setScreenMonitored(Boolean.TRUE);
//            agentService.saveAgent(agent);
//        }
//        return true;
//    }
//
//    @Path("/{callerExt}/spy/{calleeExt}/stop")
//    @POST
//    public boolean stopScreenEavesdrop(@PathParam("calleeExt") int calleeExt, @PathParam("callerExt") int callerExt) throws IOException {
//        HzAgent agent = agentService.getAgent(calleeExt);
//        websocket.removeListener(callerExt);
//        if (agent != null) {
//            agent.setScreenMonitored(Boolean.FALSE);
//            agentService.saveAgent(agent);
//        }
//        return false;
//    }
//
//    @Path("/{callerExt}/whisper/{calleeExt}")
//    @POST
//    public SpyOnCallPojo whisperOnCall(@PathParam("callerExt") int callerExt, @PathParam("calleeExt") int calleeExt) throws IOException {
//
//        HzAgent agent = agentService.getAgent(callerExt);
//        TMSDialplan agentDailplan = dialplanRepository.createTMSDialplan(UUID.randomUUID().toString(), FreeswitchContext.agent_dp);
//        agentDailplan.setTms_type("agent");
//        agentDailplan.setCall_uuid(agent.getCall_uuid());
//        agentDailplan.setAutoAswer(false);
//        agentDailplan.setDebugOn(Boolean.TRUE);
//
//        Condition agentCondition = new Condition();
//        LOG.info("whisperOnCall on: " + callerExt + " --> " + calleeExt);
//        agentCondition.addAction(Set.create(FreeswitchVariables.is_tms_dp, Boolean.TRUE));
//        agentCondition.addAction(Set.create(FreeswitchVariables.tms_uuid, agentDailplan.getKey().getTms_uuid()));
//        agentCondition.addAction(Set.create(FreeswitchVariables.tms_order, agentDailplan.getKey().getOrderPower()));
//        agentCondition.addAction(Set.create(FreeswitchVariables.is_auto_answer, Boolean.FALSE));
//        agentCondition.addAction(Set.create(FreeswitchVariables.call_direction, agent.getCallDirection()));
//        agentCondition.addAction(Set.create(FreeswitchVariables.is_dialer, Boolean.FALSE));
//        agentCondition.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, callerExt));
//        agentCondition.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, callerExt));
//        agentCondition.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, callerExt));
//
//        if (agent.getCurrentLoanId() != null) {
//            try {
//                agentCondition.addAction(Set.create(FreeswitchVariables.loan_id, agent.getCurrentLoanId()));
//                agentDailplan.setLoanId(Long.parseLong(agent.getCurrentLoanId()));
//            } catch (Exception ex) {
//
//            }
//        }
//        if (agent.getCurrentBorrowerFirstName() != null) {
//            agentCondition.addAction(Set.create(FreeswitchVariables.borrower_first_name, agent.getCurrentBorrowerFirstName()));
//            agentDailplan.setBorrowerFirstName(agent.getCurrentBorrowerFirstName());
//        }
//        if (agent.getCurrentBorrowerLastName() != null) {
//            agentCondition.addAction(Set.create(FreeswitchVariables.borrower_last_name, agent.getCurrentBorrowerLastName()));
//            agentDailplan.setBorrowerLastName(agent.getCurrentBorrowerLastName());
//        }
//
//        agentCondition.addAction(new Bridge("${sofia_contact(agent/" + calleeExt + "@" + configuration.getFreeswitchHostName() + ")}"));
//
//        String variable = FreeswitchVariables.tms_uuid + "=" + agentDailplan.getKey().getTms_uuid();
//        variable = variable + "," + FreeswitchVariables.is_tms_dp + "=" + Boolean.TRUE;
//        variable = variable + "," + FreeswitchVariables.effective_caller_id_number + "=" + callerExt;
//        variable = variable + "," + FreeswitchVariables.origination_caller_id_number + "=" + callerExt;
//        variable = variable + "," + FreeswitchVariables.origination_caller_id_name + "=" + callerExt;
//
//        if (agent.getCurrentLoanId() != null) {
//            if (agent.getCurrentLoanId().equalsIgnoreCase("") == false) {
//                variable = variable + FreeswitchVariables.loan_id + "=" + agent.getCurrentLoanId();
//                if (agent.getCurrentBorrowerFirstName() != null) {
//                    variable = variable + "," + FreeswitchVariables.borrower_first_name + "=" + agent.getCurrentBorrowerFirstName();
//                }
//                if (agent.getCurrentBorrowerLastName() != null) {
//                    variable = variable + "," + FreeswitchVariables.borrower_last_name + "=" + agent.getCurrentBorrowerLastName();
//                }
//            }
//        }
//        if (variable.equals("") == false) {
//            variable = "{" + variable + "}";
//        }
//
//        String arg;
//        if (agent.getCallDirection() == CallDirection.OUTBOUND) {
//            arg = variable + "sofia/agent/sip:" + calleeExt + "@" + configuration.getFreeswitchIP() + ":5044 'queue_dtmf:w2@500,eavesdrop:" + agent.getCurrentAgentFreeswitchUUID() + "' inline";
//        } else {
//            arg = variable + "sofia/agent/sip:" + calleeExt + "@" + configuration.getFreeswitchIP() + ":5044 'queue_dtmf:w1@500,eavesdrop:" + agent.getCurrentAgentFreeswitchUUID() + "' inline";
//        }
//        agentDailplan.setOriginate(arg);
//
//        Dialplan agentDP = new Dialplan("AgentDP", agentDailplan.getKey().getContext(), agentCondition);
//        agentDailplan.setXml(agentDP.getXML());
//        dialplanRepository.updateTMSDialplan(agentDailplan);
//        LOG.info("Freeswitch Orginate whisperOnCall: " + arg);
//        callingOutService.PlaceCallAsyc(arg);
//
//        SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
//        spyOnCallPojo.setStatus("success");
//        spyOnCallPojo.setCalleeExt(calleeExt);
//        spyOnCallPojo.setUuid(agent.getCurrentAgentFreeswitchUUID());
//
//        return spyOnCallPojo;
//    }
//    @Path("/get-active-status/{ext}")
//    @GET
//    public AgentStatus getActiveStatus(@PathParam("ext") int ext) {
//        return agentService.getAgentStatus(ext);
//    }
//
//    @Path("/set-active-status/{ext}/{active}")
//    @GET
//    public AgentStatus setActiveStatus(@PathParam("ext") int ext, @PathParam("active") DialerActiveStatusParam active) {
//        return agentService.setDialerActive(ext, active.getValue());
//    }
//
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
}
