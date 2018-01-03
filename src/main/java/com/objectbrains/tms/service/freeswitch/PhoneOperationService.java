/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.freeswitch;

import com.objectbrains.ams.iws.User;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.refrence.BeanServices;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToSofiaContact;
import com.objectbrains.tms.freeswitch.originate.OriginateBuilder;
import com.objectbrains.tms.freeswitch.pojo.FreeswitchCommand;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.hazelcast.entity.AgentStats;
import com.objectbrains.tms.pojo.AgentDirectory;
import com.objectbrains.tms.pojo.BorrowerInfo;
import com.objectbrains.tms.pojo.SpyOnCallPojo;
import com.objectbrains.tms.service.AgentCallService;
import com.objectbrains.tms.service.AgentService;
import com.objectbrains.tms.service.AgentStatsService;
import com.objectbrains.tms.service.AmsService;
import com.objectbrains.tms.service.DialplanService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.InboundCallService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service
public class PhoneOperationService {

    private static final Logger log = LoggerFactory.getLogger(PhoneOperationService.class);

    @Autowired
    private AgentService agentService;

    @Autowired
    private CallingOutService callingOutService;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private DialplanService dialplanRepository;

    @Autowired
    private AgentStatsService agentStatsService;

    @Autowired
    private AgentCallService agentCallService;
    
    @Autowired
    private InboundCallService inboundCallService;
    
    @Autowired
    private AmsService amsService;
    
    public void appendWavFileForPrompt( String Command, String freeswitchIP){
        callingOutService.PlaceShellCommand(Command, freeswitchIP);
    }
    
    @Async
    public void playPrompts(ArrayList<FreeswitchCommand> list){
        for (int i = 0; i < list.size(); i++) {
            FreeswitchCommand get = list.get(i);
            log.info("[PLAY PROMPT] Calling to place command {} for prompt with arg {} and then I will sleep for {}", get.getCommand(), get.getArg(), get.getSleeptime());
            callingOutService.PlaceFreeswitchCommandSync(get.getCommand(), get.getArg(), get.getFreeswitchIP());
            if(get.getSleeptime() > 0){
                try {
                    Thread.sleep(get.getSleeptime());
                } catch (InterruptedException ex) {
                    log.error("Exception {}", ex);
                }
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException ex) {
                
            }
        }
        
    }

    public SpyOnCallPojo threeWayCall(int calleeExt, int onCallExt){
        Agent agent = agentService.getAgent(onCallExt);
        AgentCall call = agentCallService.getActiveCall(onCallExt);
        return threeWayCall(agent, call, calleeExt, onCallExt);
    }

    public SpyOnCallPojo threeWayCall(String call_uuid, int calleeExt, int onCallExt){
        Agent agent = agentService.getAgent(onCallExt);
        AgentCall call = agentCallService.getAgentCall(onCallExt, call_uuid);
        return threeWayCall(agent, call, calleeExt, onCallExt);
    }

