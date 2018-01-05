/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.local;

import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.RecordedPhrases;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.Answer;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToAgent;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToSofiaContact;
import com.objectbrains.tms.freeswitch.dialplan.action.Export;
import com.objectbrains.tms.freeswitch.dialplan.action.Hangup;
import com.objectbrains.tms.freeswitch.dialplan.action.Playback;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.dialplan.action.Sleep;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;
import com.objectbrains.tms.hazelcast.entity.AgentTMS;
import com.objectbrains.tms.pojo.BorrowerInfo;
import com.objectbrains.tms.websocket.message.Function;
import com.objectbrains.tms.websocket.message.outbound.CallSipHeader;
import com.objectbrains.tms.websocket.message.outbound.Send;

/**
 *
 * @author hsleiman
 */
public class AgentToAgent extends DialplanBuilder {

    public AgentToAgent(DialplanVariable variable) {
        super();
        log.info("AgentToAgent");
        setVariable(variable);
    }

    @Override
    public void createDialplans() {
    }

    @Override
    public void buildDialplans() {
        AgentTMS callerAgent = agenService.getAgent(inVariables.getCallerIdInteger());
        AgentTMS calleeAgent = agenService.getAgent(inVariables.getCalleeIdInteger());

        TMSDialplan agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, inVariables.getContext());

        agentDialplan.setCallDirection(CallDirection.INTERNAL);

        log.info("From: " + inVariables.getCallerIdNumber() + " --> " + inVariables.getCalleeIdNumber());

        if (configuration.enableRecodingOnAgentToAgentDialplan()) {
            agentDialplan.setRecord(Boolean.TRUE);
        }

        agentDialplan.addAction(new Set("dialed_extension", "$1"));
        agentDialplan.addAction(new Export("dialed_extension", "$1"));

        agentDialplan.addAction(new Set(FreeswitchVariables.ringback, "${us-ring}"));
        agentDialplan.addAction(new Set("transfer_ringback", "$${hold_music}"));
        agentDialplan.addAction(new Set(FreeswitchVariables.call_timeout, "20"));
        agentDialplan.addAction(new Set("hangup_after_bridge", "true"));

        agentDialplan.addAction(new Set("continue_on_fail", "true"));

        if (callerAgent != null) {
            agentDialplan.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, callerAgent.getUserName()));
            agentDialplan.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, callerAgent.getExtension()));
            agentDialplan.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, callerAgent.getExtension()));
        }

        agentDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));

        if (inVariables.getFreeSWITCH_IPv4().equals(calleeAgent.getFreeswitchIP())) {
            agentDialplan.addAction(new BridgeToSofiaContact("$1", calleeAgent.getFreeswitchDomain()));
        } else {
            agentDialplan.addAction(new BridgeToAgent(calleeAgent.getFreeswitchIP(), calleeAgent.getExtension()));
        }

        agentDialplan.addAction(new Answer());
        agentDialplan.addAction(new Sleep(1000l));
        agentDialplan.addAction(new Playback(RecordedPhrases.AGENT_NOT_AVAILABLE));
        agentDialplan.addAction(new Sleep(1000l));
        agentDialplan.addAction(new Playback(RecordedPhrases.GOODEBYE));
        agentDialplan.addBridge(new Hangup("NORMAL_CLEARING"));

        agentDialplan.setTms_type(this.getClass().getSimpleName());
        agentDialplan.setCall_uuid(agentDialplan.getKey().getTms_uuid());
        agentDialplan.setChannelCallUUID(inVariables.getChannelCallUUID());
        agentDialplan.setCallee(inVariables.getCalleeIdNumber());
        agentDialplan.setCaller(inVariables.getCallerIdNumber());
        agentDialplan.setVariables(inVariables.toJson());
        agentDialplan.setIgnore_disposition(Boolean.TRUE);
        agentDialplan.setXMLFromDialplan();
        setReturnDialplan(agentDialplan);
        dialplanService.updateTMSDialplan(agentDialplan);

        Send send = new Send(Function.CallUUID);
        CallSipHeader callSipHeader = new CallSipHeader();
        callSipHeader.setBorrowerInfo(new BorrowerInfo());
        callSipHeader.setCallDirection(CallDirection.INTERNAL);
        callSipHeader.setIgnore_disposition(Boolean.TRUE);
        callSipHeader.setCall_uuid(agentDialplan.getCall_uuid());
        send.setCallSipHeader(callSipHeader);
        websocket.sendWithRetry(inVariables.getCallerIdInteger(), send);

    }

    @Override
    public void saveDialplans() {

    }

}
