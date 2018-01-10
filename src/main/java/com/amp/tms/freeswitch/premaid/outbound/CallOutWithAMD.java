/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.outbound;

import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.refrence.BeanServices;
import com.amp.tms.enumerated.refrence.DDD;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.AMD_VoiceStart;
import com.amp.tms.freeswitch.dialplan.action.AMD_VoiceStop;
import com.amp.tms.freeswitch.dialplan.action.DisplaceSession;
import com.amp.tms.freeswitch.dialplan.action.Playback;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.dialplan.action.ToneDetect;
import com.amp.tms.freeswitch.dialplan.action.Transfer;
import com.amp.tms.freeswitch.dialplan.action.WaitForAnswer;
import com.amp.tms.freeswitch.originate.OriginateBuilder;
import com.amp.tms.freeswitch.pojo.DialerInfoPojo;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;

/**
 *
 * 
 */
public class CallOutWithAMD extends DialplanBuilder {

    private DialerInfoPojo dialerInfoPojo;

    public CallOutWithAMD(DialerInfoPojo dialerInfoPojo) {
        super();
        this.dialerInfoPojo = dialerInfoPojo;
        log.info("********* Progresive Dialer AMD Starting Progresive");
        log.info("Progresive Dialer AMD Starting Progresive: {} {} {} {} {}", dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber(), dialerInfoPojo.getBorrowerFirstName(), dialerInfoPojo.getLoanId(), dialerInfoPojo.getAgentExt(), dialerInfoPojo.getCallUUID());
        log.info("Progresive Dialer AMD Starting Progresive: {} {} {} {} {}", dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber(), dialerInfoPojo.getBorrowerFirstName(), dialerInfoPojo.getLoanId(), dialerInfoPojo.getAgentExt(), dialerInfoPojo.getCallUUID());
        log.info("Progresive Dialer AMD Starting Progresive: {} {} {} {} {}", dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber(), dialerInfoPojo.getBorrowerFirstName(), dialerInfoPojo.getLoanId(), dialerInfoPojo.getAgentExt(), dialerInfoPojo.getCallUUID());
        log.info("Progresive Dialer AMD Starting Progresive: {} {} {} {} {}", dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber(), dialerInfoPojo.getBorrowerFirstName(), dialerInfoPojo.getLoanId(), dialerInfoPojo.getAgentExt(), dialerInfoPojo.getCallUUID());
        log.info("Progresive Dialer AMD Starting Progresive: {} {} {} {} {}", dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber(), dialerInfoPojo.getBorrowerFirstName(), dialerInfoPojo.getLoanId(), dialerInfoPojo.getAgentExt(), dialerInfoPojo.getCallUUID());
        log.info("********* Progresive Dialer AMD Starting Progresive");
    }

    @Override
    public void createDialplans() {
        log.info("Create Dialplans");

    }

    @Override
    public void buildDialplans() {
        callEnteringSBC();
        callEnteringFifo();
        waitForMedia();
        startAMD();
        verifyAMD();
        detectedAsHuman();
        detectedAsMachine();
        sendToAgent();

    }

    @Override
    public void saveDialplans() {

    }

    public void commonVariable(TMSDialplan tMSDialplan) {
        log.info("Adding Common: " + tMSDialplan.getKey().getContext());
        
        
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setCallDirection(CallDirection.OUTBOUND);
        tMSDialplan.setOutboundVendor(Boolean.FALSE);
        tMSDialplan.setCallerId(dialerInfoPojo.getSettings().getCallerId());
        //Added to mask number
        tMSDialplan.setCallerIdNumberMask(dialerInfoPojo.getSettings().getCallerIdNumber());
        tMSDialplan.setPopupType(dialerInfoPojo.getSettings().getPopupDisplayMode());

        //tMSDialplan.setIgnore_early_media(Boolean.TRUE);
        tMSDialplan.setCallerId(dialerInfoPojo.getSettings().getCallerId());
        tMSDialplan.setCallerIdNumberMask(dialerInfoPojo.getSettings().getCallerIdNumber());

        tMSDialplan.setDialer(Boolean.TRUE);
        tMSDialplan.setDialerQueueId(dialerInfoPojo.getSettings().getDialerQueuePk());
        tMSDialplan.setCallee(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber() + "");

        if (dialerInfoPojo.getSettings().isAutoAnswerEnabled() == false) {
            tMSDialplan.addAction(Set.create(FreeswitchVariables.call_timeout, 20));
            tMSDialplan.setMaxDelayBeforeAgentAnswer(20);
        }

        //tMSDialplan.setCaller(dialerInfoPojo.getAgentExt() + "");
        tMSDialplan.setCaller(1000 + "");

        tMSDialplan.setAutoAswer(dialerInfoPojo.getSettings().isAutoAnswerEnabled());
        tMSDialplan.getBorrowerInfo().setBorrowerPhoneNumber(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber() + "");
        tMSDialplan.getBorrowerInfo().setBorrowerFirstName(dialerInfoPojo.getBorrowerFirstName());
        tMSDialplan.getBorrowerInfo().setBorrowerLastName(dialerInfoPojo.getBorrowerLastName());
        tMSDialplan.getBorrowerInfo().setLoanId(dialerInfoPojo.getLoanId());
        tMSDialplan.getBorrowerInfo().setBorrowerPhoneNumberType(dialerInfoPojo.getPhoneToTypeSingle().getPhoneType());
        tMSDialplan.getBorrowerInfo().setBorrowerPhoneNumber(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber() + "");

        tMSDialplan.addAction(Set.create(FreeswitchVariables.tms_transfer, Boolean.FALSE));
        tMSDialplan.setDebugOn(Boolean.TRUE);
    }

