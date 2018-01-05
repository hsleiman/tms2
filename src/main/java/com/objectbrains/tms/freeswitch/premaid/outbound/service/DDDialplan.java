/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.outbound.service;


import com.objectbrains.sti.constants.LeaveVoiceMailAtOptions;
import com.objectbrains.sti.constants.VoiceMailOption;
import com.objectbrains.sti.db.entity.base.dialer.OutboundDialerQueueSettings;
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.tms.db.entity.cdr.CallDetailRecord;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.refrence.DDD;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.Answer;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToAgent;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToSofiaContact;
import com.objectbrains.tms.freeswitch.dialplan.action.Export;
import com.objectbrains.tms.freeswitch.dialplan.action.Fifo;
import com.objectbrains.tms.freeswitch.dialplan.action.Hangup;
import com.objectbrains.tms.freeswitch.dialplan.action.Info;
import com.objectbrains.tms.freeswitch.dialplan.action.Playback;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.dialplan.action.Sleep;
import com.objectbrains.tms.freeswitch.dialplan.action.TMSOrder;
import com.objectbrains.tms.freeswitch.dialplan.action.Transfer;
import com.objectbrains.tms.freeswitch.dialplan.action.WaitForSilence;
import com.objectbrains.tms.freeswitch.pojo.DialerInfoPojo;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.pojo.BorrowerInfo;
import com.objectbrains.tms.service.AgentCallService;
import com.objectbrains.tms.service.TMSAgentService;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.DialerQueueRecordService;
import com.objectbrains.tms.service.DispositionCodeService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.FreeswitchService;
import com.objectbrains.tms.service.dialer.CallService;
import com.objectbrains.tms.service.dialer.Dialer.CallRespondedCallback;
import com.objectbrains.tms.service.dialer.DialerService;
import com.objectbrains.tms.service.freeswitch.CallingOutService;
import com.objectbrains.tms.websocket.message.outbound.PhoneToType;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service("DDDialplan")
public class DDDialplan {

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private DialerQueueRecordService dialerQueueRecordService;

    @Autowired
    private CallService callService;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private DispositionCodeService dispositionCodeService;

    @Autowired
    private AgentCallService agentCallService;

    private static final Logger log = LoggerFactory.getLogger(DDDialplan.class);

    @Autowired
    private CallingOutService callingOutService;

    @Autowired
    private DialerService dialerService;

    @Autowired
    protected TMSAgentService agenService;
    
    @Autowired
    private FreeswitchService freeswitchService;

    @Autowired
    @Qualifier("tms-executor")
    private TaskExecutor executor;

    public TMSDialplan startAMD(DialplanVariable variable, TMSDialplan tmsDialplan) {
        CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
        callDetailRecord.setAmdStartTime(LocalDateTime.now());
        callDetailRecordService.saveCDR(callDetailRecord);
        return tmsDialplan;
    }

    public TMSDialplan waitForMedia(DialplanVariable variable, TMSDialplan tmsDialplan) {
        CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
        callDetailRecord.setAmdStartWaitForMediaTime(LocalDateTime.now());
        callDetailRecordService.saveCDR(callDetailRecord);
        return tmsDialplan;
    }

    public TMSDialplan verifyAMD(DialplanVariable variable, TMSDialplan tmsDialplan) {
        CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
        callDetailRecord.setAmdEndTime(LocalDateTime.now());

        boolean isMachine = false;
        if (variable.getAmdStatus() != null && variable.getAmdStatus().equalsIgnoreCase("machine")) {
            isMachine = true;
        } else if (variable.getAvmdDetect() != null && variable.getAvmdDetect().equalsIgnoreCase("TRUE")) {
            isMachine = true;
        }

        if (isMachine) {
            tmsDialplan.addAction(new TMSOrder(DDD.DETECTED_AS_MACHINE.name()));
            callDetailRecord.setAmd_status("machine");
            callDetailRecord.setAmd_result(variable.getAmdReslut());
        } else {
            tmsDialplan.addAction(new TMSOrder(DDD.DETECTED_AS_HUMAN.name()));
            callDetailRecord.setAmd_status("person");
            callDetailRecord.setAmd_result(variable.getAmdReslut());
        }
        tmsDialplan.addAction(new Info());
        //tmsDialplan.addAction(Set.create("bypass_media", Boolean.TRUE));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.dq_dp));

        callDetailRecordService.saveCDR(callDetailRecord);
        return tmsDialplan;
    }

