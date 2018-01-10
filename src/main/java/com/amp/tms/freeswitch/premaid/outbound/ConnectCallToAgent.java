/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.outbound;

import com.amp.tms.db.entity.cdr.CallDetailRecordTMS;
import com.amp.tms.db.entity.freeswitch.FreeswitchNode;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.refrence.BeanServices;
import com.amp.tms.enumerated.refrence.DDD;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.Answer;
import com.amp.tms.freeswitch.dialplan.action.BridgeToAgent;
import com.amp.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.amp.tms.freeswitch.dialplan.action.BridgeToSofiaContact;
import com.amp.tms.freeswitch.dialplan.action.Export;
import com.amp.tms.freeswitch.dialplan.action.Fifo;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.originate.OriginateBuilder;
import com.amp.tms.freeswitch.pojo.DialerInfoPojo;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.pojo.BorrowerInfo;

/**
 *
 * 
 */
public class ConnectCallToAgent extends DialplanBuilder {

    private DialerInfoPojo dialerInfoPojo;
    private TMSDialplan oldPassedIn = null;

    public ConnectCallToAgent(DialerInfoPojo dialerInfoPojo, TMSDialplan oldPassedIn) {
        super();
        this.dialerInfoPojo = dialerInfoPojo;
        this.oldPassedIn = oldPassedIn;
        log.info("********* Progresive Dialer AMD  Connecting Progresive");
        log.info("Progresive Dialer AMD Connecting Progresive: {} {} {} {} {}", dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber(), dialerInfoPojo.getBorrowerFirstName(), dialerInfoPojo.getLoanId(), dialerInfoPojo.getAgentExt(), dialerInfoPojo.getCallUUID());
        log.info("Progresive Dialer AMD Connecting Progresive: {} {} {} {} {}", dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber(), dialerInfoPojo.getBorrowerFirstName(), dialerInfoPojo.getLoanId(), dialerInfoPojo.getAgentExt(), dialerInfoPojo.getCallUUID());
        log.info("Progresive Dialer AMD Connecting Progresive: {} {} {} {} {}", dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber(), dialerInfoPojo.getBorrowerFirstName(), dialerInfoPojo.getLoanId(), dialerInfoPojo.getAgentExt(), dialerInfoPojo.getCallUUID());
        log.info("Progresive Dialer AMD Connecting Progresive: {} {} {} {} {}", dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber(), dialerInfoPojo.getBorrowerFirstName(), dialerInfoPojo.getLoanId(), dialerInfoPojo.getAgentExt(), dialerInfoPojo.getCallUUID());
        log.info("Progresive Dialer AMD Connecting Progresive: {} {} {} {} {}", dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber(), dialerInfoPojo.getBorrowerFirstName(), dialerInfoPojo.getLoanId(), dialerInfoPojo.getAgentExt(), dialerInfoPojo.getCallUUID());
//        try{
//            throw new Exception("DUM DUM "+dialerInfoPojo.getAgentExt()+" - "+ dialerInfoPojo.getCallUUID());
//        }catch(Exception es){
//            log.info(es.getMessage(), es);
//        }
        log.info("********* Progresive Dialer AMD Connecting Progresive");
    }

    @Override
    public void createDialplans() {
        log.info("Create Dialplans");

        TMS_UUID = dialerInfoPojo.getCallUUID();

    }

    @Override
    public void buildDialplans() {
       // TMSDialplan old = dialplanService.findTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, DDD.PLACE_CALL_IN_FIFO.name());

        callOriginatingToFifo(oldPassedIn);
        callEnteringAgent(oldPassedIn);
        callEnteringAgentOtherNode(oldPassedIn);

    }

    @Override
    public void saveDialplans() {

    }

