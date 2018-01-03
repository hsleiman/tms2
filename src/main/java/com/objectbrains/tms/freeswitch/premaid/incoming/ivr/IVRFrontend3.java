/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.incoming.ivr;

import com.objectbrains.svc.iws.CallerId;
import com.objectbrains.svc.iws.InboundDialerQueueRecord;
import com.objectbrains.svc.iws.SvcException;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.RecordedPhrases;
import com.objectbrains.tms.enumerated.refrence.BeanServices;
import com.objectbrains.tms.enumerated.refrence.HOLDOrder;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.Answer;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToIVR;
import com.objectbrains.tms.freeswitch.dialplan.action.Fifo;
import com.objectbrains.tms.freeswitch.dialplan.action.Playback;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.dialplan.action.Sleep;
import com.objectbrains.tms.freeswitch.dialplan.action.TMSOrder;
import com.objectbrains.tms.freeswitch.dialplan.action.Transfer;
import com.objectbrains.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;
import com.objectbrains.tms.freeswitch.premaid.incoming.*;
import com.objectbrains.tms.service.freeswitch.FifoService;

/**
 *
 * @author hsleiman
 */
public class IVRFrontend3 extends DialplanBuilder {

    private AgentIncomingDistributionOrder aido;
    
    private static String path = "";

    public IVRFrontend3(DialplanVariable variable, AgentIncomingDistributionOrder aido) {
        log.info(aido.toJson());
        setVariable(variable);
        this.aido = aido;
    }

    @Override
    public void createDialplans() {
//        agentDialplan = dialplanRepository.createTMSDialplan(TMS_UUID, FreeswitchContext.AGENT_DIALPLAN);
//        agentDialplan.setTms_type(this.getClass().getSimpleName());

    }

    @Override
    public void buildDialplans() {
        callEnteringSBC();
        welcomeIVR();

        buildDialplansNoSBC();
    }

    public TMSDialplan buildDialplansNoSBC() {

        customerServiceFifo();
        biuldVoicemailOption();
        useAutomatedSystem();
        return null;
    }

    @Override
    public void saveDialplans() {
    }

    private void commonVariable(TMSDialplan tMSDialplan) {
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setDebugOn(getDebugOn());
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCallerId(CallerId.ACTUAL);
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setCallDirection(CallDirection.INBOUND);
        tMSDialplan.setDialer(Boolean.FALSE);
        tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
        tMSDialplan.setCaller(inVariables.getCallerIdNumber());
        tMSDialplan.setBorrowerInfo(aido.getBorrowerInfo());

    }

    private void callEnteringSBC() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.sbc_dp);
        commonVariable(tmsDialplan);
        tmsDialplan.setRecord(Boolean.TRUE);
        tmsDialplan.setOutboundVendor(Boolean.FALSE);
        tmsDialplan.setVariables(inVariables.toJson());
        tmsDialplan.setDialer(Boolean.FALSE);
        tmsDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        tmsDialplan.addBridge(new BridgeToIVR(freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.ivr_dp)));
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
        setReturnDialplan(tmsDialplan);
    }

    private void welcomeIVR() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp);
        commonVariable(tmsDialplan);
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        //tmsDialplan.addAction(new Playback("shout://translate.google.com/translate_tts?tl=en&q=Welcome+To+CashCall+Auto"));
        tmsDialplan.addAction(new Playback(RecordedPhrases.WELCOME_TO_CASHCALL_AUTO, configuration.getCompanyInfo()));
        tmsDialplan.addAction(new TMSOrder(IVRFrontend3.useAutomatedSystem));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }
    
    public static String useAutomatedSystem = "useAutomatedSystem";
    private void useAutomatedSystem() {
          // If you are calling in to make a payment and would like to use our automated system, please press 1.
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, useAutomatedSystem);
        commonVariable(tmsDialplan);
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.addAction(new Playback(path, useAutomatedSystem));
        
        tmsDialplan.addAction(new TMSOrder(IVRFrontend3.useAutomatedSystem));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }
    
    
    public static String talkToRepresentative = "talkToRepresentative";
    private void talkToRepresentative() {
        
    }
    
    public static String iSeeYourPhoneNumber = "iSeeYourPhoneNumber";
    private void iSeeYourPhoneNumber() {
        
    }
    
    
    
    public static String forSecurityPurposesEnterLast4SSN = "forSecurityPurposesEnterLast4SSN";
    private void forSecurityPurposesEnterLast4SSN() {
        
    }
    
    public static String last4SSNCheck = "last4SSNCheck";
    private void last4SSNCheck() {
        
    }

    public static String checkSecurityForLoan = "checkSecurityForLoan";
    private void checkSecurityForLoan() {
        
    }

    private void customerServiceFifo() {
        TMSDialplan fifoDialplan;
        fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD_CUSTOMER_SERVICE);
        commonVariable(fifoDialplan);
        fifoDialplan.addAction(new Set("fifo_music", configuration.getFiFoHoldMusic()));
        fifoDialplan.addAction(new Answer());
        fifoDialplan.setBean(BeanServices.FifoService);
        fifoDialplan.setFunctionCall(FifoService.placeCallOnHold);
        //fifoDialplan.setOnce(Boolean.FALSE);

        fifoDialplan.setDialerQueueId(1l);

        fifoDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_IN_VOICEMAIL));
        fifoDialplan.addAction(new Set("fifo_orbit_exten=1000:" + configuration.getMaxCallOnHoldAllowed()));
        fifoDialplan.addAction(new Set("fifo_chime_list=" + RecordedPhrases.HOLD_ANNOUNCE_PRESS1.getAudioPath()));
        fifoDialplan.addAction(new Set("fifo_chime_freq=" + configuration.getMaxHoldAnnounceTimeInSec()));
        fifoDialplan.addAction(new Set("fifo_caller_exit_key=1"));
        fifoDialplan.addAction(new Set("fifo_caller_exit_to_orbit=true"));

        Long qPk = 1l;
        try {
            InboundDialerQueueRecord record = tmsIWS.getDefaultInboundQueueRecord();
            dialerQueueRecordRepository.storeInboundDialerQueueRecord(record);
            qPk = record.getDqPk();
        } catch (SvcException ex) {
            log.error("This is error in calling defaul inbound queue: {}", ex);
        }
        fifoDialplan.addBridge(new Fifo("InboundDialerQueue_" + qPk + " in"));

        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);
    } 

    private void biuldVoicemailOption() {
        IncomingVoicemail builder = new IncomingVoicemail(inVariables, aido);
        builder.setTMS_UUID(TMS_UUID);
        builder.buildDialplansWithoutSBC();
    }

}