    public TMSDialplan detectedAsHuman(DialplanVariable variable, TMSDialplan tmsDialplan) {
        log.debug("********* Progresive Dialer AMD detectedAsHuman Progresive [{}]", tmsDialplan.getCall_uuid());
        log.debug("Progresive Dialer AMD detectedAsHuman Progresive: {} {} {} {} {}", tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber(), tmsDialplan.getBorrowerInfo().getBorrowerFirstName(), tmsDialplan.getBorrowerInfo().getLoanId(), "", tmsDialplan.getTms_type());
        log.debug("Progresive Dialer AMD detectedAsHuman Progresive: {} {} {} {} {}", tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber(), tmsDialplan.getBorrowerInfo().getBorrowerFirstName(), tmsDialplan.getBorrowerInfo().getLoanId(), "", tmsDialplan.getTms_type());
        log.debug("Progresive Dialer AMD detectedAsHuman Progresive: {} {} {} {} {}", tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber(), tmsDialplan.getBorrowerInfo().getBorrowerFirstName(), tmsDialplan.getBorrowerInfo().getLoanId(), "", tmsDialplan.getTms_type());
        log.debug("Progresive Dialer AMD detectedAsHuman Progresive: {} {} {} {} {}", tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber(), tmsDialplan.getBorrowerInfo().getBorrowerFirstName(), tmsDialplan.getBorrowerInfo().getLoanId(), "", tmsDialplan.getTms_type());
        log.debug("Progresive Dialer AMD detectedAsHuman Progresive: {} {} {} {} {}", tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber(), tmsDialplan.getBorrowerInfo().getBorrowerFirstName(), tmsDialplan.getBorrowerInfo().getLoanId(), "", tmsDialplan.getTms_type());
        log.debug("********* Progresive Dialer AMD detectedAsHuman Progresive start [{}]", tmsDialplan.getCall_uuid());

        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(Set.create("bypass_media", Boolean.TRUE));
        //tmsDialplan.addAction(new Playback(RecordedPhrases.DNC));

        CRCallback cRCallback = new CRCallback();
        dialerService.callResponded(tmsDialplan.getCall_uuid(), System.currentTimeMillis() - tmsDialplan.getCreateLife(), cRCallback);
        log.debug("********* Progresive Dialer AMD detectedAsHuman Progresive cRCallBack [{}]", tmsDialplan.getCall_uuid());
        if (cRCallback.isConnectNow()) {
            CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
            callDetailRecord.setAmdExtTransferTo(cRCallback.getDialerInfoPojo().getAgentExt());
            callDetailRecord.setAmdConnectToAgentNow(Boolean.TRUE);
            callDetailRecord.setAmdTransferToAgentTime(LocalDateTime.now());
            tmsDialplan.addAction(new TMSOrder(DDD.SEND_TO_AGENT));
            tmsDialplan.addBridge(new BridgeToAgent(FreeswitchContext.dq_dp, agenService.getAgent(cRCallback.getDialerInfoPojo().getAgentExt()).getFreeswitchIP(), cRCallback.getDialerInfoPojo().getAgentExt()));

            callDetailRecordService.saveCDR(callDetailRecord);
        } else {
            tmsDialplan.addAction(new TMSOrder(DDD.PLACE_CALL_IN_FIFO));
            tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.dq_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
        }
        log.debug("********* Progresive Dialer AMD detectedAsHuman Progresive end. {}", tmsDialplan.getCall_uuid());
        return tmsDialplan;
    }

