/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.scheduler.annotation.QuartzJob;
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.sti.pojo.TMSBasicAccountInfo;
import com.objectbrains.sti.pojo.TMSCallDetails;
import com.objectbrains.sti.service.tms.TMSService;
import com.objectbrains.tms.db.entity.cdr.CallDetailRecord;
import com.objectbrains.tms.db.entity.cdr.SpeechToText;
import com.objectbrains.tms.db.entity.freeswitch.CDR;
import com.objectbrains.tms.db.repository.CallDetailRecordRepository;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.PhoneStatus;
import com.objectbrains.tms.enumerated.refrence.DDD;
import com.objectbrains.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.objectbrains.tms.hazelcast.AbstractEntryProcessor;
import com.objectbrains.tms.hazelcast.Configs;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.pojo.BorrowerInfo;
import com.objectbrains.tms.pojo.UploadCallRecordingPOJO;
import com.objectbrains.tms.service.freeswitch.CallingOutService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.joda.time.LocalDateTime;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class CallDetailRecordService {

    private static final Logger LOG = LoggerFactory.getLogger(CallDetailRecordService.class);

    @Autowired
    private CallDetailRecordRepository cdrRepository;

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    @Lazy
    private AgentCallService agentCallService;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private TMSService tmsIWS;

    @Autowired
    private CallingOutService callingOutService;

    private IMap<String, CallDetailRecord> recordMap;

    private IQueue<UploadCallRecordingPOJO> recordingUploadQueue;

    private IMap<String, Long> callTimeoutOnDialer;
    private IMap<String, Long> cdrSendtoSVC;
    private IMap<String, Boolean> ivrAuthorizedMap;

    @PostConstruct
    public void init() {
        recordMap = hazelcastService.getMap(Configs.CALL_DETAIL_RECORD_MAP);
        recordingUploadQueue = hazelcastService.getQueue(Configs.CALL_RECORDING_UPLOAD_QUEUE);
        callTimeoutOnDialer = hazelcastService.getMap(Configs.Call_TIMEOUT_ON_DIALER);
        cdrSendtoSVC = hazelcastService.getMap(Configs.CDR_TIMEOUT_TO_SVC);
        ivrAuthorizedMap = hazelcastService.getMap(Configs.IVR_AUTHORIZED_MAP);
    }

    @Bean
    public static Trigger recordingUploadTMSTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity("recordingUploadTMSTrigger")
                .forJob("recordingUploadTMS")
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever()
                        .withMisfireHandlingInstructionNextWithRemainingCount())
                .startNow()
                .build();
    }

    @QuartzJob(name = "recordingUploadTMS", disallowConcurrentExecution = true)
    public void uploadRecordingScheduler() {

        ArrayList<UploadCallRecordingPOJO> entries = new ArrayList<>();
        recordingUploadQueue.drainTo(entries);
        LOG.debug("Upload Recording Scheduler {}", entries.size());
        for (int i = 0; i < entries.size(); i++) {
            try {
                UploadCallRecordingPOJO get = entries.get(i);
                CallDetailRecord mcdr = getCDR(get.getCallUUID());
                if (mcdr != null && mcdr.isAnswered() && get.getDuration() > configuration.getCallDurationForSpeechToTextLimit() && configuration.enableSpeechToTextTranslation()) {
                    updateSspeechToTextRequested(mcdr.getCall_uuid(), Boolean.TRUE);
                    callingOutService.InvokRecordingUploadAndTranslate(get.getCallUUID(), get.getIp(), get.getData());
                } else {
                    callingOutService.InvokRecordingUpload(get.getCallUUID(), get.getIp(), get.getData());
                }
            } catch (Exception ex) {
                LOG.error("Exception {}", ex);
            }
        }
    }

    public void uploadRecording(String callUUID, String ip, String data, Long duration) {
        LOG.info("Upload Recording {} - {} - {}", callUUID, ip, data);
        if (configuration.getUploadRecordingQueueMax() > 0) {
            LOG.info("Upload Recording {} - {} - {}", callUUID, ip, data);
            if (configuration.getUploadRecordingChangeToDefaultIP()) {
                ip = configuration.getUploadRecordingDefaultIP();
            }
            LOG.info("Upload Recording {} - {} - {}", callUUID, ip, data);
            recordingUploadQueue.offer(new UploadCallRecordingPOJO(callUUID, ip, data, duration));
        }
    }

    @Async
    public void checkNumberToCallForDialerAcync(String number, CallDirection callDirection) {
        Long phonenumber = null;
        try {
            phonenumber = Long.parseLong(number);
        } catch (Exception ex) {
            LOG.error("Could not convert Phone number to Long {}", number);
            return;
        }

        if (configuration.getcallTimeoutForDialerCallDirection(callDirection)) {
            checkNumberToCallForDialer(normalizeDnc(phonenumber));
        }

    }

    private static String normalizeDnc(Long phoneNumber) {
        if (phoneNumber < 1_000_000_0000l) {
            phoneNumber += 1_000_000_0000l;
        }
        return phoneNumber.toString();
    }

    public boolean checkNumberToCallForDialer(String phonenumber) {
        boolean returnValue = true;
        boolean hasKey = false;

        Long storedTime = System.currentTimeMillis();

        Long value = null;
        Long timeout = null;
        if (callTimeoutOnDialer.containsKey(phonenumber)) {
            hasKey = true;

            storedTime = callTimeoutOnDialer.get(phonenumber);

            value = System.currentTimeMillis() - storedTime;

            timeout = configuration.getcallTimeoutForDialerInMillSec();
            if (value < timeout) {
                returnValue = false;
            }
        }
        if (returnValue && hasKey == false) {
            callTimeoutOnDialer.put(phonenumber, storedTime);
        }
        LOG.info("Checking phone number for dialer timeout Phone: {}, ReturnValue: {}, ttl: {}, timeout: {}", phonenumber, returnValue, value, timeout);
        return returnValue;
    }

    public void dumpNumberToCallForDialerTimeout() {
        LOG.info("Call For Dialer Timeout Entry......");
        for (Map.Entry<String, Long> entrySet : callTimeoutOnDialer.entrySet()) {
            String key = entrySet.getKey();
            Long value = entrySet.getValue();
            Long now = System.currentTimeMillis();
            LOG.info("Call For Dialer Timeout Entry: {}, entry: {}, ttl: {}", key, value, (now - value));
        }
    }

    public void removeAllNumberToCallForDialerTimeout() {
        LOG.info("Call For Dialer Timeout Entry Evirct......");
        callTimeoutOnDialer.evictAll();

    }

    public long getNumberToCallForDialerTimeout(String phonenumber) {
        Long value = callTimeoutOnDialer.get(phonenumber);
        if (value == null) {
            return 0;
        }
        return System.currentTimeMillis() - value;
    }

    private boolean badLanguage(String text) {
        return text.contains("**");
    }

    public void addSpeechToText(Integer ext, com.objectbrains.tms.websocket.message.inbound.SpeechToText speechToText) {
        SpeechToText toText = new SpeechToText();
        toText.setCall_uuid(speechToText.getCall_uuid());
        toText.setText(speechToText.getText());
        toText.setConfidence(speechToText.getConfidence());
        toText.setTimestamp(LocalDateTime.now());

        cdrRepository.persist(toText);
        if (badLanguage(speechToText.getText())) {
            LOG.info("Bad Language " + ext + " = " + speechToText.getText());
            agentCallService.setBadLanguage(ext, speechToText.getText());
        }
    }

    public CallDetailRecord findCDR(String call_uuid) {
        return recordMap.get(call_uuid);
    }

    public CallDetailRecord getCDR(String call_uuid) {
        return recordMap.get(call_uuid);
//        CallDetailRecord newRecord = new CallDetailRecord(call_uuid);
//        recordMap.putIfAbsent(call_uuid, newRecord);
//
//        CallDetailRecord callDetailRecord = recordMap.get(call_uuid);
//
//        if (callDetailRecord == null) {
//            return newRecord;
//        }
//        return callDetailRecord;
    }

    public void saveCDR(CallDetailRecord cdr) {
//        try {
//            throw new Exception("Show me the stack {} " + cdr.getCall_uuid());
//        } catch (Exception ex) {
//            LOG.error("What the fuck: ", ex);
//        }
        LOG.info("Saving saveCDR {} -> pk is {} isAgentHangup: {}", cdr.getCall_uuid(), cdr.getPk(), cdr.isAgentHangup());
        recordMap.put(cdr.getCall_uuid(), cdr);
    }

    @Async
    public void saveInitialMasterRecordIntoSVC(com.objectbrains.sti.db.entity.base.dialer.CallDetailRecord callDetailRecordMaster) {
//        tmsIWS.createOrUpdateCallDetailRecord(callDetailRecordMaster);
    }

    public void saveSBCComplet(String callUUID, Boolean hangupCause, Boolean bridgeHangupCause, CallDispositionCode systemDispositionCode) {
        if (callUUID != null) {
            recordMap.executeOnKey(callUUID, new UpdateCompleteEntryProcessor(bridgeHangupCause, hangupCause, systemDispositionCode));
        }
    }

    public void updateCallState(String callUUID, PhoneStatus phoneStatus, Long userCallDisposition) {
        if (callUUID != null) {
            if (phoneStatus != null) {
                recordMap.executeOnKey(callUUID, new UpdateCallStateEntryProcessor(phoneStatus, userCallDisposition));
            }
        }
    }

    public void updateInboundLeftVoicemail(String callUUID, Boolean inboundLeftVoicemail) {
        if (callUUID != null) {
            LOG.info("Saved Voicemail: {} - {}", callUUID, inboundLeftVoicemail);
            recordMap.executeOnKey(callUUID, new UpdateVoicemailEntryProcessor(inboundLeftVoicemail));
        }
    }

    public CallDetailRecord updateCallDetailRecord(CDR cdr) {
        if (cdr.getCall_uuid() == null) {
            return null;
        }
        return (CallDetailRecord) recordMap.executeOnKey(cdr.getCall_uuid(), new UpdateCDREntryProcessor(cdr, configuration.getLoadBalancerHostname()));
    }

    public void updateBorrowerInfo(String callUUID, BorrowerInfo borrowerInfo) {
        recordMap.executeOnKey(callUUID, new UpdateBorrowerInfoEntryProcessor(borrowerInfo));
        if (borrowerInfo != null) {
            LOG.info("Saved borrower info: {} - {}", callUUID, borrowerInfo.getLoanId());
        } else {
            LOG.info("Saved borrower info: {} - {null}", callUUID);
        }
    }

    public void updateIVRSSN(String callUUID, String ssn) {
        recordMap.executeOnKey(callUUID, new UpdateIVRSSNEntryProcessor(ssn));
        LOG.info("Saved ssn entry info: {} - {}", callUUID, ssn);
    }

    public void updateIVRZip(String callUUID, String zip) {
        recordMap.executeOnKey(callUUID, new UpdateIVRZipNEntryProcessor(zip));
        LOG.info("Saved zip entry info: {} - {}", callUUID, zip);
    }

    public void updateOptionText(String callUUID, String optionText) {
        recordMap.executeOnKey(callUUID, new UpdateOptionTextEntryProcessor(optionText));
        LOG.info("Saved optionText entry info: {} - {}", callUUID, optionText);
    }

    public void updateSpeechToTextCompleted(String callUUID, Boolean speechToTextCompleted, Double confidence, Double confidenceRight, Double confidenceLeft) {
        recordMap.executeOnKey(callUUID, new UpdateSpeechToTextCompletedEntryProcessor(speechToTextCompleted, confidence, confidenceRight, confidenceLeft));
        LOG.info("Saved speechToTextCompleted entry info: {} - {}", callUUID, speechToTextCompleted);
    }

    public void updateSpeechToTextError(String callUUID, Boolean speechToTextError) {
        recordMap.executeOnKey(callUUID, new UpdateSpeechToTextErrorEntryProcessor(speechToTextError));
        LOG.info("Saved speechToTextError entry info: {} - {}", callUUID, speechToTextError);
    }

    public void updateSspeechToTextRequested(String callUUID, Boolean speechToTextRequested) {
        recordMap.executeOnKey(callUUID, new UpdateSpeechToTextRequestedEntryProcessor(speechToTextRequested));
        LOG.info("Saved speechToTextRequested entry info: {} - {}", callUUID, speechToTextRequested);
    }

    public Boolean getIVRAuthorized(String callUUID) {
        if (ivrAuthorizedMap.containsKey(callUUID)) {
            return ivrAuthorizedMap.get(callUUID);
        }
        return Boolean.FALSE;
    }

    public void updateIVRAuthorized(String callUUID, Boolean ivrAuthorized) {
        ivrAuthorizedMap.put(callUUID, ivrAuthorized);
        recordMap.executeOnKey(callUUID, new UpdateIVRAuthorizedEntryProcessor(ivrAuthorized));
        LOG.info("Saved ivrAuthorized entry info: {} - {}", callUUID, ivrAuthorized);
    }

    public void updateLoanId(String callUUID, Long loanId) {
        recordMap.executeOnKey(callUUID, new UpdateLoanIdEntryProcessor(loanId));
        LOG.info("Saved loanId entry info: {} - {}", callUUID, loanId);
    }

    public void updateQueueId(String callUUID, Long QueueId) {
        recordMap.executeOnKey(callUUID, new UpdateQueueIdEntryProcessor(QueueId));
        LOG.info("Saved QueueId entry info: {} - {}", callUUID, QueueId);
    }

    public void updateADO(String callUUID, AgentIncomingDistributionOrder ado, Long dialerQueuePk) {
        recordMap.executeOnKey(callUUID, new UpdateADOEntryProcessor(ado, dialerQueuePk));
        LOG.info("Saved ADO entry info: {} - {} - dialer queue pk {}", callUUID, ado, dialerQueuePk);
    }

    public void updateLastTransferStep(String callUUID, Integer lastTransferStep) {
        recordMap.executeOnKey(callUUID, new UpdateLastTransferStepEntryProcessor(lastTransferStep));
        LOG.info("Saved lastTransferStep entry info: {} - {}", callUUID, lastTransferStep);
    }

    public void updateInboundDIDNumber(String callUUID, String inboundDIDNumber) {
        recordMap.executeOnKey(callUUID, new UpdateInboundDIDNumberEntryProcessor(inboundDIDNumber));
        LOG.info("Saved inbound DID Number entry info: {} - {}", callUUID, inboundDIDNumber);
    }

    public void updateIsOpen(String callUUID, Boolean isOpen) {
        recordMap.executeOnKey(callUUID, new UpdateWorkingHourEntryProcessor(isOpen));
        LOG.info("Saved inbound is open entry info: {} - {}", callUUID, isOpen);
    }

    public static class UpdateWorkingHourEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private Boolean isOpen;

        public UpdateWorkingHourEntryProcessor(Boolean isOpen) {
            this.isOpen = isOpen;
        }

        public UpdateWorkingHourEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setIsOpen(isOpen);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(isOpen);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            isOpen = in.readObject();

        }

    }