    private SpyOnCallPojo threeWayCall(Agent agent, AgentCall call, int calleeExt, int onCallExt) {
        if (onCallExt == calleeExt) {
            SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
            spyOnCallPojo.setStatus("You cant confrence on your self.");
            spyOnCallPojo.setCalleeExt(calleeExt);
            return spyOnCallPojo;
        }

        Agent calleeAgent = agentService.getAgent(calleeExt);
        AgentCall calleeCall = agentCallService.getActiveCall(calleeAgent.getExtension());
        if (calleeCall != null) {
            SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
            spyOnCallPojo.setStatus("You cant confrence at this time.");
            spyOnCallPojo.setCalleeExt(calleeExt);
            return spyOnCallPojo;
        }

        TMSDialplan old = dialplanRepository.findTMSDialplan(call.getCallUUID(), FreeswitchContext.agent_dp, null);
        if (old != null) {
            log.info("OLD " + old.getKey().toString());
        }

        TMSDialplan agentDailplan = dialplanRepository.createTMSDialplan(UUID.randomUUID().toString(), FreeswitchContext.agent_dp, "three-way-call");
        agentDailplan.setTms_type("three-way-call");
        setCommonVariable(agentDailplan, onCallExt, calleeExt, agent, call, old);

        OriginateBuilder builder = new OriginateBuilder();
        setCommandOriginateVariable(builder, agentDailplan, onCallExt, calleeExt, agent, call, old);

        builder.appendALeg("sofia/agent/sip:" + calleeExt + "@" + agentService.getFreeswitchIPForExt(calleeExt) + ":" + FreeswitchContext.agent_dp.getPort());
        builder.appendBLeg("'queue_dtmf:w3@500,eavesdrop:" + call.getAgentFreeswitchUUID() + "' inline");
        agentDailplan.setOriginate(builder.build());

        log.info("Threeway: " + agentDailplan.toJson());
        agentDailplan.setXMLFromDialplan();
        dialplanRepository.updateTMSDialplan(agentDailplan);

        log.info("Freeswitch Orginate threeWayCall: " + agentDailplan.getOriginate());
        callingOutService.PlaceOriginateToFreedwitchAsyc(agentDailplan.getOriginate(), agentService.getFreeswitchIPForExt(onCallExt));

        SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
        spyOnCallPojo.setStatus("success");
        spyOnCallPojo.setCalleeExt(calleeExt);
        spyOnCallPojo.setUuid(call.getAgentFreeswitchUUID());
        spyOnCallPojo.setArg(agentDailplan.getOriginate());
        return spyOnCallPojo;
    }

    public SpyOnCallPojo eavsdropOnCall(int calleeExt, int onCallExt) throws IOException {
        Agent agent = agentService.getAgent(onCallExt);
        AgentCall call = agentCallService.getActiveCall(onCallExt);
        return eavsdropOnCall(agent, call, calleeExt, onCallExt);
    }

    public SpyOnCallPojo eavsdropOnCall(String call_uuid, int calleeExt, int onCallExt){
        Agent agent = agentService.getAgent(onCallExt);
        AgentCall call = agentCallService.getAgentCall(onCallExt, call_uuid);
        return eavsdropOnCall(agent, call, calleeExt, onCallExt);
    }

    private SpyOnCallPojo eavsdropOnCall(Agent agent, AgentCall call, int calleeExt, int onCallExt){
        if (onCallExt == calleeExt) {
            SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
            spyOnCallPojo.setStatus("You cant eavsdrop on your self.");
            spyOnCallPojo.setCalleeExt(calleeExt);
            return spyOnCallPojo;
        }

        Agent calleeAgent = agentService.getAgent(calleeExt);
        AgentCall calleeCall = agentCallService.getActiveCall(calleeAgent.getExtension());
        if (calleeCall != null) {
            SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
            spyOnCallPojo.setStatus("You cant eavsdrop at this time.");
            spyOnCallPojo.setCalleeExt(calleeExt);
            return spyOnCallPojo;
        }
        
        log.info("Eavsdrop On call {} -> {} - {} - {}", onCallExt, calleeExt, call.getCallUUID(), call.getAgentFreeswitchUUID());

        TMSDialplan old = dialplanRepository.findTMSDialplan(call.getCallUUID(), FreeswitchContext.agent_dp, null);

        TMSDialplan agentDailplan = dialplanRepository.createTMSDialplan(UUID.randomUUID().toString(), FreeswitchContext.agent_dp, "eavsdrop-on-call");
        agentDailplan.setTms_type("eavsdrop-on-call");
        setCommonVariable(agentDailplan, onCallExt, calleeExt, agent, call, old);

        OriginateBuilder builder = new OriginateBuilder();
        setCommandOriginateVariable(builder, agentDailplan, onCallExt, calleeExt, agent, call, old);
        agentDailplan.setIgnore_disposition(Boolean.TRUE);
        builder.appendALeg("sofia/agent/sip:" + calleeExt + "@" + agentService.getFreeswitchIPForExt(calleeExt) + ":" + FreeswitchContext.agent_dp.getPort());
        builder.appendBLeg("&eavesdrop(" + call.getAgentFreeswitchUUID() + ")");

        agentDailplan.setOriginate(builder.build());

        agentDailplan.setXMLFromDialplan();
        dialplanRepository.updateTMSDialplan(agentDailplan);
        log.info("Freeswitch Orginate eavsdrop: " + agentDailplan.getOriginate());
        callingOutService.PlaceOriginateToFreedwitchAsyc(agentDailplan.getOriginate(), agentService.getFreeswitchIPForExt(onCallExt));

        
        SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
        spyOnCallPojo.setStatus("success");
        spyOnCallPojo.setCalleeExt(calleeExt);
        spyOnCallPojo.setUuid(call.getAgentFreeswitchUUID());
        spyOnCallPojo.setArg(agentDailplan.getOriginate());
        return spyOnCallPojo;
    }