    public TMSDialplan detectedAsMachine(DialplanVariable variable, TMSDialplan tmsDialplan) {

        log.info("********* Progresive Dialer AMD detectedAsMachine Progresive");
        log.info("Progresive Dialer AMD detectedAsMachine Progresive: {} {} {} {} {}", tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber(), tmsDialplan.getBorrowerInfo().getBorrowerFirstName(), tmsDialplan.getBorrowerInfo().getLoanId(), "", tmsDialplan.getTms_type());
        log.info("Progresive Dialer AMD detectedAsMachine Progresive: {} {} {} {} {}", tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber(), tmsDialplan.getBorrowerInfo().getBorrowerFirstName(), tmsDialplan.getBorrowerInfo().getLoanId(), "", tmsDialplan.getTms_type());
        log.info("Progresive Dialer AMD detectedAsMachine Progresive: {} {} {} {} {}", tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber(), tmsDialplan.getBorrowerInfo().getBorrowerFirstName(), tmsDialplan.getBorrowerInfo().getLoanId(), "", tmsDialplan.getTms_type());
        log.info("Progresive Dialer AMD detectedAsMachine Progresive: {} {} {} {} {}", tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber(), tmsDialplan.getBorrowerInfo().getBorrowerFirstName(), tmsDialplan.getBorrowerInfo().getLoanId(), "", tmsDialplan.getTms_type());
        log.info("Progresive Dialer AMD detectedAsMachine Progresive: {} {} {} {} {}", tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber(), tmsDialplan.getBorrowerInfo().getBorrowerFirstName(), tmsDialplan.getBorrowerInfo().getLoanId(), "", tmsDialplan.getTms_type());
        log.info("********* Progresive Dialer AMD detectedAsMachine Progresive");

        OutboundDialerQueueSettings queueSettings = (OutboundDialerQueueSettings) dialerQueueRecordService.getQueueSettings(tmsDialplan.getDialerQueueId());

        CallDispositionCode callDispositionCode = dispositionCodeService.answeringMachineCode();
        boolean canLeaveMsg = false;

        LeaveVoiceMailAtOptions leaveVoiceMailAtOptions = queueSettings.getLeaveVoiceMailAt();
        if (leaveVoiceMailAtOptions == null) {
            leaveVoiceMailAtOptions = LeaveVoiceMailAtOptions.FIRST;
        }

        if (leaveVoiceMailAtOptions.name().equalsIgnoreCase(tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumberType())) {
            canLeaveMsg = true;
        }
        if (leaveVoiceMailAtOptions == LeaveVoiceMailAtOptions.FIRST) {
            canLeaveMsg = true;
        }

        VoiceMailOption voiceMailOption = queueSettings.getVoiceMailOption();
        if (voiceMailOption == null) {
            voiceMailOption = VoiceMailOption.NONE;
        }

        if (voiceMailOption == VoiceMailOption.AUTO && canLeaveMsg) {
            callDispositionCode = dispositionCodeService.answeringMachineDialerLeftMessageCode();
        }
        callingOutService.callEndedAsync(tmsDialplan.getCall_uuid(), callDispositionCode, tmsDialplan.getDialerQueueId());
        CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());

        log.info("********* Progresive Dialer AMD detectedAsMachine DispositionCode: {} - {}", queueSettings.getLeaveVoiceMailAt().name(), tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumberType());
        log.info("********* Progresive Dialer AMD detectedAsMachine DispositionCode: {} - {}", callDispositionCode.getDispositionId(), callDispositionCode.getDisposition());
        log.info("********* Progresive Dialer AMD detectedAsMachine VoiceMailOption: {}", queueSettings.getVoiceMailOption().name());
//        tmsDialplan.setCallResponseCode();

        tmsDialplan.setSystemDispostionCode(callDispositionCode.getDispositionId());
        callDetailRecord.setSystemDispostionCode(callDispositionCode.getDispositionId());

