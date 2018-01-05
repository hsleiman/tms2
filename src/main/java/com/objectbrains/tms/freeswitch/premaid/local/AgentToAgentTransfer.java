/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.local;

import com.objectbrains.sti.pojo.TMSCallDetails;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.RecordedPhrases;
import com.objectbrains.tms.enumerated.refrence.BeanServices;
import com.objectbrains.tms.enumerated.refrence.HOLDOrder;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.Answer;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToAgent;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToSofiaContact;
import com.objectbrains.tms.freeswitch.dialplan.action.Fifo;
import com.objectbrains.tms.freeswitch.dialplan.action.Playback;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.dialplan.action.TMSOrder;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;
import com.objectbrains.tms.hazelcast.entity.AgentTMS;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.pojo.BorrowerInfo;
import java.util.Map;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
public class AgentToAgentTransfer extends DialplanBuilder {

    public AgentToAgentTransfer(DialplanVariable variable) {
        super();
        log.info("AgentToAgentTransfer");
        setVariable(variable);
        if (variable.getTmsUUID() != null) {
            setTMS_UUID(variable.getTmsUUID());
        }

    }

    @Override
    public void createDialplans() {

    }

    @Override
    public void buildDialplans() {
        AgentTMS callerAgent = null;
        AgentCall call = null;

        Map.Entry<Integer, AgentCall> entry = agentCallService.getTransferingCall(inVariables.getCall_uuid());
        if (entry != null) {
            callerAgent = agenService.getAgent(entry.getKey());
            call = entry.getValue();
        }
        if (callerAgent == null && inVariables.getCallerIdInteger() != null) {
            callerAgent = agenService.getAgent(inVariables.getCallerIdInteger());
        }
        if (callerAgent == null && inVariables.getRdnisInteger() != null) {
            callerAgent = agenService.getAgent(inVariables.getRdnisInteger());
        }
        if (callerAgent == null && inVariables.getCallerAniInteger() != null) {
            callerAgent = agenService.getAgent(inVariables.getCallerAniInteger());
        }
        if (callerAgent != null && call == null) {
            call = agentCallService.getTransferingCall(callerAgent.getExtension());
        }
        if (callerAgent != null && call == null) {
            call = agentCallService.getAgentCall(callerAgent.getExtension(), inVariables.getCall_uuid());
        }
        log.info("Caller Agent is " + callerAgent);
        log.info("Caller Agent is " + callerAgent);
        log.info("Caller Agent is " + callerAgent);
        log.info("Caller Agent is " + callerAgent);
        log.info("Caller Agent is " + callerAgent);
        log.info("Caller Agent is " + callerAgent);
        log.info("Caller Agent is " + callerAgent);
        log.info("Caller Agent is " + callerAgent);
        log.info("Caller Agent is " + callerAgent);
        log.info("Caller Agent is " + callerAgent);
        log.info("Caller Agent is " + callerAgent);
        log.info("Call is " + call);
        log.info("From: " + inVariables.getCallerIdNumber() + " --> " + inVariables.getCalleeIdNumber());

        callEnteringAgent(callerAgent, call);

    }

    @Override
    public void saveDialplans() {

    }

    private void callEnteringAgent(AgentTMS callerAgent, AgentCall call) {

        LocalDateTime dateTime = LocalDateTime.now();
        TMSDialplan agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, inVariables.getContext(), "AgentToAgentTransfer_" + dateTime.getMillisOfDay());
        commonVariable(agentDialplan, callerAgent, call);

        if (call != null) {
            agentCallService.callTransfered(callerAgent.getExtension(), call.getCallUUID());
        }

        agentDialplan.addAction(Set.create(FreeswitchVariables.hangup_after_bridge, true));
        agentDialplan.addAction(Set.create(FreeswitchVariables.call_timeout, 30));
        agentDialplan.setMaxDelayBeforeAgentAnswer(20);

        agentDialplan.addAction(Set.create(FreeswitchVariables.continue_on_fail, true));

