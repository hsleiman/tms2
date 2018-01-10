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
import com.amp.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.service.freeswitch.FifoService;

/**
 *
 * 
 */
public class IncomingIVR2 extends DialplanBuilder {

    private AgentIncomingDistributionOrder aido;

    public IncomingIVR2(DialplanVariable variable, AgentIncomingDistributionOrder aido) {
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
        CheckSecurityForLoan();
        VerifiedSecurityForLoan();
        ASKAreYouCallingForLoan();
        VerifiedCorrectLoanId();
        CheckProtfolioCondition();
        buildDialplansNoSBC();
    }

    public TMSDialplan buildDialplansNoSBC() {

        AskToChangeScheduledPayment();
        ChangeScheduledPayment();
        CheckIfDayIsLargerThen15();
        CheckChangedScheduledPaymentDate();
        VerifiedChangedScheduledPaymentDate();
        VerifiedChangedScheduledPaymentDateWithLateFee();
        CheckChangedScheduledPaymentCheckingAccount();
        VerifiedChangedScheduledPaymentCheckingAccount();
        ConfirmPaymentChange();
        EndPaymentChange();
        //QueueFifo();
        CustomerServiceFifo();
        biuldVoicemailOption();
        return CheckACHCondition();
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
        tmsDialplan.addAction(new TMSOrder(IVROrder2.CHECK_SECURITY_FOR_LOAN));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void CheckSecurityForLoan() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.CHECK_SECURITY_FOR_LOAN);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.CHECK_SECURITY_FOR_LOAN.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(10);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void VerifiedSecurityForLoan() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.VERIFIED_SECURITY_FOR_LOAN);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.VERIFIED_SECURITY_FOR_LOAN.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(11);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void ASKAreYouCallingForLoan() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.ASK_ARE_YOU_CALLING_FOR_LOAN);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.ASK_ARE_YOU_CALLING_FOR_LOAN.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(12);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void VerifiedCorrectLoanId() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.VERIFIED_CORRECT_LOAN_ID);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.VERIFIED_CORRECT_LOAN_ID.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(13);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void CheckProtfolioCondition() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.CHECK_PORTFOLIO_CONDITION);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.CHECK_PORTFOLIO_CONDITION.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(14);
        dialplanService.updateTMSDialplan(tmsDialplan);

    }

    private TMSDialplan CheckACHCondition() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.CHECK_ACH_CONDITION);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.CHECK_ACH_CONDITION.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(15);
        dialplanService.updateTMSDialplan(tmsDialplan);
        return tmsDialplan;
    }

    private void AskToChangeScheduledPayment() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.ASK_TO_CHANGE_SCHEDULED_PAYMENT);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.ASK_TO_CHANGE_SCHEDULED_PAYMENT.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(16);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void ChangeScheduledPayment() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.CHANGE_SCHEDULED_PAYMENT);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.CHANGE_SCHEDULED_PAYMENT.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(17);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void CheckIfDayIsLargerThen15() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.CHECK_IF_DAY_IS_LARGER_THEN_15);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.CHECK_IF_DAY_IS_LARGER_THEN_15.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(18);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void CheckChangedScheduledPaymentDate() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.CHECK_CHANGED_SCHEDULED_PAYMENT_DATE);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.CHECK_CHANGED_SCHEDULED_PAYMENT_DATE.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(19);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void VerifiedChangedScheduledPaymentDate() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.VERIFIED_CHANGED_SCHEDULED_PAYMENT_DATE);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.VERIFIED_CHANGED_SCHEDULED_PAYMENT_DATE.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(20);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }
    
    private void VerifiedChangedScheduledPaymentDateWithLateFee(){
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.VERIFIED_CHANGED_SCHEDULED_PAYMENT_DATE_WITH_LATE_FEE);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.VERIFIED_CHANGED_SCHEDULED_PAYMENT_DATE_WITH_LATE_FEE.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(20);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void CheckChangedScheduledPaymentCheckingAccount() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.CHECK_CHANGED_SCHEDULED_PAYMENT_CHECKING_ACCOUNT);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.CHECK_CHANGED_SCHEDULED_PAYMENT_CHECKING_ACCOUNT.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(21);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void VerifiedChangedScheduledPaymentCheckingAccount() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.VERIFIED_CHANGED_SCHEDULED_PAYMENT_CHECKING_ACCOUNT);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.VERIFIED_CHANGED_SCHEDULED_PAYMENT_CHECKING_ACCOUNT.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(22);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void ConfirmPaymentChange() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.CONFIRM_PAYMENT_CHANGE);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.CONFIRM_PAYMENT_CHANGE.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(23);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }
    
    private void EndPaymentChange(){
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.END_PAYMENT_CHANGE);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2);
        tmsDialplan.setFunctionCall(IVROrder2.END_PAYMENT_CHANGE.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.setIvrStepCount(24);
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private void QueueFifo() {
        TMSDialplan fifoDialplan;
        fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD);
        commonVariable(fifoDialplan);
        fifoDialplan.addAction(new Set("fifo_music", configuration.getFiFoHoldMusic()));
        fifoDialplan.addAction(new Answer());
        fifoDialplan.setBean(BeanServices.FifoService);
        fifoDialplan.setFunctionCall(FifoService.placeCallOnHold);
        fifoDialplan.setDialerQueueId(aido.getSettings().getDialerQueuePk());

        fifoDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_IN_VOICEMAIL));
        fifoDialplan.addAction(new Set("fifo_orbit_exten=1000:" + configuration.getMaxCallOnHoldAllowed()));
        fifoDialplan.addAction(new Set("fifo_chime_list=" + RecordedPhrases.HOLD_ANNOUNCE_PRESS1.getAudioPath()));
        fifoDialplan.addAction(new Set("fifo_chime_freq=" + configuration.getMaxHoldAnnounceTimeInSec()));
        fifoDialplan.addAction(new Set("fifo_caller_exit_key=1"));
        fifoDialplan.addAction(new Set("fifo_caller_exit_to_orbit=true"));

        fifoDialplan.addBridge(new Fifo("InboundDialerQueue_" + aido.getSettings().getDialerQueuePk() + " in"));
        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);
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
        IncomingVoicemail builder = new IncomingVoicemail(inVariables, aido);
        builder.setTMS_UUID(TMS_UUID);
        builder.buildDialplansWithoutSBC();
    }

}