    private void callEnteringSBC() {
        TMSDialplan sbcDialplan;
        sbcDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.sbc_dp, DDD.WAIT_FOR_MEDIA);
        commonVariable(sbcDialplan);
        sbcDialplan.setOutboundVendor(Boolean.TRUE);
        sbcDialplan.setRecord(Boolean.TRUE);
        sbcDialplan.setBean(BeanServices.DDDialplan);
        sbcDialplan.setFunctionCall("callInProgress");

        boolean includeExecuteOnTone = false;
        if (configuration.getDetectBusyToneOnAMD()) {
            sbcDialplan.addAction(ToneDetect.DetectBusy());
            includeExecuteOnTone = true;
        }
        if(configuration.getDetectSITToneOnAMD()){
            sbcDialplan.addAction(ToneDetect.DetectSitHigh1());
            sbcDialplan.addAction(ToneDetect.DetectSitLow1());
            sbcDialplan.addAction(ToneDetect.DetectSitHigh2());
            sbcDialplan.addAction(ToneDetect.DetectSitLow2());
            includeExecuteOnTone = true;
        }
        if(configuration.getDetect3BusyToneOnAMD()){
            sbcDialplan.addAction(ToneDetect.Detect3Busy(configuration.getDetect3BusyToneTimeoutOnAMD()));
            includeExecuteOnTone = true;
        }
        if(includeExecuteOnTone && configuration.getExecuteOnDetectTone()){
            sbcDialplan.addAction(new Set("execute_on_tone_detect=set amd_detect_tone=true"));
        }

        sbcDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(sbcDialplan);
    }

    private void callEnteringFifo() {
        TMSDialplan fifoDialplan;
        fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, DDD.PLACE_CALL_IN_FIFO);
        commonVariable(fifoDialplan);
        fifoDialplan.setOnce(Boolean.FALSE);
        fifoDialplan.setBean(BeanServices.DDDialplan);
        fifoDialplan.setFunctionCall(DDD.PLACE_CALL_IN_FIFO.getMethodName());
        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);

    }

    private void waitForMedia() {
        TMSDialplan dq;
        dq = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.dq_dp, DDD.WAIT_FOR_MEDIA);
        commonVariable(dq);
        dq.addAction(new TMSOrder(DDD.START_AMD));
        dq.addAction(new Set("execute_on_media=transfer 1001 XML " + FreeswitchContext.dq_dp));
        dq.addAction(new WaitForAnswer());
        dq.addBridge(new Transfer("1000 XML " + FreeswitchContext.dq_dp));

        OriginateBuilder originateBuilder = new OriginateBuilder();
        originateBuilder.putInBothLegs(FreeswitchVariables.tms_uuid, TMS_UUID);
        originateBuilder.putInBothLegs(FreeswitchVariables.is_tms_dp, Boolean.TRUE);
        originateBuilder.putInBothLegs(FreeswitchVariables.is_dialer, Boolean.TRUE);
        originateBuilder.putInBothLegs(FreeswitchVariables.call_direction, CallDirection.OUTBOUND.name());
        originateBuilder.putInBothLegs(FreeswitchVariables.dialer_queue_id, dialerInfoPojo.getSettings().getDialerQueuePk());
        originateBuilder.putInBothLegs(FreeswitchVariables.origination_callee_id_name, dialerInfoPojo.getPhoneToTypeSingle().getFirstName() + "_" + dialerInfoPojo.getPhoneToTypeSingle().getLastName());
        originateBuilder.putInBothLegs(FreeswitchVariables.origination_callee_id_number, dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber());
        originateBuilder.putInBothLegs(FreeswitchVariables.tms_transfer, Boolean.FALSE);
        originateBuilder.putInBothLegs(FreeswitchVariables.tms_order_next, DDD.WAIT_FOR_MEDIA.name());

        originateBuilder.appendALeg("sofia/dq/sip:");
        originateBuilder.appendALeg(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber());
        originateBuilder.appendALeg("@");
        originateBuilder.appendALeg(freeswitchService.getFreeswitchIPNew(dq.getCall_uuid(), FreeswitchContext.sbc_dp));
        originateBuilder.appendALeg(":");
        originateBuilder.appendALeg(FreeswitchContext.sbc_dp.getPort());
        originateBuilder.appendALeg(";transport=tcp");

        originateBuilder.appendBLeg(1000);
        originateBuilder.appendBLeg(" XML " + FreeswitchContext.dq_dp);

        dq.setBean(BeanServices.DDDialplan);
        dq.setFunctionCall(DDD.WAIT_FOR_MEDIA.getMethodName());

        dq.setOriginate(originateBuilder.build());
        dq.setOriginateIP(freeswitchService.getFreeswitchIPNew(dq.getCall_uuid(), FreeswitchContext.dq_dp));
        setOriginate(dq);
        dq.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(dq);
    }

    private void startAMD() {
        TMSDialplan dq;
        dq = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.dq_dp, DDD.START_AMD);
        commonVariable(dq);
        //dq.setRecord(Boolean.TRUE);
        dq.addAction(new Set("amd_execute_on_machine=transfer 1001 XML " + FreeswitchContext.dq_dp));
        dq.addAction(new Set("amd_execute_on_person=transfer 1002 XML " + FreeswitchContext.dq_dp));

        dq.setBean(BeanServices.DDDialplan);
        dq.setFunctionCall(DDD.START_AMD.getMethodName());

        dq.addAction(new TMSOrder(DDD.VERIFY_AMD));
        if (dialerInfoPojo.getSettings().isAnsweringMachineDetection()) {
            dq.addAction(new AMD_VoiceStart());
            dq.addAction(new DisplaceSession("tone_stream://" + configuration.getAMDStartPlayBeep()));
            dq.addAction(new Playback("silence_stream://" + configuration.getAMDSleepTime()));
            //dq.addAction(new Sleep(configuration.getAMDSleepTime()));
            dq.addAction(new AMD_VoiceStop());
        } else {
            dq.addAction(new DisplaceSession("tone_stream://" + configuration.getAMDStartPlayBeep()));
        }
        //dq.addAction(new Sleep(100l));
        //dq.addAction(Set.create("bypass_media", Boolean.TRUE));
        dq.addBridge(new Transfer("1000 XML " + FreeswitchContext.dq_dp));

        dq.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(dq);
    }

    private void verifyAMD() {
        TMSDialplan dq;
        dq = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.dq_dp, DDD.VERIFY_AMD);
        commonVariable(dq);
        dq.setBean(BeanServices.DDDialplan);
        dq.setFunctionCall(DDD.VERIFY_AMD.getMethodName());
        dq.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(dq);
    }

    private void detectedAsHuman() {
        TMSDialplan dq;
        dq = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.dq_dp, DDD.DETECTED_AS_HUMAN);
        commonVariable(dq);
        dq.setOnce(Boolean.FALSE);
        dq.setBean(BeanServices.DDDialplan);
        dq.setFunctionCall(DDD.DETECTED_AS_HUMAN.getMethodName());
        dq.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(dq);
    }

    private void detectedAsMachine() {
        TMSDialplan dq;
        dq = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.dq_dp, DDD.DETECTED_AS_MACHINE);
        commonVariable(dq);

        dq.setBean(BeanServices.DDDialplan);
        dq.setFunctionCall(DDD.DETECTED_AS_MACHINE.getMethodName());
        dq.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(dq);
    }

    private void sendToAgent() {
        TMSDialplan dq;
        dq = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.agent_dp, DDD.SEND_TO_AGENT);
        commonVariable(dq);

        dq.setBean(BeanServices.DDDialplan);
        dq.setFunctionCall(DDD.SEND_TO_AGENT.getMethodName());
        dq.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(dq);
    }
}