    public void commonVariable(TMSDialplan tMSDialplan, TMSDialplan old) {
        tMSDialplan.setDebugOn(getDebugOn());
        tMSDialplan.setTms_type(this.getClass().getSimpleName());

        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setRecord(Boolean.FALSE);
        tMSDialplan.setCallDirection(CallDirection.OUTBOUND);
        tMSDialplan.setAutoAswer(dialerInfoPojo.getSettings().isAutoAnswerEnabled());
        tMSDialplan.setPopupType(dialerInfoPojo.getSettings().getPopupDisplayMode());
        tMSDialplan.setCallee(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber() + "");
        tMSDialplan.setCaller(dialerInfoPojo.getAgentExt() + "");
        tMSDialplan.setOutboundVendor(Boolean.FALSE);
        tMSDialplan.setDialer(Boolean.TRUE);
        tMSDialplan.setDialerQueueId(dialerInfoPojo.getSettings().getDialerQueuePk());

        BorrowerInfo borrowerInfo = new BorrowerInfo();
        borrowerInfo.setLoanId(dialerInfoPojo.getLoanId());
        borrowerInfo.setBorrowerFirstName(old.getBorrowerInfo().getBorrowerFirstName());
        borrowerInfo.setBorrowerLastName(old.getBorrowerInfo().getBorrowerLastName());
        borrowerInfo.setBorrowerPhoneNumber(old.getBorrowerInfo().getBorrowerPhoneNumber());
        tMSDialplan.setBorrowerInfo(borrowerInfo);
        //tMSDialplan.addAction(new Set("fifo_bridge_uuid=" + old.getUniqueID()));

    }

    public void callEnteringAgent(TMSDialplan old) {
        TMSDialplan agentDialplan;
        agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.agent_dp, DDD.CONNECT_TO_AGENT);
        commonVariable(agentDialplan, old);

        CallDetailRecordTMS callDetailRecord = callDetailRecordService.getCDR(TMS_UUID);
        callDetailRecord.setAmdExtTransferTo(dialerInfoPojo.getAgentExt());

        agentDialplan.addAction(new Export("nolocal:api_on_media=uuid_broadcast ${uuid} playback::tone_stream://%(" + configuration.getAMDPlayBeepToAgentDuration() + ",50," + configuration.getAMDPlayBeepToAgentHZ() + ") bleg"));

        agentDialplan.addAction(Set.create(FreeswitchVariables.hangup_after_bridge, Boolean.TRUE));
        agentDialplan.addAction(Set.create(FreeswitchVariables.call_timeout, 20));
        agentDialplan.setMaxDelayBeforeAgentAnswer(20);
        agentDialplan.addAction(Set.create(FreeswitchVariables.continue_on_fail, Boolean.TRUE));

        //agentDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        //agentDialplan.addAction(new Bridge("${sofia_contact(" + dialerInfoPojo.getAgentExt() +"@"+agenService.getAgent(dialerInfoPojo.getAgentExt()).getFreeswitchDomain()+ ")}"));
        AgentTMS calleeAgent = agenService.getAgent(dialerInfoPojo.getAgentExt());
        FreeswitchNode freeswitchNode = freeswitchService.getFreeswitchNodeForCallUUID(agentDialplan.getCall_uuid());

        if (calleeAgent != null && freeswitchNode != null) {
            if (calleeAgent.getFreeswitchIP().equals(freeswitchNode.getFreeSWITCH_IPv4())) {
                agentDialplan.addAction(new BridgeToSofiaContact(dialerInfoPojo.getAgentExt(), calleeAgent.getFreeswitchDomain()));
            }
            else{
                agentDialplan.addAction(new TMSOrder(DDD.CONNECT_TO_AGENT_OTHER_NODE));
                agentDialplan.addAction(new BridgeToAgent(calleeAgent.getFreeswitchIP(), calleeAgent.getExtension()));
            }
        } else {
            agentDialplan.addAction(new BridgeToSofiaContact(dialerInfoPojo.getAgentExt(), calleeAgent.getFreeswitchDomain()));
        }
        
        agentDialplan.addAction(new TMSOrder(DDD.PLACE_CALL_IN_FIFO));
        //agentDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        agentDialplan.addBridge(new BridgeToFifo(FreeswitchContext.agent_dp, freeswitchService.getFreeswitchIPNew(agentDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));

        OriginateBuilder originateBuilder = new OriginateBuilder();
        originateBuilder.putInBothLegs(FreeswitchVariables.tms_uuid, TMS_UUID);
        originateBuilder.putInBothLegs(FreeswitchVariables.is_tms_dp, Boolean.TRUE);
        originateBuilder.putInBothLegs(FreeswitchVariables.tms_order, DDD.CONNECT_TO_AGENT.name());
        //originateBuilder.putInBothLegs(FreeswitchVariables.origination_caller_id_name, dialerInfoPojo.getPhoneToTypeSingle().getFirstName() + "_" + dialerInfoPojo.getPhoneToTypeSingle().getLastName());
        //originateBuilder.putInBothLegs(FreeswitchVariables.origination_caller_id_number, dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber());
        //originateBuilder.putInBothLegs(FreeswitchVariables.tms_transfer, Boolean.FALSE);

