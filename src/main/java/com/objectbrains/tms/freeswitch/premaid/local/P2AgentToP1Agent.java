/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.local;

import com.objectbrains.svc.iws.PopupDisplayMode;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.refrence.DDD;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.Bridge;
import com.objectbrains.tms.freeswitch.dialplan.action.Export;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.dialplan.action.TMSOrder;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.pojo.BorrowerInfo;
import com.objectbrains.tms.websocket.message.Function;
import com.objectbrains.tms.websocket.message.outbound.CallSipHeader;
import com.objectbrains.tms.websocket.message.outbound.Send;

/**
 *
 * @author hsleiman
 */
public class P2AgentToP1Agent extends DialplanBuilder {

    private Agent agent = null;
    
    public P2AgentToP1Agent(DialplanVariable variable) {
        super();
        log.info("P2AgentToP1Agent");
        setVariable(variable);
        this.setDebugOn(Boolean.TRUE);
    }

    @Override
    public void createDialplans() {
    }

    @Override
    public void buildDialplans() {
        callEnteringSBC();
        agentDialplan();
       
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
        sbcDialplan.addAction(new Export("nolocal:absolute_codec_string=PCMU"));
        sbcDialplan.addAction(new TMSOrder(DDD.P2_TO_P1_CONNECT_TO_AGENT));
        sbcDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        sbcDialplan.addAction(new Set("gateway", "p2-p1-gateway"));
        sbcDialplan.addBridge(new Bridge("sofia/gateway/${gateway}/$1"));
        log.info("Saving " + sbcDialplan.getTms_type() + " Dialplan...");
        sbcDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(sbcDialplan);
    }

    private void agentDialplan() {
        TMSDialplan agentDialplan;
        agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.agent_dp);
        commonVariable(agentDialplan);

        agentDialplan.setRecord(Boolean.FALSE);
        agentDialplan.setAutoAswer(Boolean.FALSE);
        agentDialplan.setOutboundVendor(Boolean.FALSE);
        agentDialplan.setDnc(Boolean.FALSE);
        agentDialplan.addAction(new Set(FreeswitchVariables.ringback, "${us-ring}"));
        String dist = inVariables.getCalleeIdNumber();
        if(dist.length() == "211711".length() && dist.startsWith("21")){
            dist = dist.substring(2);
        }
        
        agentDialplan.addBridge(new Bridge("sofia/agent/sip:"+dist+"@" + freeswitchService.getFreeswitchIPNew(agentDialplan.getCall_uuid(), FreeswitchContext.sbc_dp) + ":" + FreeswitchContext.sbc_dp.getPort() + ";transport=tcp"));

//        if (agent != null) {
//            agenService.setAgent(agent.getExtension(), agent);
//        }
        log.info("Saving " + agentDialplan.getTms_type() + " Dialplan...");
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

}
