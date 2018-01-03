/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer;

import com.hazelcast.core.IMap;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.svc.iws.CallDispositionCode;
import com.objectbrains.svc.iws.OutboundDialerQueueRecord;
import com.objectbrains.svc.iws.SvDialerQueueSettings;
import com.objectbrains.svc.iws.SvOutboundDialerQueueSettings;
import com.objectbrains.svc.iws.SvcException;
import com.objectbrains.svc.iws.TMSServiceIWS;
import com.objectbrains.tms.db.entity.DialerScheduleEntity;
import com.objectbrains.tms.db.repository.DialerQueueStatsRepository;
import com.objectbrains.tms.db.repository.DialerScheduleRepository;
import com.objectbrains.tms.enumerated.AgentState;
import com.objectbrains.tms.enumerated.DialerType;
import com.objectbrains.tms.hazelcast.AgentCallState;
import com.objectbrains.tms.hazelcast.Configs;
import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.hazelcast.entity.DialerCall;
import com.objectbrains.tms.hazelcast.keys.AgentQueueKey;
import com.objectbrains.tms.pojo.DialerSchedule;
import com.objectbrains.tms.service.AgentQueueAssociationService;
import com.objectbrains.tms.service.DialerQueueRecordService;
import com.objectbrains.tms.service.InboundCallService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import org.joda.time.LocalTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class DialerService implements BeanFactoryAware {

    private static final Logger LOG = LoggerFactory.getLogger(DialerService.class);

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private TMSServiceIWS tmsIws;

    @Autowired
    private AgentQueueAssociationService associationService;

//    @Autowired
//    private DialerQueueRepository dialerQueueRepository;
    @Autowired
    private DialerQueueRecordService dialerQueueRecordService;

    @Autowired
    private CallService callService;

    @Autowired
    private DialerCallService dialerCallService;

    @Autowired
    private DialerQueueStatsRepository dialerStatsRepository;

    @Autowired
    private DialerScheduleRepository dialerScheduleRepository;

    private AutowireCapableBeanFactory beanFactory;

    @Autowired
    @Qualifier("tms-executor")
    private TaskExecutor tmsExecutor;

    private IMap<Long, Dialer> dialerMap;
    private IMap<Long, Long> queueToDialerMap;

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    private void initialize() {
        dialerMap = hazelcastService.getMap(Configs.DIALER_MAP);
        queueToDialerMap = hazelcastService.getMap(Configs.QUEUE_TO_DIALER_MAP);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
    }

    private Dialer createDialer(long dialerPk, OutboundDialerQueueRecord record, LocalTime stopTime) throws DialerException {
        SvOutboundDialerQueueSettings settings = record.getSvDialerQueueSettings();
        switch (settings.getDialerMode()) {
            case PREDICTIVE:
                return new PredictiveDialer(dialerPk, record, stopTime);
            case PROGRESSIVE:
                return new ProgressiveDialer(dialerPk, record, stopTime);
            case VOICE:
                return new VoiceDialer(dialerPk, record, stopTime);
            case PREVIEW:
                switch (settings.getPreviewDialerType()) {
                    case SELECT_PHONE:
                        return new PreviewDialer(dialerPk, record, stopTime);
                    case ACCEPT_SKIP:
                    case DELAY_CALL:
                    case REGULAR:
                        return new PowerDialer(dialerPk, record, stopTime);
                }
            default:
                throw new IllegalArgumentException("Could not find appropriate Dialer based on settings for queue :" + settings.getDialerQueuePk());
        }
    }

    public void startQueue(final long queuePk) throws SvcException, DialerException {
        startQueue(queuePk, null);
    }

    public void startQueue(final long queuePk, LocalTime stopTime) throws SvcException, DialerException {

        OutboundDialerQueueRecord record = tmsIws.getOutboundDialerQueueRecord(queuePk);

        SvOutboundDialerQueueSettings settings = record.getSvDialerQueueSettings();
        LocalTime now = LocalTime.now();
        LocalTime startTime = settings.getStartTime();
        LocalTime endTime = settings.getEndTime();
        if (now.isBefore(startTime)) {
            throw new DialerException(queuePk, "Cannot start dialer until " + startTime.toString());
        }
        if (stopTime != null && now.isAfter(stopTime)) {
            throw new DialerException(queuePk, "Cannot start dialer after " + stopTime.toString());
        } else if (now.isAfter(endTime)) {
            throw new DialerException(queuePk, "Cannot start dialer after " + endTime.toString());
        }

        final long dialerPk = dialerStatsRepository.createStats(queuePk);

        final Dialer newDialer = createDialer(dialerPk, record, stopTime);
        beanFactory.autowireBean(newDialer);//...why?

        dialerMap.set(dialerPk, newDialer);
        Long oldDialerPk = queueToDialerMap.put(queuePk, dialerPk);
        if (oldDialerPk != null) {
            Dialer oldDialer = dialerMap.remove(oldDialerPk);
            if (oldDialer != null) {
                oldDialer.stop();
//            dialerLoanMap.executeOnEntries(new DeleteDialerNumbers(queuePk));
            }
        }

        dialerQueueRecordService.storeOutboundDialerQueueRecord(record);
        tmsExecutor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    final Dialer initializedDialer = (Dialer) beanFactory.initializeBean(newDialer, "dialer-" + dialerPk);
                    initializedDialer.start();
                } catch (DialerException ex) {
                    LOG.error("Exception while starting dialer: {}", queuePk, ex);
                }
            }
        });
    }

    public void resumeQueue(long queuePk) throws DialerException {
        Dialer dialer = getDialer(queuePk);
        if (dialer != null) {
            dialer.resume();
        }
    }

    public void pauseQueue(long queuePk) throws DialerException {
        Dialer dialer = getDialer(queuePk);
        if (dialer != null) {
            dialer.pause();
        }
    }

    public void stopQueue(long queuePk) throws DialerException {
        Dialer dialer = getDialer(queuePk);
        if (dialer != null) {
            dialer.stop();
        }
    }

    public List<DialerSchedule> getDialerSchedule(long queuePk) {
        List<DialerSchedule> ret = new ArrayList<>();
        for (DialerScheduleEntity entity : dialerScheduleRepository.getDialerSchedule(queuePk)) {
            DialerSchedule schedule = new DialerSchedule();
            schedule.copyFrom(entity);
            ret.add(schedule);
        }
        return ret;
    }

    public void setDialerSchedule(long queuePk, List<DialerSchedule> schedules) throws DialerException {
        String groupName = "schedule-dialer-" + queuePk;
        try {
            Set<TriggerKey> keys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName));
            scheduler.unscheduleJobs(new ArrayList<>(keys));
        } catch (SchedulerException ex) {
            throw new DialerException(queuePk, "Unable to delete previous dialer schedule for queue " + queuePk, ex);
        }

        List<DialerScheduleEntity> entities = dialerScheduleRepository.setDialerSchedule(queuePk, schedules);
        try {
            for (DialerScheduleEntity schedule : entities) {
                LocalTime startTime = schedule.getStartTime();
                LocalTime endTime = schedule.getEndTime();
                CronTrigger startTrigger
                        = TriggerBuilder.newTrigger()
                        .forJob(StartDialerJob.NAME, StartDialerJob.GROUP)
                        .usingJobData(StartDialerJob.buildDataMap(queuePk, endTime))
                        .withIdentity(schedule.getPk().toString(), groupName)
                        .withSchedule(
                                CronScheduleBuilder.weeklyOnDayAndHourAndMinute(
                                        schedule.getDayOfWeek(),
                                        startTime.getHourOfDay(),
                                        startTime.getMinuteOfHour()))
                        //                        .inTimeZone(schedule.getTimeZone().toTimeZone()))
                        .build();

                scheduler.scheduleJob(startTrigger);
            }
        } catch (SchedulerException ex) {
            throw new DialerException(queuePk, "Unable to set new schedule for dialer " + queuePk, ex);
        }
    }

    public void callInProgress(String callUUID, Long phoneNumber) {
        dialerCallService.lock(callUUID);
        try {
            DialerCall call = dialerCallService.getDialerCall(callUUID);
            if (call != null) {
                try {
                    Dialer dialer = dialerMap.get(call.getDialerPk());
                    if (dialer != null) {
                        dialer.callInProgress(call, phoneNumber);
                    }
                } catch (DialerException ex) {
                    LOG.error("Error while marking call {} as in-progress in dialer {}", callUUID, call.getQueuePk(), ex);
                }
            }
        } finally {
            dialerCallService.unlock(callUUID);
        }
    }

    public void callEnded(String callUUID, CallDispositionCode dispositionCode) {
        LOG.trace("Locking {}", callUUID);
        dialerCallService.lock(callUUID);
        LOG.trace("Locked {}", callUUID);
        try {
            DialerCall call = dialerCallService.getDialerCall(callUUID);
            LOG.trace("Got dialer call {}", callUUID);
            if (call != null) {
                switch (call.getState()) {
                    case IN_PROGRESS:
                    case PENDING:
                        try {
                            Dialer dialer = dialerMap.get(call.getDialerPk());
                            LOG.trace("Got dialer for {}, queue: {}", callUUID, call.getQueuePk());
                            if (dialer != null) {
                                dialer.callEnded(call, dispositionCode);
                                LOG.trace("dialer.callEnded {}, queue: {}", callUUID, call.getQueuePk());
                            }
                        } catch (DialerException ex) {
                            LOG.error("Error while marking call {} as success in dialer {}", callUUID, call.getQueuePk(), ex);
                        }
                        break;
                    default:
                        LOG.warn("call [{}] is in state [{}], expected [{} or {}]. Ignoring request.",
                                callUUID, call.getState(),
                                DialerCall.State.PENDING,
                                DialerCall.State.IN_PROGRESS);
                        break;
                }
            }
        } finally {
            LOG.trace("Unlocking");
            dialerCallService.unlock(callUUID);
            LOG.trace("Unlocked");
        }
    }
