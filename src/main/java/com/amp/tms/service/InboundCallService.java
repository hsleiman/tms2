/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.objectbrains.scheduler.annotation.RunOnce;
import com.amp.crm.constants.CallRoutingOption;
import com.amp.crm.constants.IncomingCallAgent;
import com.amp.crm.db.entity.base.dialer.DialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.InboundDialerQueueSettings;
import com.amp.crm.embeddable.AgentCallOrder;
import com.amp.crm.embeddable.InboundDialerQueueRecord;
import com.amp.crm.exception.CrmException;
import com.amp.crm.pojo.TMSCallDetails;
import com.amp.crm.service.dialer.DialerQueueService;
import com.amp.crm.service.tms.TMSService;
import com.amp.tms.enumerated.AgentState;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.amp.tms.hazelcast.AgentCallState;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import com.amp.tms.pojo.BorrowerInfo;
import com.amp.tms.service.dialer.CallService;
import com.amp.tms.websocket.Websocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service
public class InboundCallService {

    private static final Logger LOG = LoggerFactory.getLogger(InboundCallService.class);

    @Autowired
    private TMSAgentService agentService;

    @Autowired
    private AgentStatsService statsService;

    @Autowired
    private AgentCallService agentCallService;

    @Autowired
    private TMSService tmsIws;

    @Autowired
    private DialerQueueRecordService dialerQueueRecordRepository;

    @Autowired
    private CallService callService;

    @Autowired
    private CallDetailRecordService callDetailRecordService;
    
    @Autowired
    private DialerQueueService dialerQueueService;

    @Autowired
    @Lazy
    private Websocket websocket;

    @RunOnce
    private void initialOnTMSLoad() {
        inboundCallOrder(null, 7147182832l, UUID.randomUUID().toString());
    }

    public AgentIncomingDistributionOrder inboundCallOrderDefult(Long queuePk, String callUUID) {
        return inboundCallOrder(queuePk, null, callUUID, null);
    }