        originateBuilder.appendALeg("sofia/fifo/sip:");
        originateBuilder.appendALeg(dialerInfoPojo.getAgentExt());
        originateBuilder.appendALeg("@");
        originateBuilder.appendALeg(agenService.getFreeswitchIPForExt(dialerInfoPojo.getAgentExt()));
        originateBuilder.appendALeg(":");
        originateBuilder.appendALeg(FreeswitchContext.agent_dp.getPort());
        originateBuilder.appendALeg(" ");

        originateBuilder.appendBLeg(dialerInfoPojo.getAgentExt());
        originateBuilder.appendBLeg(" XML " + FreeswitchContext.fifo_dp);
        agentDialplan.setOriginate(originateBuilder.build());

        agentDialplan.setBean(BeanServices.DDDialplan);
        agentDialplan.setFunctionCall(DDD.TRANSFER_TO_AGENT.getMethodName());

        agentDialplan.setOriginateIP(agenService.getFreeswitchIPForExt(dialerInfoPojo.getAgentExt()));
        setOriginate(agentDialplan);

        agentDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(agentDialplan);

        callDetailRecordService.saveCDR(callDetailRecord);

    }

     private void callEnteringAgentOtherNode(TMSDialplan old) {
        TMSDialplan agentDialplan;
        agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.agent_dp, DDD.CONNECT_TO_AGENT_OTHER_NODE);
        commonVariable(agentDialplan, old);

        CallDetailRecordTMS callDetailRecord = callDetailRecordService.getCDR(TMS_UUID);
        callDetailRecord.setAmdExtTransferTo(dialerInfoPojo.getAgentExt());

        agentDialplan.addAction(new Export("nolocal:api_on_media=uuid_broadcast ${uuid} playback::tone_stream://%(" + configuration.getAMDPlayBeepToAgentDuration() + ",50," + configuration.getAMDPlayBeepToAgentHZ() + ") bleg"));

        agentDialplan.addAction(Set.create(FreeswitchVariables.hangup_after_bridge, Boolean.TRUE));
        agentDialplan.addAction(Set.create(FreeswitchVariables.call_timeout, 20));
        agentDialplan.setMaxDelayBeforeAgentAnswer(20);
        agentDialplan.addAction(Set.create(FreeswitchVariables.continue_on_fail, Boolean.TRUE));

        AgentTMS calleeAgent = agenService.getAgent(dialerInfoPojo.getAgentExt());

        agentDialplan.addAction(new BridgeToSofiaContact(dialerInfoPojo.getAgentExt(), calleeAgent.getFreeswitchDomain()));
        
        agentDialplan.addAction(new TMSOrder(DDD.PLACE_CALL_IN_FIFO));
        //agentDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        agentDialplan.addBridge(new BridgeToFifo(FreeswitchContext.agent_dp, freeswitchService.getFreeswitchIPNew(agentDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));

        agentDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(agentDialplan);


    }

    public void callOriginatingToFifo(TMSDialplan old) {
        TMSDialplan fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, DDD.CONNECT_TO_AGENT);
        commonVariable(fifoDialplan, old);
        fifoDialplan.addAction(Set.create("bypass_media", Boolean.TRUE));
        fifoDialplan.addAction(new Set("fifo_bridge_uuid=" + old.getUniqueID()));
        fifoDialplan.setBean(BeanServices.DDDialplan);
        fifoDialplan.setFunctionCall(DDD.CONNECT_TO_AGENT.getMethodName());
        fifoDialplan.addAction(new Set("fifo_music=$${hold_music}"));
        fifoDialplan.addAction(new Answer());

        fifoDialplan.addBridge(new Fifo("OutboundDialerQueue_" + dialerInfoPojo.getSettings().getDialerQueuePk() + " out nowait"));

        fifoDialplan.setXMLFromDialplan();

        dialplanService.updateTMSDialplan(fifoDialplan);
    }

}