//    public void callSucceeded(String callUUID) {
//        DialerCall call = dialerCallService.getDialerCall(callUUID);
//        if (call != null) {
//            try {
//                dialerMap.get(call.getQueuePk()).callSucceeded(call);
//            } catch (DialerException ex) {
//                LOG.error("Error while marking call {} as success in dialer {}", callUUID, call.getQueuePk(), ex);
//            }
//        }
//    }
//
//    public void callFailed(String callUUID, CallDispositionCode dispositionCode) {
//        DialerCall call = dialerCallService.getDialerCall(callUUID);
//        if (call != null) {
//            try {
//                dialerMap.get(call.getQueuePk()).callFailed(call, dispositionCode);
//            } catch (DialerException ex) {
//                LOG.error("Error while marking call {} as failed in dialer {}", callUUID, call.getQueuePk(), ex);
//            }
//        }
//    }

    public void callResponded(String callUUID, long responseTimeMillis, Dialer.CallRespondedCallback callback) {
        LOG.trace("Locking {}", callUUID);
        dialerCallService.lock(callUUID);
        LOG.trace("Locked {}", callUUID);
        try {
            DialerCall call = dialerCallService.getDialerCall(callUUID);
            LOG.trace("Got dialer call {}", callUUID);
            if (call != null) {
                if (call.getResponseTimeMillis() != null) {
                    LOG.warn("call [{}] has already been marked as respondeed. Ignoring request.", callUUID);
                    return;
                }
                Dialer dialer = dialerMap.get(call.getDialerPk());
                LOG.trace("Got dialer for {}, queue: {}", callUUID, call.getQueuePk());
                if (dialer != null) {
                    try {
                        dialer.callResponded(call, responseTimeMillis, callback);
                        LOG.trace("dialer.callResponded {}, queue: {}", callUUID, call.getQueuePk());
                    } catch (DialerException ex) {
                        LOG.error("Error while marking call {} as responded in dialer {}", callUUID, call.getQueuePk(), ex);
                    }
                }
            }
        } finally {
            LOG.trace("Unlocking");
            dialerCallService.unlock(callUUID);
            LOG.trace("Unlocked");
        }
    }

    public void callDropped(String callUUID, long waitTimeMillis, CallDispositionCode dispositionCode) {
        dialerCallService.lock(callUUID);
        try {
            DialerCall call = dialerCallService.getDialerCall(callUUID);
            if (call != null) {
                switch (call.getState()) {
                    case IN_PROGRESS:
                    case PENDING:
                        Dialer dialer = dialerMap.get(call.getDialerPk());
                        if (dialer != null) {
                            try {
                                dialer.callDropped(call, waitTimeMillis, dispositionCode);
                            } catch (DialerException ex) {
                                LOG.error("Error while marking call {} as dropped in dialer {}", callUUID, call.getQueuePk(), ex);
                            }
                        }
                        break;
                    default:
                        LOG.warn("call [{}] is in state [{}], expected [{} or {}]. Ignoring request.",
                                callUUID, call.getState(),
                                DialerCall.State.PENDING,
                                DialerCall.State.IN_PROGRESS);
                        break;
                }
            }
        } finally {
            dialerCallService.unlock(callUUID);
        }
    }

    public TreeMap<Integer, Map<DialerType, List<QueueWeight>>> getPrioirtyQueueWeightMap(int ext) {
        //get queues associated with this agent
        Map<AgentQueueKey, AgentWeightedPriority> associations = associationService.getAssociations(ext);

        Set<Long> inboundQueuePks = new HashSet<>();
        for (Map.Entry<AgentQueueKey, AgentWeightedPriority> entrySet : associations.entrySet()) {
            AgentQueueKey key = entrySet.getKey();
            AgentWeightedPriority awp = entrySet.getValue();
            if (awp.getDialerType() == DialerType.INBOUND) {
                inboundQueuePks.add(key.getQueuePk());
            }
        }

        Map<Long, SvDialerQueueSettings> settingsMap = dialerQueueRecordService.getQueueSettings(inboundQueuePks);

        //now build the primary tree map
        TreeMap<Integer, Map<DialerType, List<QueueWeight>>> priorityMap = new TreeMap<>();
        for (Map.Entry<AgentQueueKey, AgentWeightedPriority> entrySet : associations.entrySet()) {
            AgentQueueKey key = entrySet.getKey();
            AgentWeightedPriority awp = entrySet.getValue();

//            DialerQueue queue = queues.get(key.getQueuePk());
//            Integer priority = awp.getPriority();
//            Double weight = awp.getWeight();
//            if (priority == null) {
//                priority = queue.getPriority();
//            }
//            if (weight == null) {
//                weight = queue.getWeight();
//            }
//            AgentWeightedPriority value = new AgentWeightedPriority(awp);
//            value.setPriority(priority);
//            value.setWeight(weight);
            QueueWeight qwp = new QueueWeight(key.getQueuePk(), awp, settingsMap.get(key.getQueuePk()));

            Map<DialerType, List<QueueWeight>> typeMap = priorityMap.get(awp.getPriority());

            if (typeMap == null) {
                typeMap = new HashMap<>();
                priorityMap.put(awp.getPriority(), typeMap);
            }
            List<QueueWeight> list = typeMap.get(awp.getDialerType());
            if (list == null) {
                list = new ArrayList<>();
                typeMap.put(awp.getDialerType(), list);
            }
            list.add(qwp);
        }
        return priorityMap;
    }

    public void agentReady(int ext) {
        LOG.info("agentReady: " + ext);
        if (callService.connectToWaitingPrimaryCall(ext)) {
            return;
        }
        LOG.info("agentReady after connect to waiting primary: {}", ext);
        TreeMap< Integer, Map<DialerType, List<QueueWeight>>> priorityMap = getPrioirtyQueueWeightMap(ext);
        LOG.info("agentReady after connect to waiting primary: {}", ext);
        while (true) {
            QueueWeight qwp = pollNextQueue(priorityMap);
            if (qwp == null) {
                LOG.info("no more queues to process");
                return;
            }
            LOG.info("trying queue: {}", qwp.queuePk);
            if (qwp.awp.getDialerType() == DialerType.INBOUND) {
                if (InboundCallService.shouldRecieveCall(false, AgentState.IDLE, new AgentCallState(), qwp.awp, qwp.settings)
                        && callService.connectToNextWaitingCall(qwp.queuePk, ext)) {
                    LOG.info("INBOUND connected to waiting call. QueuePK: {}, ext:{}", qwp.queuePk, ext);
                    return;
                }
            } else {
                Dialer dialer = getDialer(qwp.queuePk);
                try {
                    LOG.info("OUTBOUND chandle agent ready. QueuePK: {}, ext:{}", qwp.queuePk, ext);
                    if (dialer != null && dialer.handleAgentReady(ext)) {
                        return;
                    }
                } catch (DialerException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * traverse the queue by priority and select queue that this agent will
     * become available for
     *
     * @param priorityMap
     * @return
     */
    public QueueWeight pollNextQueue(TreeMap<Integer, Map<DialerType, List<QueueWeight>>> priorityMap) {
        for (Map<DialerType, List<QueueWeight>> typeMap : priorityMap.values()) {
            QueueWeight qwp;
            qwp = Utils.pollNextWeightedObject(typeMap.get(DialerType.INBOUND));
            if (qwp != null) {
                return qwp;
            }
            qwp = Utils.pollNextWeightedObject(typeMap.get(DialerType.POWER));
            if (qwp != null) {
                return qwp;
            }
            qwp = Utils.pollNextWeightedObject(typeMap.get(DialerType.PROGRESSIVE));
            if (qwp != null) {
                return qwp;
            }
            qwp = Utils.pollNextWeightedObject(typeMap.get(DialerType.PREDICTIVE));
            if (qwp != null) {
                return qwp;
            }
        }
        return null;//there are no more queues
    }

    public List<Dialer> getDialers() {
        Set<Long> dialerPks = new HashSet<>(queueToDialerMap.values());
        return new ArrayList<>(dialerMap.getAll(dialerPks).values());
    }

    public Dialer getDialer(long queuePk) {
        Long dialerPk = queueToDialerMap.get(queuePk);
        if (dialerPk == null) {
            return null;
        }
        return dialerMap.get(dialerPk);
    }

    public Dialer getDialerByPk(long dialerPk) {
        return dialerMap.get(dialerPk);
    }

    public static class QueueWeight implements WeightedObject {

        public final long queuePk;
        public final SvDialerQueueSettings settings;
        public final AgentWeightedPriority awp;

        public QueueWeight(long queuePk, AgentWeightedPriority awp, SvDialerQueueSettings settings) {
            this.queuePk = queuePk;
            this.awp = awp;
            this.settings = settings;
        }

        @Override
        public double getWeight() {
            return awp.getWeight();
        }

    }

}
