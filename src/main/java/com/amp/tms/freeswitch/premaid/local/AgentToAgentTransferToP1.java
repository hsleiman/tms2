/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.local;

import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.refrence.DDD;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.Bridge;
import com.amp.tms.freeswitch.dialplan.action.Export;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.hazelcast.entity.AgentCall;
import com.amp.tms.pojo.BorrowerInfo;
import java.util.Map;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
public class AgentToAgentTransferToP1 extends DialplanBuilder {

    public AgentToAgentTransferToP1(DialplanVariable variable) {
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
        callEnteringSBC(callerAgent, call);
    }

    @Override
    public void saveDialplans() {

    }

    private void callEnteringAgent(AgentTMS callerAgent, AgentCall call) {

        LocalDateTime dateTime = LocalDateTime.now();
        TMSDialplan agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, inVariables.getContext(), "AgentToAgentTransferToP1");
        commonVariable(agentDialplan, callerAgent, call);

        if (call != null) {
            agentCallService.callTransfered(callerAgent.getExtension(), call.getCallUUID());
        }

        agentDialplan.setRecord(Boolean.FALSE);
        agentDialplan.addAction(new Set(FreeswitchVariables.ringback, "${us-ring}"));

        String dist = inVariables.getCalleeIdNumber();
        if (dist.length() == "211711".length() && dist.startsWith("21")) {
            dist = dist.substring(2);
        }

        agentDialplan.addBridge(new Bridge("sofia/agent/sip:" + dist + "@" + freeswitchService.getFreeswitchIPNew(agentDialplan.getCall_uuid(), FreeswitchContext.sbc_dp) + ":" + FreeswitchContext.sbc_dp.getPort() + ";transport=tcp"));

        //agentDialplan.addBridge(new BridgeToSofiaContact(inVariables.getCalleeIdInteger(), calleeAgent.getFreeswitchDomain()));
        agentDialplan.setActivateAgentOnCall(Boolean.TRUE);

        agentDialplan.setXMLFromDialplan();
        setReturnDialplan(agentDialplan);
        dialplanService.updateTMSDialplan(agentDialplan);

    }

    public void commonVariable(TMSDialplan tMSDialplan, AgentTMS callerAgent, AgentCall call) {

        tMSDialplan.setAutoAswer(Boolean.FALSE);
        tMSDialplan.setOutboundVendor(Boolean.FALSE);
        tMSDialplan.setDnc(Boolean.FALSE);
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

    private void callEnteringSBC(AgentTMS callerAgent, AgentCall call) {
        TMSDialplan sbcDialplan;
        sbcDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.sbc_dp, "AgentToAgentTransferToP1");
        commonVariable(sbcDialplan, callerAgent, call);
        sbcDialplan.setRecord(Boolean.TRUE);
        sbcDialplan.addAction(new Export("nolocal:absolute_codec_string=PCMU"));
        sbcDialplan.addAction(new TMSOrder(DDD.P2_TO_P1_CONNECT_TO_AGENT));
        sbcDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        sbcDialplan.addAction(new Set("gateway", "p2-p1-gateway"));
        sbcDialplan.addBridge(new Bridge("sofia/gateway/${gateway}/$1"));
        log.info("Saving " + sbcDialplan.getTms_type() + " Dialplan...");
        sbcDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(sbcDialplan);
    }

}