    public SpyOnCallPojo whisperOnCall(int calleeExt, int onCallExt) throws IOException {
        Agent agent = agentService.getAgent(onCallExt);
        AgentCall call = agentCallService.getActiveCall(onCallExt);
        return whisperOnCall(agent, call, calleeExt, onCallExt);
    }

    public SpyOnCallPojo whisperOnCall(String call_uuid, int calleeExt, int onCallExt){
        Agent agent = agentService.getAgent(onCallExt);
        AgentCall call = agentCallService.getAgentCall(onCallExt, call_uuid);
        return whisperOnCall(agent, call, calleeExt, onCallExt);
    }

    private SpyOnCallPojo whisperOnCall(Agent agent, AgentCall call, int calleeExt, int onCallExt) {
        if (onCallExt == calleeExt) {
            SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
            spyOnCallPojo.setStatus("You cant whisper on your self.");
            spyOnCallPojo.setCalleeExt(calleeExt);
            return spyOnCallPojo;
        }

        Agent calleeAgent = agentService.getAgent(calleeExt);
        AgentCall calleeCall = agentCallService.getActiveCall(calleeAgent.getExtension());
        if (calleeCall != null) {
            SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
            spyOnCallPojo.setStatus("You cant whisper at this time.");
            spyOnCallPojo.setCalleeExt(calleeExt);
            return spyOnCallPojo;
        }

        TMSDialplan old = dialplanRepository.findTMSDialplan(call.getCallUUID(), FreeswitchContext.agent_dp, null);

        TMSDialplan agentDailplan = dialplanRepository.createTMSDialplan(UUID.randomUUID().toString(), FreeswitchContext.agent_dp, "whisper-on-call");
        agentDailplan.setTms_type("whisper-on-call");
        setCommonVariable(agentDailplan, onCallExt, calleeExt, agent, call, old);
        agentDailplan.setIgnore_disposition(Boolean.TRUE);

        OriginateBuilder builder = new OriginateBuilder();
        setCommandOriginateVariable(builder, agentDailplan, onCallExt, calleeExt, agent, call, old);

        builder.appendALeg("sofia/agent/sip:" + calleeExt + "@" + agentService.getFreeswitchIPForExt(calleeExt) + ":" + FreeswitchContext.agent_dp.getPort());
        if (call.getCallDirection() == CallDirection.OUTBOUND) {
            builder.appendBLeg("'queue_dtmf:w2@500,eavesdrop:" + call.getAgentFreeswitchUUID() + "' inline");
        } else {
            builder.appendBLeg("'queue_dtmf:w1@500,eavesdrop:" + call.getAgentFreeswitchUUID() + "' inline");
        }
        agentDailplan.setOriginate(builder.build());

        agentDailplan.setXMLFromDialplan();
        dialplanRepository.updateTMSDialplan(agentDailplan);
        log.info("Freeswitch Orginate whisperOnCall: " + agentDailplan.getOriginate());
        callingOutService.PlaceOriginateToFreedwitchAsyc(agentDailplan.getOriginate(), agentService.getFreeswitchIPForExt(onCallExt));

        SpyOnCallPojo spyOnCallPojo = new SpyOnCallPojo();
        spyOnCallPojo.setStatus("success");
        spyOnCallPojo.setCalleeExt(calleeExt);
        spyOnCallPojo.setUuid(call.getAgentFreeswitchUUID());

        return spyOnCallPojo;
    }