//    public static class GetCDREntryProcessor extends AbstractEntryProcessor
    public static class UpdateInboundDIDNumberEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private String inboundDIDNumber;

        public UpdateInboundDIDNumberEntryProcessor(String inboundDIDNumber) {
            this.inboundDIDNumber = inboundDIDNumber;
        }

        public UpdateInboundDIDNumberEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setInboundDIDNumber(inboundDIDNumber);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(inboundDIDNumber);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            inboundDIDNumber = in.readObject();

        }

    }

    public static class UpdateOptionTextEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private String optionText;

        public UpdateOptionTextEntryProcessor(String optionText) {
            this.optionText = optionText;
        }

        public UpdateOptionTextEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setOptionText(optionText);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(optionText);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            optionText = in.readObject();

        }

    }

    public static class UpdateIVRAuthorizedEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private Boolean ivrAuthorized;

        public UpdateIVRAuthorizedEntryProcessor(Boolean ivrAuthorized) {
            this.ivrAuthorized = ivrAuthorized;
        }

        public UpdateIVRAuthorizedEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setIvrAuthorized(ivrAuthorized);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(ivrAuthorized);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            ivrAuthorized = in.readObject();
        }

    }

    public static class UpdateLastTransferStepEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private Integer lastTransferStep;

        public UpdateLastTransferStepEntryProcessor(Integer lastTransferStep) {
            this.lastTransferStep = lastTransferStep;
        }

        public UpdateLastTransferStepEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setLastTrasferStep(lastTransferStep);
            record.setLastTrasferStepTimestamp(LocalDateTime.now());

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(lastTransferStep);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            lastTransferStep = in.readObject();

        }

    }

    public static class UpdateADOEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private AgentIncomingDistributionOrder ado;
        private Long dialerQueuePk; 

        public UpdateADOEntryProcessor(AgentIncomingDistributionOrder ado, Long dialerQueuePk) {
            this.ado = ado;
            this.dialerQueuePk = dialerQueuePk;
        }

        public UpdateADOEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setAdo(ado.toJson());

            if (Objects.equals(ado.getCallDetails().getDialerQueuePk(), dialerQueuePk)) {
                if (ado.getAgents() != null) {
                    record.setAdoAgentAvialable(ado.getAgents().size());
                }
                record.setMultiLine(ado.getMultiLine());
                if (ado.getIncomingCallOrderSelected() != null) {
                    record.setIncomingCallOrderSelected(ado.getIncomingCallOrderSelected());
                }
                record.setDialerQueueName(ado.getDialerQueueName());
                if (record.getLogCallOrder() == null || record.getLogCallOrder().equals("")) {
                    record.setLogCallOrder(ado.getLogOfCallOrder());
                } else {
                    String old = record.getLogCallOrder() + ado.getLogOfCallOrder();
                    record.setLogCallOrder(old);
                }
            }
            record.setAdoUpdateDateTime(LocalDateTime.now());
            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(ado);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            ado = in.readObject();

        }

    }

    public static class UpdateSpeechToTextRequestedEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private Boolean speechToTextRequested;

        public UpdateSpeechToTextRequestedEntryProcessor(Boolean speechToTextRequested) {
            this.speechToTextRequested = speechToTextRequested;
        }

        public UpdateSpeechToTextRequestedEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setSpeechToTextRequested(speechToTextRequested);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(speechToTextRequested);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            speechToTextRequested = in.readObject();

        }
    }

    public static class UpdateSpeechToTextCompletedEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private Boolean speechToTextCompleted;
        private Double confidence;
        private Double confidenceRight;
        private Double confidenceLeft;

        public UpdateSpeechToTextCompletedEntryProcessor(Boolean speechToTextCompleted, Double confidence, Double confidenceRight, Double confidenceLeft) {
            this.speechToTextCompleted = speechToTextCompleted;
            this.confidence = confidence;
            this.confidenceLeft = confidenceLeft;
            this.confidenceRight = confidenceRight;
        }

        public UpdateSpeechToTextCompletedEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setSpeechToTextCompleted(speechToTextCompleted);
            record.setSpeechToTextConfidence(confidence);
            record.setSpeechToTextConfidenceRight(confidenceRight);
            record.setSpeechToTextConfidenceLeft(confidenceLeft);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(speechToTextCompleted);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            speechToTextCompleted = in.readObject();

        }
    }

    public static class UpdateSpeechToTextErrorEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private Boolean speechToTextError;

        public UpdateSpeechToTextErrorEntryProcessor(Boolean speechToTextError) {
            this.speechToTextError = speechToTextError;
        }

        public UpdateSpeechToTextErrorEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setSpeechToTextError(speechToTextError);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(speechToTextError);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            speechToTextError = in.readObject();

        }
    }

    public static class UpdateQueueIdEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private Long queueId;

        public UpdateQueueIdEntryProcessor(Long queueId) {
            this.queueId = queueId;
        }

        public UpdateQueueIdEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setDialerQueueId(queueId);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(queueId);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            queueId = in.readObject();

        }

    }

    public static class UpdateLoanIdEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private Long loanId;

        public UpdateLoanIdEntryProcessor(Long loanId) {
            this.loanId = loanId;
        }

        public UpdateLoanIdEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.getBorrowerInfo().setLoanId(loanId);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(loanId);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            loanId = in.readObject();

        }

    }

    public static class UpdateIVRZipNEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private String zip;

        public UpdateIVRZipNEntryProcessor(String zip) {
            this.zip = zip;
        }

        public UpdateIVRZipNEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setIvrZipCode(zip);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(zip);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            zip = in.readObject();

        }

    }

    public static class UpdateIVRSSNEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private String ssn;

        public UpdateIVRSSNEntryProcessor(String ssn) {
            this.ssn = ssn;
        }

        public UpdateIVRSSNEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setIvrSSN(ssn);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(ssn);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            ssn = in.readObject();

        }

    }

    public static class UpdateBorrowerInfoEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private BorrowerInfo borrowerInfo;

        public UpdateBorrowerInfoEntryProcessor(BorrowerInfo borrowerInfo) {
            this.borrowerInfo = borrowerInfo;
        }

        public UpdateBorrowerInfoEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setBorrowerInfo(borrowerInfo);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(borrowerInfo);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            borrowerInfo = in.readObject();

        }

    }

    public static class UpdateVoicemailEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> implements DataSerializable {

        private Boolean inboundLeftVoicemail = Boolean.FALSE;

        public UpdateVoicemailEntryProcessor(Boolean voicemailInput) {
            this.inboundLeftVoicemail = voicemailInput;
        }

        public UpdateVoicemailEntryProcessor() {
        }

        @Override
        public Object process(Map.Entry<String, CallDetailRecord> entry, boolean isPrimary) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            record.setInboundLeftVoicemail(inboundLeftVoicemail);

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(inboundLeftVoicemail);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            inboundLeftVoicemail = in.readObject();

        }

    }

    @SpringAware
    public static class UpdateCompleteEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> {

        private Boolean bridgeHangupCause;
        private Boolean hangupCause;
        private CallDispositionCode systemDispositionCode;

        @Autowired
        private CallingOutService callingOutService;

        @Autowired
        private DispositionCodeService dispositionCodeService;

        public UpdateCompleteEntryProcessor() {
        }

        public UpdateCompleteEntryProcessor(Boolean bridgeHangupCause, Boolean hangupCause, CallDispositionCode systemDispositionCode) {
            this.bridgeHangupCause = bridgeHangupCause;
            this.hangupCause = hangupCause;
            this.systemDispositionCode = systemDispositionCode;
        }

        @Override
        protected Void process(Map.Entry<String, CallDetailRecord> entry, boolean isMain) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }

            if (record.getSystemDispostionCode() == null) {
                record.setSystemDispostionCode(systemDispositionCode.getDispositionId());
            } else if (record.getSystemDispostionCode() != 7 && record.getSystemDispostionCode() != 168) {
                record.setSystemDispostionCode(systemDispositionCode.getDispositionId());
            }

            record.setBridgeHangupCauseBoolValue(bridgeHangupCause);
            record.setHangupCauseBoolValue(hangupCause);
            record.setComplete(Boolean.TRUE);

            if (isMain && record.getDialer() && record.getComplete() && record.isWrapped()) {
                callingOutService.callEndedAsync(record.getCall_uuid(), dispositionCodeService.getDispositionCodeFromId(record.getUserDispostionCode()), record.getDialerQueueId());
            }

            entry.setValue(record);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(bridgeHangupCause);
            out.writeObject(hangupCause);
            out.writeObject(systemDispositionCode);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            bridgeHangupCause = in.readObject();
            hangupCause = in.readObject();
            systemDispositionCode = in.readObject();
        }

    }

    @SpringAware
    public static class UpdateCallStateEntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> {

        @Autowired
        private CallingOutService callingOutService;

        @Autowired
        private DispositionCodeService dispositionCodeService;

        private PhoneStatus phoneStatus;
        private Long userCallDisposition;

        private UpdateCallStateEntryProcessor() {
        }

        public UpdateCallStateEntryProcessor(PhoneStatus phoneStatus, Long userCallDisposition) {
            this.phoneStatus = phoneStatus;
            this.userCallDisposition = userCallDisposition;
        }

        @Override
        protected Void process(Map.Entry<String, CallDetailRecord> entry, boolean isMain) {
            CallDetailRecord record = entry.getValue();
            if (record == null) {
                record = new CallDetailRecord(entry.getKey());
            }
            LOG.info("Processing UpdateCallStateEntryProcessor: {} for {}", phoneStatus, record.getCall_uuid());
            boolean modified = true;
            switch (phoneStatus) {
                case RINGING:
                    record.setRinged(true);
                    record.setRingedTime(LocalDateTime.now());
                    break;
                case ANSWER:
                    record.setAnswered(true);
                    record.setAnswerTime(LocalDateTime.now());
                    break;
                case TRANSFER:
                    record.incrementTransferCount();
                    break;
                case AGENT_HANGUP:
                    record.setAgentHangup(true);
                    record.setEndTimeFromWebsocket(LocalDateTime.now());
                    break;
                case HANGUP:
                    record.setCallerHangup(true);
                    record.setEndTimeFromWebsocket(LocalDateTime.now());
                    break;
                case WRAP:
                    record.setWrapped(true);
                    record.setWrapTime(LocalDateTime.now());
                    record.setUserDispostionCode(userCallDisposition);
                    if (isMain && record.getDialer() && record.getComplete() && record.isWrapped()) {
                        callingOutService.callEndedAsync(record.getCall_uuid(), dispositionCodeService.getDispositionCodeFromId(record.getUserDispostionCode()), record.getDialerQueueId());
                    }
                    break;
                default:
                    modified = false;
                //we don't update the value
            }
            if (modified) {
                entry.setValue(record);
            }
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(phoneStatus);
            out.writeObject(userCallDisposition);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            phoneStatus = in.readObject();
            userCallDisposition = in.readObject();
        }

    }

    @SpringAware
    public static class UpdateCDREntryProcessor extends AbstractEntryProcessor<String, CallDetailRecord> {

        private CDR cdr;
        private String hostname;

        @Autowired
        private TMSAgentService agentService;

        @Autowired
        private DispositionCodeService dispositionCodeService;

        @Autowired
        private SystemMonitorInfo systemMonitorInfo;

        @Autowired
        private AgentCallService agentCallService;

        @Autowired
        private TMSService tmsIWS;
//        
        @Autowired
        private CallingOutService callingOutService;

        @Autowired
        private CallDetailRecordService callDetailRecordService;

        private UpdateCDREntryProcessor() {
        }

        public UpdateCDREntryProcessor(CDR cdr, String hostname) {
            this.cdr = cdr;
            this.hostname = hostname;
        }

        @Override
        protected CallDetailRecord process(Map.Entry<String, CallDetailRecord> entry, boolean isMain) {
            CallDetailRecord mcdr = entry.getValue();
            if (mcdr == null) {
                LOG.warn("Building Master CDR: [Not FOUND] " + cdr.getCall_uuid() + " - " + cdr.getContext() + " - " + cdr.getOrderPower());
                return null;
            }
            LOG.info("Building Master CDR Process: " + mcdr.getCall_uuid() + " - " + cdr.getContext() + " - " + cdr.getOrderPower());
            mcdr.setCallDirection(cdr.getCallDirection());
            mcdr.setDialer(cdr.getDialer());
            if (mcdr.getDialer()) {
                mcdr.setDialerQueueId(cdr.getDialerQueueId());
            } else if (cdr.getDialerQueueId() != null) {
                mcdr.setDialerQueueId(cdr.getDialerQueueId());
            } else {
                if (mcdr.getDialerQueueId() == null && mcdr.getBorrowerInfo() != null && mcdr.getBorrowerInfo().getLoanId() != null) {
                    TMSCallDetails details = tmsIWS.getLoanInfoByLoanPk(mcdr.getBorrowerInfo().getLoanId());
                    if (details != null && details.getDialerQueuePk() != null) {
                        mcdr.setDialerQueueId(details.getDialerQueuePk());
                    }
                }
            }
            if (mcdr.getEnd_time() == null || cdr.getEndTime().isAfter(mcdr.getEnd_time())) {
                mcdr.setEnd_time(cdr.getEndTime());
            }
            if (mcdr.getStart_time() == null || cdr.getStartTime().isBefore(mcdr.getStart_time())) {
                mcdr.setStart_time(cdr.getStartTime());
            }

            if (cdr.getContext() == FreeswitchContext.sbc_dp) {

                String folder = FreeswitchConfiguration.formatToYYYY_MM_DD(cdr.getCreateTimestamp());
                mcdr.setCall_recording_url("https://" + hostname + "/tms/recordings/" + folder + "/" + cdr.getCall_uuid() + "/" + cdr.getCall_uuid() + ".wav");
                mcdr.setDuration(cdr.getDuration());

                if (cdr.getCallDirection() == CallDirection.INBOUND) {
                    mcdr.setAnswermsec(cdr.getAnswermsec());
                    mcdr.setWaitmsec(cdr.getWaitmsec());
                    mcdr.setCaller_id_number(cdr.getCaller_id_number());
                    if (mcdr.getBorrowerInfo().getLoanId() == null) {
                        mcdr.getBorrowerInfo().setLoanId(cdr.getLoanId());
                    }
                    if (mcdr.getDialer() == false) {
                        callDetailRecordService.checkNumberToCallForDialerAcync(cdr.getCaller_id_number(), cdr.getCallDirection());
                    }
                } else if (cdr.getCallDirection() == CallDirection.OUTBOUND) {
                    mcdr.setCallee_id_number(cdr.getCallee_id_number());
                    if (mcdr.getDialer() == false) {
                        callDetailRecordService.checkNumberToCallForDialerAcync(cdr.getCallee_id_number(), cdr.getCallDirection());
                    }
                    mcdr.setProgress_mediasec(cdr.getProgress_mediamsec());
                    mcdr.setProgresssec(cdr.getProgressmsec());
                }
                mcdr.setEffective_caller_id_number(cdr.getEffective_caller_id_number());
                int cause = 0;
                if (cdr.getHangup_cause_q850() == null) {
                    LOG.info("cdr hangup cause: {}", cause);
                } else {
                    cause = cdr.getHangup_cause_q850();
                }

                mcdr.setCompleteFinal(Boolean.TRUE);
            }
            if (cdr.getContext() == FreeswitchContext.dq_dp) {
                mcdr.setAmd_status(cdr.getAmd_status());
                mcdr.setAmd_result(cdr.getAmd_result());

                if (mcdr.getBorrowerInfo().getLoanId() == null) {
                    mcdr.getBorrowerInfo().setLoanId(cdr.getLoanId());
                }
                mcdr.getBorrowerInfo().setBorrowerFirstName(cdr.getBorrowerFirstName());
                mcdr.getBorrowerInfo().setBorrowerLastName(cdr.getBorrowerLastName());
                mcdr.getBorrowerInfo().setBorrowerPhoneNumber(cdr.getBorrowerPhone());

                if (cdr.getOrderPower().equals(DDD.START_AMD.name()) || cdr.getOrderPower().equals(DDD.WAIT_FOR_MEDIA.name()) || cdr.getOrderPower().equals(DDD.VERIFY_AMD.name())) {
                    mcdr.setAmdBeforeFifoDropped(Boolean.TRUE);
                }

            }
            if (cdr.getContext() == FreeswitchContext.fifo_dp) {
                if (mcdr.getDialer()) {
                    if (cdr.getOrderPower().equals(DDD.PLACE_CALL_IN_FIFO.name())) {
                        if (cdr.getBridge_hangup_cause() == null) {
                            mcdr.setAmdEndFifoTime(cdr.getEndTime());
                            mcdr.setAmdFifoDropped(Boolean.TRUE);
                        }
                    }
                }
            }
            if (cdr.getContext() == FreeswitchContext.ivr_dp) {
                if (cdr.getIvr_step() != null) {
                    if (mcdr.getLastIvrStep() == null || cdr.getIvr_step() > mcdr.getLastIvrStep()) {
                        mcdr.setLastIvrStep(cdr.getIvr_step());
                    }
                }
            }
            if (cdr.getContext() == FreeswitchContext.agent_dp) {
                mcdr.setAutoAswer(cdr.getAutoAswer());
                if (mcdr.getBorrowerInfo().getLoanId() == null) {
                    mcdr.getBorrowerInfo().setLoanId(cdr.getLoanId());
                }
                mcdr.getBorrowerInfo().setBorrowerFirstName(cdr.getBorrowerFirstName());
                mcdr.getBorrowerInfo().setBorrowerLastName(cdr.getBorrowerLastName());
                mcdr.getBorrowerInfo().setBorrowerPhoneNumber(cdr.getBorrowerPhone());

                try {
                    Agent agent = null;
                    agent = agentService.getAgent(mcdr.getLastAgent());
                    if (agent != null) {
                        mcdr.setUsername(agent.getUserName());
                    }
                } catch (Exception ex) {

                }

                boolean updateLastAgent = false;

                if (mcdr.getLastAgentTimestamp() == null || cdr.getEndTime().isAfter(mcdr.getLastAgentTimestamp())) {
                    updateLastAgent = true;
                    mcdr.setAgentGroupId(cdr.getAgentGroupId());
                }

                boolean updatefirstAgent = false;
                if (mcdr.getFirstAgentTimestamp() == null || cdr.getEndTime().isBefore(mcdr.getFirstAgentTimestamp())) {
                    updatefirstAgent = true;
                }

                if (mcdr.getDialer() && (cdr.getBridge_hangup_cause() == null || (cdr.getBridge_hangup_cause() != null && cdr.getBridge_hangup_cause().equalsIgnoreCase("NORMAL_CLEARING") == false))) {
                    mcdr.setAmdDroppedBeforeAgentAnswerTimestamp(cdr.getEndTime());
                    mcdr.setAmdDroppedBeforeAgentAnswer(true);
                }

                if (cdr.getCallDirection() == CallDirection.OUTBOUND) {
                    mcdr.setAnswermsec(cdr.getAnswermsec());
                    mcdr.setWaitmsec(cdr.getWaitmsec());

                    if (isMain) {
                        agentService.updateLastOutboundCallTimes(mcdr.getCaller_id_number());
                    }

                    mcdr.setCaller_id_number(cdr.getCaller_id_number());

                    if (mcdr.getDialer()) {// && mcdr.getCaller_id_number().startsWith("1")) {
                        mcdr.setCaller_id_number(cdr.getDestination_number());
                    } else if (cdr.getOrderPower().contains("AgentToAgentTransfer")) {// && mcdr.getCaller_id_number().startsWith("1")) {
                        mcdr.setCaller_id_number(cdr.getDestination_number());
                    }

                    if (updateLastAgent) {
                        mcdr.setLastAgentString(mcdr.getCaller_id_number());
                    }
                    if (updatefirstAgent) {
                        mcdr.setFirstAgentString(mcdr.getCaller_id_number());
                    }

                } else if (cdr.getCallDirection() == CallDirection.INBOUND) {
                    mcdr.setCallee_id_number(cdr.getCallee_id_number());
                    mcdr.setProgress_mediasec(cdr.getProgress_mediamsec());

                    if (isMain) {
                        agentService.updateLastInboundCallTimes(mcdr.getCaller_id_number());
                    }

                    mcdr.setProgresssec(cdr.getProgressmsec());

                    if (updateLastAgent) {
                        mcdr.setLastAgentString(mcdr.getCallee_id_number());
                    }
                    if (updatefirstAgent) {
                        if (cdr.getDestination_number_profile_min() != null) {
                            mcdr.setFirstAgentString(cdr.getDestination_number_profile_min());
                        } else {
                            mcdr.setFirstAgentString(mcdr.getCallee_id_number());
                        }
                    }
                } else if (cdr.getCallDirection() == CallDirection.INTERNAL) {
                    mcdr.setCallee_id_number(cdr.getCallee_id_number());
                    mcdr.setProgress_mediasec(cdr.getProgress_mediamsec());
                    mcdr.setProgresssec(cdr.getProgressmsec());
                    mcdr.setCaller_id_number(cdr.getCaller_id_number());
                    mcdr.setCallee_id_number(cdr.getDestination_number());

                    if (isMain && mcdr.getCaller_id_number() != null) {
                        Integer caller = Integer.parseInt(mcdr.getCaller_id_number());
                        callingOutService.callEndedForAgent(caller, mcdr.getCall_uuid(), null, mcdr.getDialerQueueId());
                    }

                    if (isMain && mcdr.getCallee_id_number() != null) {
                        Integer callee = Integer.parseInt(mcdr.getCallee_id_number());
                        callingOutService.callEndedForAgent(callee, mcdr.getCall_uuid(), null, mcdr.getDialerQueueId());
                    }

                }

                if (updateLastAgent && mcdr.getAmdExtTransferTo() != null && mcdr.getTransferCount() == 0) {
                    mcdr.setLastAgent(mcdr.getAmdExtTransferTo());
                }

                if (updatefirstAgent && mcdr.getAmdExtTransferTo() != null && mcdr.getTransferCount() == 0) {
                    mcdr.setFirstAgentString(mcdr.getCallee_id_number());
                }

                if (isMain && (mcdr.isCallerHangup() == mcdr.isAgentHangup() || mcdr.isAnswered() == false) && mcdr.getLastAgent() != null) {
                    //Never got to the agent. d
                    callingOutService.callEndedForAgent(mcdr.getLastAgent(), mcdr.getCall_uuid(), null, mcdr.getDialerQueueId());
                }

                if (isMain && mcdr.getDialer() && mcdr.getComplete() && mcdr.isWrapped()) {
                    callingOutService.callEndedAsync(mcdr.getCall_uuid(), dispositionCodeService.getDispositionCodeFromId(mcdr.getUserDispostionCode()), mcdr.getDialerQueueId());
                }

                if (mcdr.isAnswered() && mcdr.getInboundLeftVoicemail()) {
                    mcdr.setInboundLeftVoicemail(false);
                }
            }

            if (mcdr.getBorrowerInfo() != null && mcdr.getBorrowerInfo().getLoanId() != null && mcdr.getBorrowerInfo().getLoanId() != 0) {
                TMSBasicAccountInfo basicLoanInfo = tmsIWS.getBasicAccountInfoForTMS(mcdr.getBorrowerInfo().getLoanId());
                mcdr.setNextDueDate(basicLoanInfo.getNextDueDate());
            }

            entry.setValue(mcdr);
            return mcdr;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(cdr);
            out.writeUTF(hostname);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            cdr = in.readObject();
            hostname = in.readUTF();
        }

    }

}