        AgentTMS calleeAgent = agenService.getAgent(inVariables.getCalleeIdInteger());
        agentDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));

        if (inVariables.getFreeSWITCH_IPv4().equals(calleeAgent.getFreeswitchIP())) {
            agentDialplan.addAction(new BridgeToSofiaContact(inVariables.getCalleeIdInteger(), calleeAgent.getFreeswitchDomain()));
        } else {
            agentDialplan.addAction(new TMSOrder(callEnteringAgentSecondNode(callerAgent, call)));
            agentDialplan.addAction(new BridgeToAgent(calleeAgent.getFreeswitchIP(), calleeAgent.getExtension()));
        }

        //agentDialplan.addBridge(new BridgeToSofiaContact(inVariables.getCalleeIdInteger(), calleeAgent.getFreeswitchDomain()));
        agentDialplan.setActivateAgentOnCall(Boolean.TRUE);

        agentDialplan.setXMLFromDialplan();
        setReturnDialplan(agentDialplan);
        dialplanService.updateTMSDialplan(agentDialplan);

    }

    private String callEnteringAgentSecondNode(AgentTMS callerAgent, AgentCall call) {

        LocalDateTime dateTime = LocalDateTime.now();
        TMSDialplan agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, inVariables.getContext(), "AgentToAgentTransfer_" + dateTime.getMillisOfDay() + "_1");
        commonVariable(agentDialplan, callerAgent, call);

        if (call != null) {
            agentCallService.callTransfered(callerAgent.getExtension(), call.getCallUUID());
        }

        agentDialplan.addAction(Set.create(FreeswitchVariables.hangup_after_bridge, true));
        agentDialplan.addAction(Set.create(FreeswitchVariables.call_timeout, 20));
        agentDialplan.setMaxDelayBeforeAgentAnswer(20);

        agentDialplan.addAction(Set.create(FreeswitchVariables.continue_on_fail, true));

        AgentTMS calleeAgent = agenService.getAgent(inVariables.getCalleeIdInteger());
        agentDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));

        agentDialplan.addAction(new BridgeToSofiaContact(inVariables.getCalleeIdInteger(), calleeAgent.getFreeswitchDomain()));

        //agentDialplan.addBridge(new BridgeToSofiaContact(inVariables.getCalleeIdInteger(), calleeAgent.getFreeswitchDomain()));
        agentDialplan.setActivateAgentOnCall(Boolean.TRUE);

        agentDialplan.setXMLFromDialplan();
        //setReturnDialplan(agentDialplan);
        dialplanService.updateTMSDialplan(agentDialplan);

        return agentDialplan.getKey().getOrderPower();

    }

    private void callEnteringFifo(AgentTMS callerAgent, AgentCall call) {
        TMSDialplan fifoDialplan;
        fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD);
        commonVariable(fifoDialplan, callerAgent, call);
        fifoDialplan.addAction(new Set("fifo_music", configuration.getFiFoHoldMusic()));

        fifoDialplan.addBridge(new Answer());
        fifoDialplan.setBean(BeanServices.FifoService);
        fifoDialplan.setFunctionCall("placeCallOnHold");

        fifoDialplan.addAction(new Playback(RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT));

        Long queuePk = null;
        if (call != null && call.getQueuePk() != null) {
            queuePk = call.getQueuePk();
        } else {
            //attempt to look it up from svc
            Long loanId = fifoDialplan.getBorrowerInfo().getLoanId();
            TMSCallDetails callDetails = null;
            if (loanId != null) {
                callDetails = tmsIWS.getLoanInfoByLoanPk(loanId);
            }
            if (callDetails != null) {
                queuePk = callDetails.getDialerQueuePk();
            }
        }
        if (queuePk != null) {
            fifoDialplan.setDialerQueueId(queuePk);
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_" + queuePk + " in"));
        } else {
            fifoDialplan.setDialerQueueId(1l);
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_1" + " in"));
        }

        log.info("Saving " + fifoDialplan.getTms_type() + " Dialplan...");

        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);
    }

    public void commonVariable(TMSDialplan tMSDialplan, AgentTMS callerAgent, AgentCall call) {
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCall_uuid(inVariables.getCall_uuid());
        tMSDialplan.setAutoAswer(Boolean.TRUE);
        tMSDialplan.setCallDirection(inVariables.getCallDirection());
        if (inVariables.getTmsTransfer() == null || inVariables.getTmsTransfer() == false) {
            if (call != null) {
                tMSDialplan.setCall_uuid(call.getCallUUID());
                tMSDialplan.setCallDirection(call.getCallDirection());
                tMSDialplan.setBorrowerInfo(call.getBorrowerInfo());
                tMSDialplan.setDialerQueueId(call.getQueuePk());
            } else {
                tMSDialplan.setBorrowerInfo(new BorrowerInfo());
                log.info("Agent is not on Call " + call + " - " + inVariables.getCallerIdInteger() + " -> " + inVariables.getCalleeIdInteger());
            }
        }

        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setChannelCallUUID(inVariables.getChannelCallUUID());
        tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
        tMSDialplan.setCaller(inVariables.getCallerIdNumber());
        tMSDialplan.setVariables(inVariables.toJson());
        tMSDialplan.setDialer(inVariables.getDialer());
        tMSDialplan.setDialerQueueId(inVariables.getDialerQueuePk());

    }

}