    public AgentIncomingDistributionOrder inboundCallOrder(Long queuePk, long phoneNumber, String callUUID, Long loanPk) {
        try {
            return inboundCallOrder(queuePk, phoneNumber, callUUID, tmsIws.getLoanInfoByLoanPk(loanPk));
        } catch (CrmException ex) {
            java.util.logging.Logger.getLogger(InboundCallService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public AgentIncomingDistributionOrder inboundCallOrder(Long queuePk, long phoneNumber, String callUUID) {
        try {
            return inboundCallOrder(queuePk, phoneNumber, callUUID, tmsIws.getLoanInfoByPhoneNumber(phoneNumber));
        } catch (CrmException ex) {
            java.util.logging.Logger.getLogger(InboundCallService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean shouldRecieveCall(int ext, boolean inline, CallDirection direction, boolean autoDialed) {
        return shouldRecieveCall(ext, inline, direction, autoDialed, null);
    }

    public boolean shouldRecieveCall(int ext, boolean inline, CallDirection direction, boolean autoDialed, AgentWeightedPriority awp) {
        AgentState state = statsService.getAgentState(ext);
        AgentCallState callState = agentCallService.getAgentCallState(ext, direction, autoDialed);
        LOG.trace("agent: {}, state: {}, cannotReceive: {}, hasCalls: {}", ext, state, callState.cannotReceive(), callState.hasCalls());
        return shouldRecieveCall(inline, state, callState, awp, null);
    }

    public static boolean shouldRecieveCall(boolean inline, AgentState state, AgentCallState callState, AgentWeightedPriority awp, DialerQueueSettings settings) {
        Boolean leader = null;
        Boolean allowAfterHours = null;
        if (awp != null) {
            leader = awp.getLeader();
            allowAfterHours = awp.getAllowAfterHours();
        }
        LocalTime startTime = null, endTime = null;
        if (settings != null) {
            startTime = settings.getStartTime();
            endTime = settings.getEndTime();
        }
        if (callState == null) {
            callState = new AgentCallState();
        }
        return shouldRecieveCall(inline, state, callState.cannotReceive(), callState.hasCalls(), leader, allowAfterHours, startTime, endTime);
    }

    private static boolean shouldRecieveCall(boolean inline, AgentState state, boolean cannotReceiveCall, boolean onlyIfInline,
            Boolean leader, Boolean afterHoursOnly, LocalTime startTime, LocalTime endTime) {
        if (state == null || !state.isReadyState()) {
            return false;
        }
        if (leader != null && leader) {
            return false;
        }
        if (afterHoursOnly == null) {
            afterHoursOnly = false;
        }

        LocalTime now = LocalTime.now();
        boolean isDuringHours;
        if (startTime == null || endTime == null) {
            isDuringHours = true;
        } else if (startTime.isBefore(endTime)) {
            isDuringHours = now.isAfter(startTime) && now.isBefore(endTime);
        } else {
            isDuringHours = now.isAfter(startTime) || now.isBefore(endTime);
        }
        if (afterHoursOnly ^ (!isDuringHours)) {
            return false;
        }
        if (cannotReceiveCall) {
            return false;
        }
        if (inline) {
            return true;
        }
        if (onlyIfInline) {
            return false;
        }
        LOG.trace("Should Recieve Call: Inline: {}, AgetState: {}, CannotReceiveCall: {}, OnlyIfInline: {}, Leader: {}, AfterHour: {}, StartTime: {}, EndTime: {}, Result: {}", inline, state, cannotReceiveCall, onlyIfInline, leader, afterHoursOnly, startTime, endTime, state == AgentState.IDLE);
        return state == AgentState.IDLE;
    }

    private AgentIncomingDistributionOrder inboundCallOrder(Long queuePk, Long phoneNumber, String callUUID, TMSCallDetails details) {
        AgentIncomingDistributionOrder aido = new AgentIncomingDistributionOrder();

        if (details != null) {

            BorrowerInfo borrowerInfo = new BorrowerInfo();
            borrowerInfo.setBorrowerFirstName(details.getFirstName());
            borrowerInfo.setBorrowerLastName(details.getLastName());
            borrowerInfo.setLoanId(details.getAccountPk());
            borrowerInfo.setBorrowerPhoneNumber(String.valueOf(phoneNumber));
            aido.setBorrowerInfo(borrowerInfo);
            aido.setDefaultExtension(details.getDefaultExtension());

            aido.setCallDetails(details);

            if (queuePk == null) {
                queuePk = details.getDialerQueuePk();
            }
        } else {
            BorrowerInfo borrowerInfo = new BorrowerInfo();
            borrowerInfo.setBorrowerPhoneNumber(String.valueOf(phoneNumber));
            aido.setBorrowerInfo(borrowerInfo);
            if (queuePk == null) {
                queuePk = 1l;
            }
        }

        LOG.trace("[AIDO]-1 running queue: {} - {}", queuePk, callUUID);
        if (queuePk == null) {
            return aido;
        }
        InboundDialerQueueRecord record;
        try {
            LOG.trace("Getting Inbound Dialer Queue Settings for {}", queuePk);
            record = dialerQueueService.getInboundDialerQueueRecord(queuePk);
        } catch (Exception ex) {
            LOG.error("Failed to get InboundDialerQueueRecord for queue [{}]", queuePk, ex);
            return null;
        }
        LOG.trace("[AIDO]-2 running queue: {} - {}", queuePk, callUUID);
        dialerQueueRecordRepository.storeInboundDialerQueueRecord(record);

        InboundDialerQueueSettings settings = record.getDialerQueueSettings();
        CallRoutingOption callOrder = settings.getCallRoutingOption();

        if (details != null) {
            aido.setDialerQueueName(details.getQueueName());
        }
        aido.setDialerQueuePK(queuePk);
        aido.setSettings(settings);
        aido.setPopupDisplayMode(settings.getPopupDisplayMode());
        aido.setMaxDelayBeforeAgentAnswer(settings.getMaxDelayBeforeAgentAnswer());
        aido.setIsAutoAnswer(settings.isAutoAnswerEnabled());

        Map<String, AgentWeightedPriority> awpMap = Utils.convertToMap(record.getAgentWeightPriorityList());
        LOG.trace("[AIDO]-3 running queue: {} - {}", queuePk, callUUID);
        for (AgentCallOrder order : settings.getAgentCallOrder()) {
            LOG.trace("[AIDO]-4 running queue: {} - {}", queuePk, callUUID);
            if (order == null) {
                LOG.error("recieved null AgentCallOrder");
                continue;
            }
            boolean inline = order.isInline();
            aido.setMultiLine(inline + "");
            List<AgentTMS> agents;
            IncomingCallAgent ica = order.getIncomingCallAgent();
            LOG.trace("[AIDO]-5 running queue: {} - {}- {}", queuePk, callUUID, ica);
            aido.setIncomingCallOrderSelected(ica.name());
            LOG.trace("[AIDO]-6 running queue: {} - handling incoming call order {}", queuePk, ica);
            switch (ica) {
                case PRIMARY_AGENT:
                    if (details == null) {
                        LOG.trace("[AIDO]-7 running queue: {} - details is null, skipping", queuePk, ica);
                        continue;
                    }
                    String primaryAgentName = details.getPrimaryAgentUsername();
                    LOG.trace("[AIDO]-8 running queue: {} - primaryAgentName is {}", queuePk, primaryAgentName);
                    if (primaryAgentName == null) {
                        continue;
                    }
                    agents = new ArrayList<>();
                    agents.add(agentService.getAgent(primaryAgentName));
                    break;
                case SECONDARY_AGENTS:
                    if (details == null) {
                        LOG.trace("[AIDO]-7 running queue: {} - details is null, skipping", queuePk, ica);
                        continue;
                    }
                    List<String> secondaryAgentNames = details.getSecondaryAgentUsernameList();
                    LOG.trace("[AIDO]-8 running queue: {} - secondaryAgentNames: {}", queuePk, secondaryAgentNames);
                    if (secondaryAgentNames == null || secondaryAgentNames.isEmpty()) {
                        continue;
                    }
                    agents = agentService.getAgents(secondaryAgentNames);
                    break;
                case QUEUE_GROUP_AGENTS:
                    agents = agentService.getAgents(record.getAgentWeightPriorityList(), record.getWeightedPriority(), callOrder);
                    break;
                default:
                    //TODO throw error or something
                    continue;
            }

            Set<Integer> extensions = Utils.getExtensions(agents);
            LOG.trace("[AIDO]-9 running queue: {} - Agents Size: {} - Ext: {}", queuePk, agents.size(), extensions);
            final Map<Integer, AgentState> agentStates = statsService.getAgentStates(extensions);
            LOG.trace("[AIDO]-10 running queue: {} - Agents Size: {} - Agent States: {} - Ext: {}", queuePk, agents.size(), agentStates.size(), extensions);
            final Map<Integer, AgentCallState> agentCallStates = agentCallService.getAgentCallStates(extensions, CallDirection.INBOUND, false);

            Collections.sort(agents, new Comparator<AgentTMS>() {

                @Override
                public int compare(AgentTMS o1, AgentTMS o2) {
                    AgentCallState acs1 = agentCallStates.get(o1.getExtension());
                    AgentCallState acs2 = agentCallStates.get(o2.getExtension());
                    boolean hc1 = acs1 != null && acs1.hasCalls();
                    boolean hc2 = acs2 != null && acs2.hasCalls();

                    if (!hc1 && hc2) {
                        return -1;
                    }
                    if (hc1 && !hc2) {
                        return 1;
                    }
                    return 0;
                }

            });

            //maybe add agents to primary call map
            switch (ica) {
                case PRIMARY_AGENT:
                case SECONDARY_AGENTS:
                    if (inline) {
                        break;
                    }
                    for (AgentTMS agent : agents) {
                        AgentState state = agentStates.get(agent.getExtension());
                        LOG.debug("Maybe adding agent {}, state: {}, inline: {}, queuePk: {}", agent.getExtension(), state, inline, queuePk);
                        if (state != null) {
                            callService.addPrimaryCall(agent.getExtension(), queuePk, callUUID);
                        }
                    }
            }
            LOG.trace("[AIDO]-11 running queue: {} - Agents Size: {} - Agent States: {} - agentCallStates: {} - Ext: {}", queuePk, agents.size(), agentStates.size(), agentCallStates.size(), extensions);

            //remove agents that are not ready
            for (Iterator<AgentTMS> it = agents.iterator(); it.hasNext();) {
                AgentTMS agent = it.next();
                Integer ext = agent.getExtension();
                AgentState state = agentStates.get(ext);
                AgentCallState callState = agentCallStates.get(ext);
                LOG.trace("agent: {}, state: {}, cannotReceive: {}, hasCalls: {}", ext, state, callState.cannotReceive(), callState.hasCalls());
                if (!shouldRecieveCall(inline, state, callState, awpMap.get(agent.getUserName()), settings)
                        || websocket.checkAgentExt(ext) == false) {
                    it.remove();
                }
            }

            LOG.trace("[AIDO]-11.5 running queue: {} - Agents Size: {} - Agent States: {} - agentCallStates: {} - Ext: {}", queuePk, agents.size(), agentStates.size(), agentCallStates.size(), extensions);
            //now add the agents to the aido
            LOG.trace("[AIDO]-12 running queue: {} - Agents Size: {}", queuePk, agents.size());
            aido.addAgents(agents, inline, ica.name(), awpMap);
            LOG.trace("[AIDO]-13 running queue: {} - {}- {}", queuePk);
        }

        LOG.trace("AIDO: {}", aido);
        callDetailRecordService.updateADO(callUUID, aido, queuePk);
        return aido;
    }

}
