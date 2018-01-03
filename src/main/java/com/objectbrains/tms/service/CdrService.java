/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.hazelcast.core.IQueue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.scheduler.annotation.QuartzJob;
import com.objectbrains.svc.iws.SvCallDetailRecord;
import com.objectbrains.svc.iws.SvCollectionCallLog;
import com.objectbrains.svc.iws.TMSService;
import com.objectbrains.tms.db.entity.cdr.CallDetailRecord;
import com.objectbrains.tms.db.entity.freeswitch.CDR;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.db.repository.CdrRepository;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.hazelcast.Configs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class CdrService {

    private static final Logger LOG = LoggerFactory.getLogger(CdrService.class);

    @Autowired
    private DialplanService dialplanRepository;

    @Autowired
    private AgentService agentService;

    @Autowired
    private HazelcastService hazelcast;

    @Autowired
    private DispositionCodeService dispositionCodeService;

    @Autowired
    private CdrRepository cdrRepository;

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private FreeswitchConfiguration freeswitchConfiguration;

    @Autowired
    private FreeswitchService freeswitchService;

    @Autowired
    private TMSService tmsIWS;

    @ConfigContext
    private ConfigurationUtility config;

    private IQueue<SCdrEntry> svCollectionCallLogs;

    private IQueue<MCdrEntry> callDetailRecordQueue;

    @PostConstruct
    private void init() {
        svCollectionCallLogs = hazelcast.getQueue(Configs.SCDR_QUEUE);
        callDetailRecordQueue = hazelcast.getQueue(Configs.MCDR_QUEUE);
    }

    private long currentTimeMillis() {
        long l = hazelcast.getCluster().getClusterTime();
        LOG.debug("HZ ClusterTime {}", l);
        return l;
    }

    public void storeCDR(CDR cdr) {
        cdrRepository.persistCDR(cdr);
        try {
            updateCallDetailRecord(cdr);

            SvCollectionCallLog callLogSlave = new SvCollectionCallLog();

            callLogSlave.setCallDurationInMilliSec(cdr.getDuration());
            callLogSlave.setAnswerInMilliSec(cdr.getAnswermsec());
            callLogSlave.setWaitInMilliSec(cdr.getWaitmsec());
            callLogSlave.setContext(cdr.getContext().getSvcContext());

            callLogSlave.setStartTime(cdr.getStartTime());
            callLogSlave.setEndTime(cdr.getEndTime());
            callLogSlave.setAnswerTime(cdr.getAnswerTime());
            callLogSlave.setCallUUID(cdr.getCall_uuid());
            freeswitchService.releaseFreeswitchNode(cdr.getCall_uuid());
            callLogSlave.setAutoAnswer(cdr.getAutoAswer());
            callLogSlave.setDialer(cdr.getDialer());
            if (cdr.getDialer() != null) {
                if (cdr.getDialer()) {
                    callLogSlave.setDialerQueuePk(cdr.getDialerQueueId());
                }
            } else {
                callLogSlave.setDialer(false);
            }
            callLogSlave.setEffectiveCallerIdNumber(cdr.getEffective_caller_id_number());
            callLogSlave.setNetworkAddr(cdr.getNetwork_addr());
            callLogSlave.setDestinationNumber(cdr.getDestination_number());
            callLogSlave.setCallDirection(cdr.getCallDirection().getSvcCallDirection());
            if (cdr.getTms_uuid() != null) {
                TMSDialplan tmsDialplan = dialplanRepository.getTMSDialplanForCDR(cdr.getTms_uuid(), cdr.getContext(), cdr.getOrderPower());

                callLogSlave.setCalleeIdNumber(tmsDialplan.getCallee());
                callLogSlave.setCallerIdNumber(tmsDialplan.getCaller());

                if (tmsDialplan.getBorrowerInfo() != null) {
                    callLogSlave.setBorrowerFirstName(tmsDialplan.getBorrowerInfo().getBorrowerFirstName());
                    callLogSlave.setBorrowerLastName(tmsDialplan.getBorrowerInfo().getBorrowerLastName());
                }

            } else {
                callLogSlave.setCalleeIdNumber(cdr.getCallee_id_number());
                callLogSlave.setCallerIdNumber(cdr.getCaller_id_number());
                callLogSlave.setBorrowerFirstName(cdr.getBorrowerFirstName());
                callLogSlave.setBorrowerLastName(cdr.getBorrowerLastName());
            }

            callLogSlave.setLoanPk(cdr.getLoanId());
            try {
                if (cdr.getCallDirection() == CallDirection.INBOUND
                        || cdr.getCallDirection() == CallDirection.OUTBOUND) {
                    if (cdr.getLoanId() != null) {
                        svCollectionCallLogs.offer(new SCdrEntry(callLogSlave));
                    }
                }
            } catch (Exception ex) {
                LOG.error("Error in SVC", ex);
            }
        } catch (Exception ex) {
            LOG.error("Error in TMS", ex);
        }

    }

    public void updateCallDetailRecord(CDR cdr) {
        LOG.info("Building Master CDR: " + cdr.getCall_uuid() + " - " + cdr.getContext() + " - " + cdr.getOrderPower());
        CallDetailRecord mcdr = callDetailRecordService.updateCallDetailRecord(cdr);
        if (mcdr == null) {
            return;
        }
        SvCallDetailRecord callDetailRecordMaster = updateCallDetailRecord(mcdr);

        try {
//            callDetailRecordService.saveCDR(mcdr);
            if ((cdr.getCallDirection() == CallDirection.INBOUND
                    || cdr.getCallDirection() == CallDirection.OUTBOUND)) {
                LOG.info("Placing Master CDR in Queue {}", callDetailRecordMaster.getCallUUID());
                LOG.info(mcdr.toJson());
                callDetailRecordQueue.offer(new MCdrEntry(callDetailRecordMaster, currentTimeMillis()));
            }
        } catch (Exception ex) {
            LOG.error("Error in SVC", ex);
        }
    }

    public void offerNewMasterCallDetailRecordToSVCONLY(CallDetailRecord mcdr) {
        SvCallDetailRecord callDetailRecordMaster = updateCallDetailRecord(mcdr);
        if (callDetailRecordMaster != null) {
            LOG.info("Placing Master CDR in Queue Manualy {}", callDetailRecordMaster.getCallUUID());
            callDetailRecordQueue.offer(new MCdrEntry(updateCallDetailRecord(mcdr), currentTimeMillis()));
        }
    }

    public SvCallDetailRecord updateCallDetailRecord(CallDetailRecord mcdr) {
        SvCallDetailRecord callDetailRecordMaster = new SvCallDetailRecord();
        callDetailRecordMaster.setAnswerInMilliSec(mcdr.getAnswermsec());
        callDetailRecordMaster.setAnswerTime(mcdr.getAnswerTime());
        callDetailRecordMaster.setAutoAnswer(mcdr.getAutoAswer());
        callDetailRecordMaster.setBorrowerFirstName(mcdr.getBorrowerInfo().getBorrowerFirstName());
        callDetailRecordMaster.setBorrowerLastName(mcdr.getBorrowerInfo().getBorrowerLastName());
        callDetailRecordMaster.setBorrowerPhoneNumber(mcdr.getBorrowerInfo().getBorrowerPhoneNumber());
        if (mcdr.getCallDirection() != null) {
            callDetailRecordMaster.setCallDirection(mcdr.getCallDirection().getSvcCallDirection());
        }
        callDetailRecordMaster.setSpeechToTextRequested(mcdr.getSpeechToTextRequested());
        LOG.info("Setting speech to text flag to complete for {} - {}", mcdr.getCall_uuid(), mcdr.getSpeechToTextCompleted());
        callDetailRecordMaster.setSpeechToTextCompleted(mcdr.getSpeechToTextCompleted());
        callDetailRecordMaster.setCallDurationInMilliSec(mcdr.getDuration());
        callDetailRecordMaster.setCallRecordingUrl(mcdr.getCall_recording_url());
        callDetailRecordMaster.setCallUUID(mcdr.getCall_uuid());
        callDetailRecordMaster.setCalleeIdNumber(mcdr.getCallee_id_number());
        callDetailRecordMaster.setCallerIdNumber(mcdr.getCaller_id_number());
        callDetailRecordMaster.setComplete(mcdr.getComplete());
        callDetailRecordMaster.setDialer(mcdr.getDialer());
        if (mcdr.getDialer()) {
            callDetailRecordMaster.setDialerQueuePk(mcdr.getDialerQueueId());
            callDetailRecordMaster.setAmdStatus(mcdr.getAmd_status());
        } else {
            if (mcdr.getCallDirection() == CallDirection.INBOUND) {
                if (mcdr.getDialerQueueId() == null) {
                    callDetailRecordMaster.setDialerQueuePk(1l);
                } else {
                    callDetailRecordMaster.setDialerQueuePk(mcdr.getDialerQueueId());
                }
            }
        }

        callDetailRecordMaster.setAgentHangup(mcdr.isAgentHangup());
        callDetailRecordMaster.setCallerHangup(mcdr.isCallerHangup());

        callDetailRecordMaster.setEffectiveCallerIdNumber(mcdr.getEffective_caller_id_number());
        callDetailRecordMaster.setEndTime(mcdr.getEnd_time());
        callDetailRecordMaster.setLoanPk(mcdr.getBorrowerInfo().getLoanId());
        callDetailRecordMaster.setStartTime(mcdr.getStart_time());
        callDetailRecordMaster.setUsername(mcdr.getUsername());
        callDetailRecordMaster.setWaitInMilliSec(mcdr.getWaitmsec());
        callDetailRecordMaster.setIsVoicemail(mcdr.getInboundLeftVoicemail());
        callDetailRecordMaster.setUsername(mcdr.getUsername());

//        try {
//            Agent agent = null;
//            agent = agentService.getAgent(mcdr.getLastAgent());
//            if (agent != null) {
//                callDetailRecordMaster.setUsername(agent.getUserName());
//                mcdr.setUsername(agent.getUserName());
//            }
////            callDetailRecordMaster.setBadLanguage(mcdr.i);
//        } catch (Exception ex) {
//
//        }
        String disposition = null;
        try {
            disposition = dispositionCodeService.getDispositionCodeFromId(mcdr.getUserDispostionCode()).getDisposition();
        } catch (Exception ex) {
            disposition = "Unknown-" + mcdr.getUserDispostionCode();
        }
        callDetailRecordMaster.setUserDisposition(disposition);

        try {
            disposition = dispositionCodeService.getDispositionCodeFromId(mcdr.getSystemDispostionCode()).getDisposition();
        } catch (Exception ex) {
            disposition = "Unknown-" + mcdr.getSystemDispostionCode();
        }
        callDetailRecordMaster.setSystemDisposition(disposition);
        callDetailRecordMaster.setCallDispositionId(mcdr.getSystemDispostionCode());
        if (Objects.equals(mcdr.getSystemDispostionCode(), dispositionCodeService.answeringMachineDialerLeftMessageCode().getDispositionId())) {
            callDetailRecordMaster.setUserDisposition(disposition);
        }

        if (Objects.equals(mcdr.getSystemDispostionCode(), dispositionCodeService.answeringMachineCode().getDispositionId())) {
            callDetailRecordMaster.setUserDisposition(disposition);
        }

        //callDetailRecordMaster.setDestinationNumber(mcdr.getd);
        return callDetailRecordMaster;

    }

    @Bean
    public static Trigger cdrSvcUploaderTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity("cdrSvcUploaderTrigger")
                .forJob("cdrSVCUploader")
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever()
                        .withMisfireHandlingInstructionNextWithRemainingCount())
                .startNow()
                .build();
    }