    private void setCommandOriginateVariable(OriginateBuilder builder, TMSDialplan agentDailplan, int callerExt, int calleeExt, Agent agent, AgentCall call, TMSDialplan old) {
        builder.putInBothLegs(FreeswitchVariables.tms_uuid, agentDailplan.getKey().getTms_uuid());
        builder.putInBothLegs(FreeswitchVariables.tms_order, agentDailplan.getKey().getOrderPower());
        builder.putInBothLegs(FreeswitchVariables.call_uuid, call.getCallUUID());
        builder.putInBothLegs(FreeswitchVariables.is_tms_dp, Boolean.TRUE);
        builder.putInBothLegs(FreeswitchVariables.effective_caller_id_number, callerExt);
        builder.putInBothLegs(FreeswitchVariables.origination_caller_id_number, callerExt);
        builder.putInBothLegs(FreeswitchVariables.origination_caller_id_name, callerExt);

        if (old != null) {
            builder.putInBothLegs(FreeswitchVariables.call_direction, old.getCallDirection().name());
            builder.putInBothLegs(FreeswitchVariables.is_dialer, old.getDialer());
        }
        //builder.putInBLegs(FreeswitchVariables., null);

        BorrowerInfo agentBorrowerInfo = call.getBorrowerInfo();
        if (agentBorrowerInfo != null && agentBorrowerInfo.getLoanId() != null) {
            builder.putInBothLegs(FreeswitchVariables.loan_id, agentBorrowerInfo.getLoanId());
            if (agentBorrowerInfo.getBorrowerFirstName() != null) {
                builder.putInBothLegs(FreeswitchVariables.borrower_first_name, agentBorrowerInfo.getBorrowerFirstName());

            }
            if (agentBorrowerInfo.getBorrowerLastName() != null) {
                builder.putInBothLegs(FreeswitchVariables.borrower_last_name, agentBorrowerInfo.getBorrowerLastName());
            }
            if (agentBorrowerInfo.getBorrowerPhoneNumber() != null) {
                builder.putInBothLegs(FreeswitchVariables.borrower_phone, agentBorrowerInfo.getBorrowerPhoneNumber());
            }
        }
    }

    private void setCommonVariable(TMSDialplan agentDailplan, int onCallExt, int calleeExt, Agent agent, AgentCall call, TMSDialplan old) {
        agentDailplan.setCall_uuid(call.getCallUUID());
        agentDailplan.setAutoAswer(false);
        agentDailplan.setDebugOn(Boolean.TRUE);
        agentDailplan.setBean(BeanServices.FsAgentService);
        agentDailplan.setFunctionCall("startCallForAgentCaller");
        agentDailplan.setMaxDelayBeforeAgentAnswer(30);

        log.info(agentDailplan.getTms_type() + " on: " + onCallExt + " --> " + calleeExt);
        agentDailplan.setAutoAswer(Boolean.FALSE);
        agentDailplan.setCaller(onCallExt + "");
        agentDailplan.setCallee(calleeExt + "");
        if (old != null) {
            agentDailplan.setDialer(old.getDialer());
            agentDailplan.setCallDirection(old.getCallDirection());
        } else {
            agentDailplan.setDialer(Boolean.FALSE);
            agentDailplan.setCallDirection(call.getCallDirection());
        }
        agentDailplan.setBorrowerInfo(call.getBorrowerInfo());
        //agentDailplan.addBridge(new Bridge("${sofia_contact(agent/" + calleeExt + "@" + configuration.getFreeswitchDomainForExt(calleeExt)+ ")}"));
        agentDailplan.addBridge(new BridgeToSofiaContact(calleeExt, agentService.getFreeswitchDomainForExt(calleeExt)));
    }

    public List<AgentDirectory> getAgentDirectory() {
        List<User> users = amsService.getAllUsers();
        Map<String, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getUserName(), user);
        }
        Collection<Agent> agents = agentService.getAgents(userMap.keySet());
        Map<Integer, AgentStats> statsMap = agentStatsService.getAgentStats(agents);
        Map<Integer, AgentCall> callMap = agentCallService.getActiveCalls(agents);

        List<AgentDirectory> agentDirectorys = new ArrayList<>();
        for (Agent agent : agents) {
            String userName = agent.getUserName();
            int extension = agent.getExtension();
            agentDirectorys.add(new AgentDirectory(agent, userMap.get(userName),
                    statsMap.get(extension),
                    callMap.get(extension)));
        }
        return agentDirectorys;
    }
    
}
