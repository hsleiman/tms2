/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.incoming;

import com.amp.crm.constants.CallerId;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.RecordedPhrases;
import com.amp.tms.enumerated.refrence.BeanServices;
import com.amp.tms.enumerated.refrence.HOLDOrder;
import com.amp.tms.enumerated.refrence.IVROrder;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.Answer;
import com.amp.tms.freeswitch.dialplan.action.BridgeToIVR;
import com.amp.tms.freeswitch.dialplan.action.Fifo;
import com.amp.tms.freeswitch.dialplan.action.PlayAndGetDigits;
import com.amp.tms.freeswitch.dialplan.action.Playback;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.Sleep;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.dialplan.action.Transfer;
import com.amp.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.service.freeswitch.FifoService;

/**
 *
 * @author hsleiman
 */
public class IncomingIVR extends DialplanBuilder {

    private AgentIncomingDistributionOrder aido;

    public IncomingIVR(DialplanVariable variable, AgentIncomingDistributionOrder aido) {
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
        WelcomeIVR();
        IdTheLoanAndTryToVerify();
        VerifyLoanId();
        MainMenu();
        VerifyMain();
        SelectPaymentType();
        SelectPaymentEntry();
        SelectedPaymentEntry();
        ReviewPaymentEntry();
        ApplyPaymentEntry();
        CustomerServiceFifo();
        biuldVoicemailOption();
    }

    public TMSDialplan buildDialplansNoSBC() {

        VerifyMain();
        SelectPaymentType();
        CustomerServiceFifo();
        biuldVoicemailOption();
        return MainMenu();
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

    private void WelcomeIVR() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp);
        commonVariable(tmsDialplan);
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        //tmsDialplan.addAction(new Playback("shout://translate.google.com/translate_tts?tl=en&q=Welcome+To+CashCall+Auto"));
        tmsDialplan.addAction(new Playback(RecordedPhrases.WELCOME_TO_CASHCALL_AUTO, configuration.getCompanyInfo()));
        tmsDialplan.addAction(new TMSOrder(IVROrder.FOUND_LOAN_ID));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void IdTheLoanAndTryToVerify() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder.FOUND_LOAN_ID);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain);
        tmsDialplan.setFunctionCall(IVROrder.FOUND_LOAN_ID.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void VerifyLoanId() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder.VERIFY_LOAN_ID);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain);
        tmsDialplan.setFunctionCall(IVROrder.VERIFY_LOAN_ID.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    public TMSDialplan MainMenu() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder.MAIN_MENU);
        commonVariable(tmsDialplan);
        String fileToPlay = RecordedPhrases.MAIN_PROMPT.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 3, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new TMSOrder(IVROrder.VERIFY_MAIN));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
        return tmsDialplan;
    }

    private void VerifyMain() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder.VERIFY_MAIN);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain);
        tmsDialplan.setFunctionCall(IVROrder.VERIFY_MAIN.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void SelectPaymentType() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder.SELECT_PAYMENT_TYPE);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain);
        tmsDialplan.setFunctionCall(IVROrder.SELECT_PAYMENT_TYPE.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void SelectPaymentEntry() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder.SELECT_PAYMENT_ENTRY);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain);
        tmsDialplan.setFunctionCall(IVROrder.SELECT_PAYMENT_ENTRY.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void SelectedPaymentEntry() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder.SELECTED_PAYMENT_ENTRY);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain);
        tmsDialplan.setFunctionCall(IVROrder.SELECTED_PAYMENT_ENTRY.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void ReviewPaymentEntry() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder.REVIEW_PAYMENT_ENTRY);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain);
        tmsDialplan.setFunctionCall(IVROrder.REVIEW_PAYMENT_ENTRY.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void ApplyPaymentEntry() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder.APPLY_PAYMENT_ENTRY);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain);
        tmsDialplan.setFunctionCall(IVROrder.APPLY_PAYMENT_ENTRY.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
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
        fifoDialplan.setDialerQueueId(aido.getSettings().getDialerQueuePk());
        
        fifoDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_IN_VOICEMAIL));
        fifoDialplan.addAction(new Set("fifo_orbit_exten=1000:" + configuration.getMaxCallOnHoldAllowed()));
        fifoDialplan.addAction(new Set("fifo_chime_list=" + RecordedPhrases.HOLD_ANNOUNCE_PRESS1.getAudioPath()));
        fifoDialplan.addAction(new Set("fifo_chime_freq="+configuration.getMaxHoldAnnounceTimeInSec()));
        fifoDialplan.addAction(new Set("fifo_caller_exit_key=1"));
        fifoDialplan.addAction(new Set("fifo_caller_exit_to_orbit=true"));
        
        fifoDialplan.addBridge(new Fifo("InboundDialerQueue_" + aido.getSettings().getDialerQueuePk() + " in"));
        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);
    }

    private void biuldVoicemailOption() {
        IncomingVoicemail builder = new IncomingVoicemail(inVariables, aido);
        builder.setTMS_UUID(TMS_UUID);
        builder.buildDialplansWithoutSBC();
    }

}
