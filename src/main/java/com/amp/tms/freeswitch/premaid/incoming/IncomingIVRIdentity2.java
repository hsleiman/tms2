/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.incoming;

import com.amp.crm.constants.CallerId;
import com.amp.crm.embeddable.InboundDialerQueueRecord;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.RecordedPhrases;
import com.amp.tms.enumerated.refrence.BeanServices;
import com.amp.tms.enumerated.refrence.HOLDOrder;
import com.amp.tms.enumerated.refrence.IVROrder2;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.Answer;
import com.amp.tms.freeswitch.dialplan.action.BridgeToIVR;
import com.amp.tms.freeswitch.dialplan.action.Fifo;
import com.amp.tms.freeswitch.dialplan.action.Playback;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.Sleep;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.dialplan.action.Transfer;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.service.freeswitch.FifoService;

/**
 *
 * 
 */
public class IncomingIVRIdentity2 extends DialplanBuilder {

    public IncomingIVRIdentity2(DialplanVariable variable) {
        super();
        setVariable(variable);
    }

    @Override
    public void createDialplans() {
//        agentDialplan = dialplanRepository.createTMSDialplan(TMS_UUID, FreeswitchContext.AGENT_DIALPLAN);
//        agentDialplan.setTms_type(this.getClass().getSimpleName());

    }

    @Override
    public void buildDialplans() {
        callEnteringSBC();
        WelcomeIVR();
        AskForSSNOrLoan();
        CheckForSSNOrLoan();
        AskForLoan();
        AskForSSN();
        AskForDOB();
        CheckSecurityForDOB();
        VerifiedSecurityForDOB();
        CustomerServiceFifo();
        biuldVoicemailOption();
    }

    @Override
    public void saveDialplans() {
    }

    public void commonVariable(TMSDialplan tMSDialplan) {
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setDebugOn(getDebugOn());
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCallerId(CallerId.ACTUAL);
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setCallDirection(CallDirection.INBOUND);
        tMSDialplan.setDialer(Boolean.FALSE);
        tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
        tMSDialplan.setCaller(inVariables.getCallerIdNumber());
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

    private void WelcomeIVR() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp);
        commonVariable(tmsDialplan);
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.addAction(new Playback(RecordedPhrases.WELCOME_TO_CASHCALL_AUTO, configuration.getCompanyInfo()));
        tmsDialplan.addAction(new TMSOrder(IVROrder2.ASK_FOR_SSN_OR_LOAN));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.setIvrStepCount(0);
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void AskForSSNOrLoan() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.ASK_FOR_SSN_OR_LOAN);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRCallerIdentity2);
        tmsDialplan.setFunctionCall(IVROrder2.ASK_FOR_SSN_OR_LOAN.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(1);
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void CheckForSSNOrLoan() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.CHECK_FOR_SSN_OR_LOAN);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRCallerIdentity2);
        tmsDialplan.setFunctionCall(IVROrder2.CHECK_FOR_SSN_OR_LOAN.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(2);
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void AskForLoan() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.ASK_FOR_LOAN);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRCallerIdentity2);
        tmsDialplan.setFunctionCall(IVROrder2.ASK_FOR_LOAN.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(3);
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void AskForSSN() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.ASK_FOR_SSN);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRCallerIdentity2);
        tmsDialplan.setFunctionCall(IVROrder2.ASK_FOR_SSN.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(4);
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void AskForDOB() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.ASK_FOR_DOB);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRCallerIdentity2);
        tmsDialplan.setFunctionCall(IVROrder2.ASK_FOR_DOB.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(5);
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void CheckSecurityForDOB() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.CHECK_SECURITY_FOR_DOB);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRCallerIdentity2);
        tmsDialplan.setFunctionCall(IVROrder2.CHECK_SECURITY_FOR_DOB.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(6);
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void VerifiedSecurityForDOB() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.VERIFIED_SECURITY_FOR_DOB);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRCallerIdentity2);
        tmsDialplan.setFunctionCall(IVROrder2.VERIFIED_SECURITY_FOR_DOB.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(7);
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void CustomerServiceFifo() {
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
            InboundDialerQueueRecord record = dialerQueueService.getDefaultInboundQueueRecord();
            dialerQueueRecordRepository.storeInboundDialerQueueRecord(record);
            qPk = record.getDqPk();
        } catch (Exception ex) {
            log.error("This is error in calling defaul inbound queue: {}", ex);
        }
        fifoDialplan.addBridge(new Fifo("InboundDialerQueue_" + qPk + " in"));

        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);
    }

    private void biuldVoicemailOption() {
        IncomingVoicemail builder = new IncomingVoicemail(inVariables);
        builder.setTMS_UUID(TMS_UUID);
        builder.buildDialplansWithoutSBC();
    }
}
