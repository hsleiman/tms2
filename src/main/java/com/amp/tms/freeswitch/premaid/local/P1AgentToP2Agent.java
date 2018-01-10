/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.local;

import com.amp.crm.constants.PopupDisplayMode;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.refrence.DDD;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.Bridge;
import com.amp.tms.freeswitch.dialplan.action.BridgeToSofiaContact;
import com.amp.tms.freeswitch.dialplan.action.Export;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.hazelcast.entity.AgentTMS;

/**
 *
 * 
 */
public class P1AgentToP2Agent extends DialplanBuilder {

    private AgentTMS agent = null;
    
    public P1AgentToP2Agent(DialplanVariable variable, AgentTMS agent) {
        super();
        log.info("P1AgentToP2Agent");
        setVariable(variable);
        this.agent = agent;
        this.setDebugOn(Boolean.TRUE);
    }

    @Override
    public void createDialplans() {
    }

    @Override
    public void buildDialplans() {
        agentDialplan();
        callEnteringSBC();
    }

    @Override
    public void saveDialplans() {

    }

    public void commonVariable(TMSDialplan tMSDialplan) {
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setCallDirection(CallDirection.INTERNAL);
        tMSDialplan.setAutoAswer(Boolean.FALSE);
        tMSDialplan.setPopupType(PopupDisplayMode.SAME_WINDOW);

        tMSDialplan.setCaller(inVariables.getCallerIdNumber());
        tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
        tMSDialplan.setOutboundVendor(Boolean.FALSE);
        
        tMSDialplan.setChannelCallUUID(inVariables.getChannelCallUUID());

        tMSDialplan.setVariables(inVariables.toJson());
        tMSDialplan.setIgnore_disposition(Boolean.TRUE);


        tMSDialplan.setDialer(Boolean.FALSE);
    }

    private void callEnteringSBC() {
        TMSDialplan sbcDialplan;
        sbcDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.sbc_dp);
        commonVariable(sbcDialplan);
        sbcDialplan.setRecord(Boolean.TRUE);
        sbcDialplan.addAction(new TMSOrder(DDD.P1_TO_P2_CONNECT_TO_AGENT));
        sbcDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        sbcDialplan.addBridge(new Bridge("sofia/agent/sip:" + agent.getExtension() + "@" + agent.getFreeswitchIP() + ":5044;transport=tcp"));

        log.info("Saving " + sbcDialplan.getTms_type() + " Dialplan...");
        sbcDialplan.setXMLFromDialplan();
        setReturnDialplan(sbcDialplan);
        dialplanService.updateTMSDialplan(sbcDialplan);
    }

    private void agentDialplan() {
        AgentTMS callerAgent = agenService.getAgent(inVariables.getCallerIdInteger());
        AgentTMS calleeAgent = agenService.getAgent(agent.getExtension());
        
        TMSDialplan agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.agent_dp, DDD.P1_TO_P2_CONNECT_TO_AGENT);
        commonVariable(agentDialplan);
        log.info("From: " + inVariables.getCallerIdNumber() + " --> " + inVariables.getCalleeIdNumber());

        agentDialplan.addAction(new Set("dialed_extension", "$1"));
        agentDialplan.addAction(new Export("dialed_extension", "$1"));

        agentDialplan.addAction(new Set(FreeswitchVariables.ringback, "${us-ring}"));
        agentDialplan.addAction(new Set("transfer_ringback", "$${hold_music}"));
        agentDialplan.addAction(new Set(FreeswitchVariables.call_timeout, "20"));
        //actions.add(new Set("sip_exclude_contact","${network_addr}"));
        agentDialplan.addAction(new Set("hangup_after_bridge", "true"));

        agentDialplan.addAction(new Set("continue_on_fail", "false"));

        if (callerAgent != null) {
            agentDialplan.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, callerAgent.getUserName()));
            agentDialplan.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, callerAgent.getExtension()));
            agentDialplan.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, callerAgent.getExtension()));
        }

        agentDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        agentDialplan.addAction(new BridgeToSofiaContact("$1", calleeAgent.getFreeswitchDomain()));
        
        agentDialplan.setXMLFromDialplan();
        //setReturnDialplan(agentDialplan);
        dialplanService.updateTMSDialplan(agentDialplan);

    }

}