        if (queueSettings.getVoiceMailOption() == VoiceMailOption.AUTO && canLeaveMsg) {
            tmsDialplan.addAction(new Answer());
            tmsDialplan.addAction(new Sleep(250l));
            for (int i = 0; i < configuration.getAMDWaitForSilenceCount(); i++) {
                tmsDialplan.addAction(new WaitForSilence(configuration.getAMDWaitForSilence()));
                tmsDialplan.addAction(new Sleep(configuration.getAMDWaitForSilenceSleep()));
            }
            tmsDialplan.addAction(new Playback(configuration.getRecordingFile(queueSettings.getVoiceMailName())));

            tmsDialplan.addAction(new Sleep(500l));
            tmsDialplan.addBridge(new Hangup("NORMAL_CLEARING"));
        } else if (queueSettings.getVoiceMailOption() == VoiceMailOption.MANUAL) {
            tmsDialplan.addAction(new Sleep(1000l));
            tmsDialplan.addBridge(new Hangup("NORMAL_CLEARING"));
        } else {
            tmsDialplan.addAction(new Sleep(1000l));
            tmsDialplan.addBridge(new Hangup("NORMAL_CLEARING"));
        }
        callDetailRecordService.saveCDR(callDetailRecord);
        tmsDialplan.setXMLFromDialplan();
        return tmsDialplan;
    }

    public TMSDialplan sendToAgent(DialplanVariable variable, TMSDialplan tmsDialplan) {
        CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
        callDetailRecord.setAmdTransferToAgentTime(LocalDateTime.now());

        //tmsDialplan.addAction(new Export("beep_api_result_amdt=${sched_api(+2 ${uuid}_amdt uuid_broadcast ${uuid} playback::tone_stream://%("+configuration.getAMDPlayBeepToAgentDuration()+",50,"+configuration.getAMDPlayBeepToAgentHZ()+") bleg)}"));
        //tmsDialplan.addAction(new Export("nolocal:api_on_answer=uuid_broadcast ${uuid} playback::tone_stream://%("+configuration.getAMDPlayBeepToAgentDuration()+",50,"+configuration.getAMDPlayBeepToAgentHZ()+") bleg"));
        tmsDialplan.addAction(new Export("nolocal:execute_on_media=displace_session tone_stream://%(" + configuration.getAMDPlayBeepToAgentDuration() + ",50," + configuration.getAMDPlayBeepToAgentHZ() + ");loops=" + configuration.getAMDPlayBeepToAgentLast()));

//        tmsDialplan.addAction(Set.create("beep_api_result","${sched_api(+1 ${uuid}_amdt uuid_broadcast ${uuid} playback::tone_stream://%(200,100,1392) bleg)}"));
        // tmsDialplan.addAction(new Sleep(5000l));
        tmsDialplan.addAction(Set.create(FreeswitchVariables.hangup_after_bridge, Boolean.TRUE));
        tmsDialplan.addAction(Set.create(FreeswitchVariables.call_timeout, 20));
        tmsDialplan.setMaxDelayBeforeAgentAnswer(20);
        tmsDialplan.addAction(Set.create(FreeswitchVariables.continue_on_fail, Boolean.TRUE));

        tmsDialplan.addAction(new BridgeToSofiaContact(callDetailRecord.getAmdExtTransferTo(), agenService.getAgent(callDetailRecord.getAmdExtTransferTo()).getFreeswitchDomain()));

        tmsDialplan.addAction(new TMSOrder(DDD.PLACE_CALL_IN_FIFO));
        tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.agent_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
        callDetailRecordService.saveCDR(callDetailRecord);
        return tmsDialplan;
    }

    public TMSDialplan connectToAgent(DialplanVariable variable, TMSDialplan tmsDialplan) {
        CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
        callDetailRecord.setAmdTransferToAgentTime(LocalDateTime.now());

        callDetailRecordService.saveCDR(callDetailRecord);
        return tmsDialplan;
    }

    public TMSDialplan placeCallInFifo(DialplanVariable variable, TMSDialplan tmsDialplan) {
        CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
        callDetailRecord.setAmdStartFifoTime(LocalDateTime.now());
        callDetailRecord.setAmdConnectToAgentNow(Boolean.FALSE);

        OutboundDialerQueueSettings queueSettings = (OutboundDialerQueueSettings) dialerQueueRecordService.getQueueSettings(tmsDialplan.getDialerQueueId());

        log.info(tmsDialplan.getCallDirection() + " putting call on wait: " + tmsDialplan.getCall_uuid());
        log.info(tmsDialplan.getCallDirection() + " putting call on wait: " + tmsDialplan.getCallee());
        log.info(tmsDialplan.getCallDirection() + " putting call on wait: " + variable.getTmsUUID() + " -+> " + tmsDialplan.getKey().getTms_uuid());
        log.info(tmsDialplan.getCallDirection() + " putting call on wait: " + variable.getTmsUUID() + " -+> " + tmsDialplan.getCalleeLong());

//        if (tmsDialplan.getCounter() <= 1) {
//            log.info(tmsDialplan.getCallDirection() + " callRespondedAsync: " + variable.getTmsUUID());
//            callingOutService.callRespondedAsync(tmsDialplan.getCall_uuid(), System.currentTimeMillis() - tmsDialplan.getCreateLife(), callService);
//
//        } else {
        //dialerService.callResponded(tmsDialplan.getCalleeLong(), System.currentTimeMillis() - tmsDialplan.getCreateLife());
        log.info(tmsDialplan.getCallDirection() + "x2 putCallOnWait: " + variable.getTmsUUID());
        PhoneToType type = new PhoneToType();
        type.setFirstName(tmsDialplan.getBorrowerInfo().getBorrowerFirstName());
        type.setLastName(tmsDialplan.getBorrowerInfo().getBorrowerLastName());
        type.setPhoneNumber(tmsDialplan.getCalleeLong());
        type.setPhoneType(tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumberType());

        if (queueSettings == null || queueSettings.getHoldMusicName() == null) {
            tmsDialplan.addAction(new Set("fifo_music", configuration.getFiFoForDialer()));
        } else {
            tmsDialplan.addAction(new Set("fifo_music", configuration.getRecordingFile(queueSettings.getHoldMusicName())));
        }
        //}
        //tmsDialplan.addAction(new Set("fifo_music", configuration.getFiFoHoldMusic()));
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addBridge(new Fifo("OutboundDialerQueue_" + tmsDialplan.getDialerQueueId() + " in"));
        callDetailRecordService.saveCDR(callDetailRecord);
        
        callingOutService.putCallOnWaitForDialerAsync(tmsDialplan.getDialerQueueId(), tmsDialplan.getKey().getTms_uuid(), tmsDialplan.getBorrowerInfo().getLoanId(), type, 50);
        return tmsDialplan;
    }

    public TMSDialplan callExitingFifo(DialplanVariable variable, TMSDialplan tmsDialplan) {
        CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
        callDetailRecord.setAmdEndFifoTime(LocalDateTime.now());
//        tmsDialplan.addAction(Set.create("bypass_media", Boolean.TRUE));
//        tmsDialplan.addAction(new Set("fifo_bridge_uuid=" + tmsDialplan.getUniqueID()));
//        //tmsDialplan.addAction(new Set("fifo_music=$${hold_music}"));
//        tmsDialplan.addAction(new Answer());
//        tmsDialplan.addBridge(new Fifo("InboundDialerQueue_" + tmsDialplan.getDialerQueueId() + " out nowait"));
        callDetailRecordService.saveCDR(callDetailRecord);
        return tmsDialplan;
    }

    public TMSDialplan callInProgress(DialplanVariable variable, TMSDialplan tmsDialplan) {
        dialerService.callInProgress(tmsDialplan.getCall_uuid(), tmsDialplan.getCalleeLong());
        return tmsDialplan;
    }

    private class CRCallback implements CallRespondedCallback, Runnable {

        private long queuePk;
        private String callUUID;
        private Long loanId;
        private PhoneToType phoneToTypes;
        private DialerInfoPojo dialerInfoPojo;

        private boolean connectNow = true;

        @Override
        public boolean connectOutboundCallToAgent(int ext, String CallUUID, OutboundDialerQueueSettings settings, Long loanId, PhoneToType phoneToTypes) {
            dialerInfoPojo = new DialerInfoPojo();
            dialerInfoPojo.setBorrowerFirstName(phoneToTypes.getFirstName());
            dialerInfoPojo.setBorrowerLastName(phoneToTypes.getLastName());
            dialerInfoPojo.setLoanId(loanId);
            dialerInfoPojo.setSettings(settings);
            dialerInfoPojo.addPhoneToTypeSingle(phoneToTypes);
            dialerInfoPojo.setDialerMode(settings.getDialerMode());
            dialerInfoPojo.setAgentExt(ext);
            dialerInfoPojo.setCallUUID(CallUUID);
            BorrowerInfo info = new BorrowerInfo();
            info.setBorrowerFirstName(phoneToTypes.getFirstName());
            info.setBorrowerLastName(phoneToTypes.getLastName());
            info.setBorrowerPhoneNumber(Long.toString(phoneToTypes.getPhoneNumber()));
            info.setLoanId(loanId);
            return agentCallService.callStarted(ext, CallUUID, null, false, info, CallDirection.OUTBOUND, settings.getDialerQueuePk(), null, true, configuration.getCallWaitTimeoutBeforeConnect(null));
        }

        @Override
        public void putCallOnWait(long queuePk, String callUUID, Long loanId, PhoneToType phoneToTypes) {
            this.queuePk = queuePk;
            this.callUUID = callUUID;
            this.loanId = loanId;
            this.phoneToTypes = phoneToTypes;
            executor.execute(this);
            connectNow = false;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(75l);
            } catch (InterruptedException ex) {
            }
            callService.putCallOnWait(queuePk, callUUID, loanId, phoneToTypes);
        }

        public boolean isConnectNow() {
            return connectNow;
        }

        public DialerInfoPojo getDialerInfoPojo() {
            return dialerInfoPojo;
        }

    }

}
