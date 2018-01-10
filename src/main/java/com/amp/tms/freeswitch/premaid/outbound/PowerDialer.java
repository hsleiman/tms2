/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.outbound;

import com.amp.crm.constants.PopupDisplayMode;
import com.amp.crm.constants.PreviewDialerType;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.refrence.BeanServices;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.BridgeToSofiaContact;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.originate.OriginateBuilder;
import com.amp.tms.freeswitch.pojo.DialerInfoPojo;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;

/**
 *
 * 
 */
public class PowerDialer extends DialplanBuilder {


    private DialerInfoPojo dialerInfoPojo;

    public PowerDialer(DialerInfoPojo dialerInfoPojo) {
        super();
        log.info("PowerDialer");
        this.dialerInfoPojo = dialerInfoPojo;
        setTMS_UUID(this.dialerInfoPojo.getCallUUID());
    }

    @Override
    public void createDialplans() {
        log.info("Create Dialplans");
    }

    @Override
    public void buildDialplans() {
        callEnteringAgent();
        callOriginatingToSBC();
    }

    @Override
    public void saveDialplans() {

    }

    public void commonVariable(TMSDialplan tMSDialplan) {
        log.info("Adding Common: "+tMSDialplan.getKey().getContext());
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setCallDirection(CallDirection.OUTBOUND);
        tMSDialplan.setCallee(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber() + "");
        tMSDialplan.setCaller(dialerInfoPojo.getAgentExt() + "");
        tMSDialplan.getBorrowerInfo().setBorrowerPhoneNumber(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber() + "");
        tMSDialplan.getBorrowerInfo().setBorrowerPhoneNumberType(dialerInfoPojo.getPhoneToTypeSingle().getPhoneType());
        tMSDialplan.getBorrowerInfo().setBorrowerFirstName(dialerInfoPojo.getBorrowerFirstName());
        tMSDialplan.getBorrowerInfo().setBorrowerLastName(dialerInfoPojo.getBorrowerLastName());
        tMSDialplan.setDialer(Boolean.TRUE);
        //tMSDialplan.addAction(Set.create(FreeswitchVariables.tms_transfer, Boolean.FALSE));
        tMSDialplan.addAction(new Set(FreeswitchVariables.ringback, "${us-ring}"));
        tMSDialplan.setCallerId(dialerInfoPojo.getSettings().getCallerId());
        tMSDialplan.setCallerIdNumberMask(dialerInfoPojo.getSettings().getCallerIdNumber());
        tMSDialplan.setDebugOn(Boolean.TRUE);
    }

    public void callEnteringAgent() {
        log.info("Create Agent Dialplan");
        TMSDialplan agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.agent_dp);
        commonVariable(agentDialplan);

        agentDialplan.setRecord(Boolean.FALSE);        
        agentDialplan.setAutoAswer(dialerInfoPojo.getSettings().isAutoAnswerEnabled());
        if (dialerInfoPojo.getPreviewDialerType() == PreviewDialerType.REGULAR) {
            agentDialplan.setPopupType(dialerInfoPojo.getSettings().getPopupDisplayMode());
        } else {
            agentDialplan.setPopupType(PopupDisplayMode.SAME_WINDOW);
        }
        
        agentDialplan.setOutboundVendor(Boolean.FALSE);
        
        
        //agentDialplan.addAction(new Export("beep_api_result_amdt=${sched_api(+2 ${uuid}_amdt uuid_broadcast ${uuid} playback::tone_stream://%("+configuration.getPowerPlayBeepToAgentDuration()+",50,"+configuration.getPowerPlayBeepToAgentHZ()+") bleg)}"));
        
        agentDialplan.setDialerQueueId(dialerInfoPojo.getSettings().getDialerQueuePk());
        agentDialplan.getBorrowerInfo().setLoanId(dialerInfoPojo.getLoanId());
        //agentDialplan.addBridge(new Bridge("${sofia_contact(" + dialerInfoPojo.getAgentExt() + ")}"));
        agentDialplan.addAction(new BridgeToSofiaContact(dialerInfoPojo.getAgentExt(), agenService.getAgent(dialerInfoPojo.getAgentExt()).getFreeswitchDomain()));
        
        
        agentDialplan.setXMLFromDialplan();
        
        dialplanService.updateTMSDialplan(agentDialplan);
       
    }

    public void callOriginatingToSBC() {
        log.info("Create SBC Dialplan");
        TMSDialplan sbcDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.sbc_dp);
        commonVariable(sbcDialplan);
        sbcDialplan.setRecord(Boolean.TRUE);
        sbcDialplan.setOutboundVendor(Boolean.TRUE);
//        sbcDialplan.setCallerId(dialerInfoPojo.getSettings().getCallerId());
//        sbcDialplan.setCallerIdNumberMask(dialerInfoPojo.getSettings().getCallerIdNumber());
        
        sbcDialplan.setBean(BeanServices.DDDialplan);
        sbcDialplan.setFunctionCall("callInProgress");
        
        OriginateBuilder originateBuilder = new OriginateBuilder();
        originateBuilder.putInBothLegs(FreeswitchVariables.tms_uuid, TMS_UUID);
        originateBuilder.putInBothLegs(FreeswitchVariables.call_direction, CallDirection.OUTBOUND.name());
        originateBuilder.putInBothLegs(FreeswitchVariables.is_tms_dp, Boolean.TRUE);
        originateBuilder.putInBothLegs(FreeswitchVariables.is_dialer, Boolean.TRUE);
        originateBuilder.putInBothLegs(FreeswitchVariables.tms_transfer, Boolean.FALSE);
        originateBuilder.putInBothLegs(FreeswitchVariables.dialer_queue_id, dialerInfoPojo.getSettings().getDialerQueuePk());
        originateBuilder.putInBothLegs(FreeswitchVariables.origination_callee_id_name, dialerInfoPojo.getPhoneToTypeSingle().getFirstName()+"_"+dialerInfoPojo.getPhoneToTypeSingle().getLastName());
        originateBuilder.putInBothLegs(FreeswitchVariables.origination_callee_id_number, dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber());
        // You need this because if you remove it the system would think its transfer call.
        originateBuilder.putInBothLegs(FreeswitchVariables.tms_transfer, Boolean.FALSE);
        
        // Should be Sbc then agent.
        originateBuilder.appendALeg("sofia/sbc/sip:");
        originateBuilder.appendALeg(dialerInfoPojo.getAgentExt());
        originateBuilder.appendALeg("@");
        originateBuilder.appendALeg(agenService.getFreeswitchIPForExt(dialerInfoPojo.getAgentExt()));
        originateBuilder.appendALeg(":");
        originateBuilder.appendALeg(FreeswitchContext.agent_dp.getPort());
        //TODO need to add
        originateBuilder.appendALeg(";transport=tcp");
        
        originateBuilder.appendBLeg(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber());
        originateBuilder.appendBLeg(" XML sbc_dp");
      
        
        sbcDialplan.setOriginate(originateBuilder.build());
        sbcDialplan.setOriginateIP(freeswitchService.getFreeswitchIPNew(sbcDialplan.getCall_uuid(), FreeswitchContext.sbc_dp));
        setOriginate(sbcDialplan);
        
        sbcDialplan.setXMLFromDialplan();
        
        dialplanService.updateTMSDialplan(sbcDialplan);
    }
}