//    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    @QuartzJob(name = "cdrSVCUploader", disallowConcurrentExecution = true)
    public void cdrSVCUploader() {
        LOG.debug("Running CDR upload to SVC.");
        ArrayList<MCdrEntry> entries = new ArrayList<>();
        callDetailRecordQueue.drainTo(entries);
        LOG.debug("Entries is empty: {}", !entries.isEmpty());
        if (!entries.isEmpty()) {
            Map<String, MCdrEntry> map = new HashMap<>(entries.size());
            LOG.debug("Map size {}", map.size());
            //we gather entries with the same callUUID and keep the most recent
            for (MCdrEntry entry : entries) {
                String key = entry.record.getCallUUID();
                MCdrEntry existing = map.get(key);
                if (existing != null) {
                    LOG.debug("Processing Key {}, {}, {}", key, existing.timestamp, entry.timestamp);
                }
                if (existing == null || existing.timestamp < entry.timestamp) {
                    if (existing != null) {
                        LOG.debug("Processing Key {}, {}, {}", key, existing.timestamp, entry.timestamp);
                    } else {
                        LOG.debug("Processing Key {}, {}, {}", key, "null", entry.timestamp);
                    }
                    map.put(key, entry);
                }
            }
            LOG.debug("Map size {}", map.size());

            for (MCdrEntry entry : map.values()) {
                long elapse = currentTimeMillis() - entry.timestamp;
                LOG.debug("Elapse Time {}", elapse);
                if (elapse > 1000) {
                    try {
                        SvCallDetailRecord svCallDetailRecord = entry.record;
                        LOG.info("Sending Master CDR for: {} - {}", svCallDetailRecord.getCallUUID(), svCallDetailRecord.getLoanPk());
                        tmsIWS.createOrUpdateCallDetailRecord(svCallDetailRecord);
                        continue;
                    } catch (Throwable th) {
                        LOG.warn("Failed to send Master CDR, will retry later", th);
                        entry.retryCount++;
                    }
                }
                if (entry.retryCount < config.getInteger("cdrSVCUploader.mcdr.retryCount", 3)) {
                    callDetailRecordQueue.add(entry);
                } else {
                    LOG.error("Failed to send Master CDR, too many retries. Giving up.");
                }
            }
        }

        ArrayList<SCdrEntry> logs = new ArrayList<>();
        svCollectionCallLogs.drainTo(logs);
        LOG.debug("Sending Slave CDR in Queue to SVC: {}", svCollectionCallLogs.size());
        if (!logs.isEmpty()) {
            LOG.info("Sending Slave CDR in Queue to SVC: {}", svCollectionCallLogs.size());

            for (SCdrEntry entry : logs) {
                try {
                    SvCollectionCallLog callLog = entry.record;
                    LOG.info("Sending Slave CDR for: {} - {}", callLog.getCallUUID(), callLog.getContext());
                    tmsIWS.createCollectionCallLog(callLog);
                    continue;
                } catch (Throwable th) {
                    LOG.warn("Failed to send Slave CDR, will retry later", th);
                    entry.retryCount++;
                }
                if (entry.retryCount < config.getInteger("cdrSVCUploader.scdr.retryCount", 3)) {
                    svCollectionCallLogs.add(entry);
                } else {
                    LOG.error("Failed to send Slave CDR, too many retries. Giving up.");
                }
            }
        }
    }

    public static class SCdrEntry implements DataSerializable {

        private int retryCount = 0;
        private SvCollectionCallLog record;

        private SCdrEntry() {
        }

        SCdrEntry(SvCollectionCallLog log) {
            this.record = log;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeInt(retryCount);
            out.writeObject(record);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            retryCount = in.readInt();
            record = in.readObject();
        }

    }

    public static class MCdrEntry implements DataSerializable {

        private long timestamp;
        private SvCallDetailRecord record;
        private int retryCount = 0;

        private MCdrEntry() {
        }

        MCdrEntry(SvCallDetailRecord record, long timestamp) {
            this.timestamp = timestamp;
            this.record = record;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeLong(timestamp);
            out.writeObject(record);
            out.writeInt(retryCount);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            timestamp = in.readLong();
            record = in.readObject();
            retryCount = in.readInt();
        }

    }

}
