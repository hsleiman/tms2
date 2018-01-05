/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer;

import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.sti.constants.CallDispositionActionType;
import com.objectbrains.sti.constants.CallTimeCode;
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.sti.db.entity.disposition.action.CallDispositionAction;
import com.objectbrains.sti.db.entity.disposition.action.DoNotCallAction;
import com.objectbrains.sti.db.entity.disposition.action.DoNotCallPhoneAction;
import com.objectbrains.sti.db.entity.disposition.action.RetryCallAction;
import com.objectbrains.sti.embeddable.AgentWeightPriority;
import com.objectbrains.sti.embeddable.OutboundDialerQueueRecord;
import com.objectbrains.sti.pojo.BasicPhoneData;
import com.objectbrains.sti.pojo.CustomerPhoneData;
import com.objectbrains.sti.pojo.DialerQueueAccountDetails;
import com.objectbrains.sti.service.dialer.DialerQueueService;
import com.objectbrains.sti.service.dialer.PhoneNumberCallable;
import com.objectbrains.sti.service.tms.TMSService;
import com.objectbrains.tms.enumerated.AgentState;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.hazelcast.AgentDialerState;
import com.objectbrains.tms.hazelcast.Configs;
import com.objectbrains.tms.hazelcast.entity.AgentTMS;
import com.objectbrains.tms.hazelcast.entity.DialerCall;
import com.objectbrains.tms.hazelcast.entity.DialerLoan;
import com.objectbrains.tms.hazelcast.entity.DialerStats;
import com.objectbrains.tms.service.AgentCallService;
import com.objectbrains.tms.service.AgentQueueAssociationService;
import com.objectbrains.tms.service.TMSAgentService;
import com.objectbrains.tms.service.AgentStatsService;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.DialplanService;
import com.objectbrains.tms.service.DncService;
import com.objectbrains.tms.websocket.WebsocketService;
import com.objectbrains.tms.websocket.message.outbound.PhoneToType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author connorpetty
 */
public abstract class AbstractDialer implements Dialer, DataSerializable, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDialer.class);

    protected OutboundDialerQueueRecord record;

    protected IQueue<DialerQueueAccountDetails> notReadyLoans;
    protected IQueue<DialerQueueAccountDetails> readyLoans;
    protected IQueue<LoanNumber> retryCalls;

    protected IMap<Long, DialerQueueAccountDetails> loanDetailsMap;
    protected IMap<Long, DialerLoan> loanMap;
    protected ILock dialerLock;

    protected DateTime endTime;

    protected long dialerPk;

    @Autowired
    protected CallDetailRecordService callDetailRecordService;

    @Autowired
    protected AgentQueueAssociationService assocService;

    @Autowired
    protected DialerService dialerService;

    @Autowired
    protected TMSAgentService agentService;

    @Autowired
    protected AgentStatsService statsService;

    @Autowired
    protected AgentCallService agentCallService;

    @Autowired
    protected HazelcastService hazelcastService;

    @Autowired
    protected CallService callService;

    @Autowired
    protected DialerStatsService dialerStatsService;

    @Autowired
    protected DialerCallService dialerCallService;

    @Autowired
    protected TMSService tmsIws;
    
    @Autowired
    protected DialerQueueService dialerQueueService;

    @Autowired
    protected Scheduler scheduler;

    @Autowired
    protected DncService dncService;

    @ConfigContext
    protected DialerConfig config;

    @Autowired
    protected WebsocketService websocketService;

    @Autowired
    private DialplanService dialplanRepository;

    protected AbstractDialer() {
    }

    public AbstractDialer(long dialerPk, OutboundDialerQueueRecord record, LocalTime stopTime) {
        this.dialerPk = dialerPk;
        this.record = record;
        if (stopTime != null) {
            this.endTime = stopTime.toDateTimeToday();
        } else {
            //we save as a DateTime to basically pin down the endtime to today
            this.endTime = record.getDialerQueueSettings().getEndTime().toDateTimeToday();
        }
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(record);
        out.writeObject(endTime);
        out.writeLong(dialerPk);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        record = in.readObject();
        endTime = in.readObject();
        dialerPk = in.readLong();
    }

    @Override
    public DateTime getEndTime() {
        return endTime;
    }

    private TriggerKey readyLoansTriggerKey() {
        return TriggerKey.triggerKey("ready-loans", pausableTriggerGroupPrefix());
    }

    private TriggerKey stopDialerTriggerKey() {
        return TriggerKey.triggerKey("stop-dialer", dialerTriggerGroupPrefix());
    }

    protected final String queueTriggerGroupPrefix() {
        return "dialer-" + getQueuePk();
    }

    protected final String dialerTriggerGroupPrefix() {
        return queueTriggerGroupPrefix() + "-" + getDialerPk();
    }

    protected final String pausableTriggerGroupPrefix() {
        return dialerTriggerGroupPrefix() + "-pausable";
    }

    private String dialerRetryTriggerGroup() {
        return pausableTriggerGroupPrefix() + "-retry";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        final long queuePk = record.getDqPk();
        notReadyLoans = Configs.getNotReadyLoansQueue(hazelcastService, dialerPk);
        readyLoans = Configs.getReadyLoansQueue(hazelcastService, dialerPk);
        retryCalls = Configs.getRetryCallQueue(hazelcastService, dialerPk);
        loanDetailsMap = Configs.getDialerLoanDetailsMap(hazelcastService, dialerPk);
        loanMap = Configs.getDialerLoanMap(hazelcastService, dialerPk);
        dialerLock = Configs.getDialerLock(hazelcastService, dialerPk);
    }

    private void clearData() {
        notReadyLoans.destroy();
        readyLoans.destroy();
        retryCalls.destroy();
        loanDetailsMap.destroy();
        loanMap.destroy();
        dialerCallService.evictCalls(getDialerPk());

//        notReadyLoans.clear();
//        readyLoans.clear();
//        retryCalls.clear();
//        loanDetailsMap.clear();
//        loanMap.clear();
//        dialerCallService.evictCalls(getQueuePk());
    }

    private static String normalizeDnc(Long phoneNumber) {
        if (phoneNumber < 1_000_000_0000l) {
            phoneNumber += 1_000_000_0000l;
        }
        return phoneNumber.toString();
    }

    private LoanNumber pollNextLoanNumber() {
        LoanNumber loanNumber = pollReadyCall();
        while (loanNumber == null) {
            DialerQueueAccountDetails details = readyLoans.poll();
            if (details == null) {
                return null;
            }
            long loanPk = details.getAccountPk();
            loanNumber = Utils.getFirstNumber(details);
            if (loanNumber == null) {
                //this loan has no numbers so just mark it as completed
                loanCompleted(loanPk, DialerLoan.CompleteReason.NO_MORE_NUMBERS);
            } else {
                DialerLoan loan = loanMap.get(loanPk);
                setLoanState(loan, DialerLoan.State.IN_PROGRESS);
                loanMap.put(loanPk, loan);
            }
        }
        return loanNumber;
    }

    protected final boolean makeNextCall(Integer ext) throws DialerException {
        if (!isRunning()) {
            return false;
        }
        for (;;) {
            LoanNumber loanNumber = pollNextLoanNumber();
            if (loanNumber == null) {
                return false;
            }
            Long loanPk = loanNumber.getLoanPk();
            DialerQueueAccountDetails details = loanDetailsMap.get(loanPk);
            PhoneToType callData = Utils.getPhoneToType(loanNumber, details);
            Long phoneNumber = callData.getPhoneNumber();
            try {
                LOG.info("Checking Call[ext: {}, phoneNumber: {}, loanPk: {}, QueuePK: {}]", ext, phoneNumber, loanPk, getQueuePk());
                if (dncService.isInDNC(normalizeDnc(phoneNumber))) {
                    LOG.info("Skipping Call, is dnc[ext: {}, phoneNumber: {}, loanPk: {}, QueuePK: {}]", ext, phoneNumber, loanPk, getQueuePk());

                    dialplanRepository.LogDialplanInfoIntoDb("DIALER_DIALER", "Skipping Call, is dnc[ext: {}, phoneNumber: {}, loanPk: {}, QueuePK: {}]", ext, phoneNumber, loanPk, getQueuePk());

                } else if (shouldNumberCheck()
                        && callDetailRecordService.checkNumberToCallForDialer(normalizeDnc(phoneNumber)) == false) {

                    LOG.info("Skipping Call, is called recently[ext: {}, phoneNumber: {}, loanPk: {}, timeout: {}, QueuePK: {}]", ext, phoneNumber, loanPk, callDetailRecordService.getNumberToCallForDialerTimeout(normalizeDnc(phoneNumber)), getQueuePk());
                    dialplanRepository.LogDialplanInfoIntoDb("DIALER_DIALER", "Skipping Call, is called recently[ext: {}, phoneNumber: {}, loanPk: {}, timeout: {}, QueuePK: {}]", ext, phoneNumber, loanPk, callDetailRecordService.getNumberToCallForDialerTimeout(normalizeDnc(phoneNumber)), getQueuePk());

                } else {
                    PhoneNumberCallable callable = dialerQueueService.canCallNumberInQueue(getQueuePk(), loanPk, phoneNumber);
                    CallTimeCode timeCode = callable.getCallTimeCode();
                    switch (timeCode) {
                        case OK_TO_CALL:
                            //call
                            LOG.info("Making Call[ext: {}, phoneNumber: {}, loanPk: {}, QueuePK: {}]", ext, phoneNumber, loanPk, getQueuePk());
                            String uuid = makeCall(ext, loanNumber, details);
                            if (uuid == null) {
                                LOG.warn("Failed to start call {} for loanPk: {}, in QueuePK: {}, "
                                        + "most likely agent was unavailable. Trying again in a minute", phoneNumber, loanPk, getQueuePk());
                                addReadyCall(loanNumber);
                                return false;
                            }
                            dialerCallService.createCall(uuid, getQueuePk(), getDialerPk(), loanNumber, callData);
                            return true;
                        case TOO_EARLY:
                            //put in retry queue
                            scheduleRetry(loanNumber, callable.getEarliestTimeToCall());
                            continue;
                    }
                }
                //skip phone number but try next one
                prepareNextCall(loanNumber, details);
            }catch (Exception ex) {
                LOG.error("Error occurred while checking whether loanPk: [{}] is in QueuePK: [{}], skipping loan", loanPk, getQueuePk(), ex);
                scheduleRetry(loanNumber, LocalDateTime.now().plusMinutes(1));
            }
            //let loan die and pick next loan
            
        }
    }

    protected boolean shouldNumberCheck() {
        return true;
    }

    protected abstract String makeCall(Integer ext, LoanNumber loanNumber, DialerQueueAccountDetails details);

    private void loanCompleted(Long loanPk, DialerLoan.CompleteReason reason) {
        Map<Long, DialerLoan> loanMap = getLoans();
        DialerLoan loan = loanMap.get(loanPk);
        setLoanState(loan, DialerLoan.State.COMPLETE);
        loan.setCompleteReason(reason);
        loan.setCompleteTime(LocalDateTime.now());
        loanMap.put(loanPk, loan);
    }

    private void setCallState(DialerCall call, DialerCall.State newState) {
        DialerCall.State oldState = call.getState();
        call.setState(newState);
        dialerStatsService.updateStateCount(getDialerPk(), oldState, newState);
    }

    private void setLoanState(DialerLoan loan, DialerLoan.State newState) {
        DialerLoan.State oldState = loan.getState();
        loan.setState(newState);
        dialerStatsService.updateStateCount(getDialerPk(), oldState, newState);
    }

    @Override
    public void addReadyCall(LoanNumber loanNumber) {
        retryCalls.add(new LoanNumber(loanNumber));
        dialerStatsService.incrementReadyCallCount(getDialerPk());
    }

    private LoanNumber pollReadyCall() {
        LoanNumber call = retryCalls.poll();
        if (call != null) {
            dialerStatsService.decrementReadyCallCount(getDialerPk());
        }
        return call;
    }

    @Override
    public final void handleReadyLoans() throws DialerException {
        if (isRunning()) {
            dialerLock.lock();
            try {
                handleReadyLoansInternal();
            } finally {
                dialerLock.unlock();
            }
        }
    }

    protected void handleReadyLoansInternal() throws DialerException {
        for (AgentWeightPriority agent : record.getAgentWeightPriorityList()) {
            Integer extension = agentService.getAgentExtension(agent.getUsername());
            if (extension == null) {
                throw new NullPointerException("Agent " + agent.getUsername() + " could not be found!");
            }
            handleAgentReady0(extension, true);
        }
    }

//    @Override
//    public final void handleReadyLoans() throws DialerException {
//        if (isRunning()) {
//            handleReadyLoansInternal();
//        }
//    }
//
//    protected void handleReadyLoansInternal() throws DialerException {
//        Map<Integer, ?> agents = assocService.getParticipatingAgents(getQueuePk(), CallDirection.OUTBOUND, true);
//        for (Integer extension : agents.keySet()) {
//            handleAgentReadyInternal(extension);
//        }
//    }
    @Override
    public void callEnded(DialerCall call, CallDispositionCode dispositionCode) throws DialerException {
        Long dispositionId = dispositionCode.getDispositionId();
        call.setDispositionCodeId(dispositionId);

        CallDispositionActionType type;

        if (dispositionCode.isSuccess()) {
            LOG.info("call succeeded {}", call.getCallUUID());
            setCallState(call, DialerCall.State.SUCCESSFUL);
            type = CallDispositionActionType.MARK_ACCOUNT_AS_COMPLETED;
        } else {
            LOG.info("call failed {}", call.getCallUUID());
            setCallState(call, DialerCall.State.FAILED);
            type = CallDispositionActionType.TRY_NEXT_PHONE_NUMBER;
        }
        dialerCallService.save(call);

        handleDispositionCode(call, dispositionCode, type);
    }

    private LocalDateTime calculateExpireTime(long seconds) {
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds((int) seconds);
        if (seconds >= 60 * 60 * 24) {//1 day
            LocalDateTime tomorrow = LocalDate.now().plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT);
            if (expireTime.isAfter(tomorrow)) {
                expireTime = expireTime.toLocalDate().toLocalDateTime(LocalTime.MIDNIGHT);;
            }
        }
        return expireTime;
    }

    private void handleDispositionCode(DialerCall call, CallDispositionCode dispositionCode,
            CallDispositionActionType defaultActionType) throws DialerException {
        LOG.info("handling disposition: [{} - {}] for call [{}]",
                dispositionCode.getDispositionId(), dispositionCode.getDisposition(), call.getCallUUID());
        CallDispositionActionType type = defaultActionType;
        DialerQueueAccountDetails details = loanDetailsMap.get(call.getLoanPk());
        if (getState() == State.STOPPED) {
            loanCompleted(call.getLoanPk(), DialerLoan.CompleteReason.DIALER_STOPPED);
            return;
        }
        CallDispositionAction action = findResponseAction(details, dispositionCode);
        if (action != null) {
            type = action.getActionType();
        } else {
            LOG.warn("no action found for dispositionCode {}", dispositionCode.getDispositionId());
        }
        LOG.debug("performing action [{}] for call [{}] ", type, call.getCallUUID());
        switch (type) {
            case MARK_ACCOUNT_AS_COMPLETED:
                loanCompleted(call.getLoanPk(), DialerLoan.CompleteReason.DISPOSITIONED);
                return;
            case DO_NOT_CALL://do not call loan
                DoNotCallAction dncAction = (DoNotCallAction) action;
                LocalDateTime expireTime = calculateExpireTime(dncAction.getDncDurationInSeconds());
                for (CustomerPhoneData data : details.getCustomerPhoneData()) {
                    for (BasicPhoneData phoneData : data.getBasicPhoneData()) {
                        dncService.createDNC(normalizeDnc(phoneData.getPhoneNumber()), "DIALER_ACTION", expireTime);
                    }
                }
                break;
            case DO_NOT_CALL_NUMBER://do not call phone number
                DoNotCallPhoneAction dncpAction = (DoNotCallPhoneAction) action;
                expireTime = calculateExpireTime(dncpAction.getDncDurationInSeconds());
                dncService.createDNC(normalizeDnc(call.getCallInfo().getPhoneNumber()), "DIALER_ACTION", expireTime);
                break;
            case RETRY_CALL:
                RetryCallAction retryAction = (RetryCallAction) action;
                long failCount = dialerCallService.getFailedCallCount(getDialerPk(),
                        call.getLoanPk(),
                        dispositionCode.getDispositionId());
                if (failCount <= retryAction.getRetryCount()) {
                    LocalDateTime now = LocalDateTime.now();
                    long recallTime = retryAction.getRecallTimeInSeconds();
                    if (recallTime == 0) {
                        addReadyCall(call);
                    } else {
                        scheduleRetry(call, now.plusSeconds((int) recallTime));
                    }
                    return;
                }
            //fall through
            case TRY_NEXT_PHONE_NUMBER:
                break;
        }
        prepareNextCall(call, details);
    }

    private CallDispositionAction findResponseAction(DialerQueueAccountDetails details, CallDispositionCode dispositionCode) {
        return dispositionCode.getAction();
    }

    private void scheduleRetry(LoanNumber loanNumber, LocalDateTime time) throws DialerException {
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(loanNumber.getLoanPk() + "-" + loanNumber.getNumberIndex(), dialerRetryTriggerGroup())
                .forJob(RetryCallJob.NAME, RetryCallJob.GROUP)
                .usingJobData(RetryCallJob.buildDataMap(getDialerPk(), loanNumber))
                .startAt(time.toDate())
                .endAt(getEndTime().toDate())
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();
        try {
            scheduler.scheduleJob(trigger);
            dialerStatsService.incrementScheduledCallCount(getDialerPk());
        } catch (SchedulerException ex) {
            throw new DialerException(this, ex);
        }
    }

    private void prepareNextCall(LoanNumber call, DialerQueueAccountDetails details) {
        //get next number
        LoanNumber nextNumber = getNextRetryNumber(call, details);
        if (nextNumber != null) {
            //prepare the next number
            addReadyCall(nextNumber);
        } else {
            loanCompleted(call.getLoanPk(), DialerLoan.CompleteReason.NO_MORE_NUMBERS);
        }
    }

    protected LoanNumber getNextRetryNumber(LoanNumber call, DialerQueueAccountDetails details) {
        return Utils.getNextNumber(call, details);
    }

    @Override
    public void callInProgress(DialerCall call, Long phoneNumber) throws DialerException {
        call.getCallInfo().setPhoneNumber(phoneNumber);
        setCallState(call, DialerCall.State.IN_PROGRESS);
        dialerCallService.save(call);
    }

    @Override
    public void callResponded(DialerCall call, long respondTimeMillis, CallRespondedCallback callback) throws DialerException {
        call.setResponseTimeMillis(respondTimeMillis);
        dialerCallService.save(call);
        LOG.trace("Saved call: {}", call.getCallUUID());
        LOG.trace("dialerStatsService.addResponseTime");
        dialerStatsService.addResponseTime(getDialerPk(), respondTimeMillis);
    }

    @Override
    public void callDropped(DialerCall call, long waitTimeMillis, CallDispositionCode dispositionCode) throws DialerException {
        LOG.info("callDropped {}", call);
        call.setDispositionCodeId(dispositionCode.getDispositionId());
        call.setWaitTimeMillis(waitTimeMillis);
        setCallState(call, DialerCall.State.DROPPED);
        dialerCallService.save(call);
        dialerStatsService.addWaitTimeMillis(getDialerPk(), waitTimeMillis);

        handleDispositionCode(call, dispositionCode, CallDispositionActionType.MARK_ACCOUNT_AS_COMPLETED);
    }

    @Override
    public long getQueuePk() {
        return record.getDqPk();
    }

    @Override
    public long getDialerPk() {
        return dialerPk;
//        return dialerStatsService.getStats(getQueuePk()).getPk();
    }

    @Override
    public Queue<DialerQueueAccountDetails> getNotReadyLoans() {
        return notReadyLoans;
    }

    @Override
    public Queue<DialerQueueAccountDetails> getReadyLoans() {
        return readyLoans;
    }

    @Override
    public Map<Long, DialerLoan> getLoans() {
        return loanMap;
    }

    @Override
    public OutboundDialerQueueRecord getRecord() {
        return record;
    }

    protected void scheduleTriggers() throws DialerException {
        int delayMin = config.getStartDelayMinutes();
        LocalDateTime startTime = LocalDateTime.now().plusMinutes(config.getStartDelayMinutes());
        Trigger readyLoansTrigger = TriggerBuilder.newTrigger()
                .withIdentity(readyLoansTriggerKey())
                .forJob(ReadyLoansJob.NAME, ReadyLoansJob.GROUP)
                .usingJobData(ReadyLoansJob.buildDataMap(getDialerPk(), record.getDialerQueueSettings().isBestTimeToCall()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .startAt(startTime.toDate())
                .build();
        try {
            scheduler.scheduleJob(readyLoansTrigger);

            if (delayMin > 0) {
                String queueName = dialerQueueService.getDialerQueueByPk(getQueuePk()).getQueueName();
                List<AgentTMS> agents = agentService.getAgents(getRecord().getAgentWeightPriorityList(), null, null);

                StringBuilder message = new StringBuilder();
                message.append("Dialer for ")
                        .append(queueName)
                        .append(" will begin in ")
                        .append(delayMin);

                if (delayMin > 1) {
                    message.append(" minutes");
                } else {
                    message.append(" minute");
                }
                websocketService.sendPushNotification(agents, message.toString());
            }
        } catch (SchedulerException ex) {
            throw new DialerException(this, ex);
        } catch (Exception ex) {
            throw new DialerException(this, ex);
        }

        Trigger stopDialerTrigger = TriggerBuilder.newTrigger()
                .withIdentity(stopDialerTriggerKey())
                .forJob(StopDialerJob.NAME, StopDialerJob.GROUP)
                .usingJobData(StopDialerJob.buildDataMap(getDialerPk()))
                .startAt(getEndTime().toDate())
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();
        try {
            scheduler.scheduleJob(stopDialerTrigger);
        } catch (SchedulerException ex) {
            throw new DialerException(this, ex);
        }
    }

    private void resumeTriggers() throws DialerException {
        try {
            scheduler.resumeTriggers(GroupMatcher.triggerGroupStartsWith(pausableTriggerGroupPrefix()));
        } catch (SchedulerException ex) {
            throw new DialerException(this, ex);
        }
    }

    private void pauseTriggers() throws DialerException {
        try {
            scheduler.pauseTriggers(GroupMatcher.triggerGroupStartsWith(pausableTriggerGroupPrefix()));
        } catch (SchedulerException ex) {
            throw new DialerException(this, ex);
        }
    }

    private void unscheduleTriggers() throws DialerException {
        try {
            Set<TriggerKey> retryKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupStartsWith(queueTriggerGroupPrefix()));
            scheduler.unscheduleJobs(new ArrayList<>(retryKeys));
        } catch (SchedulerException ex) {
            throw new DialerException(this, ex);
        }
    }

    @Override
    public void start() throws DialerException {
        //just in case there are triggers hanging around
        dialerLock.lock();
        try {
            LOG.info("Unschedule Triggers for {} QueuePK: {}", loanMap.size(), getQueuePk());
            unscheduleTriggers();

//            clearData();
            List<DialerQueueAccountDetails> detailsList = new ArrayList<>(record.getAccountDetails());
//            dialerPk = dialerStatsService.startStats(getQueuePk(), detailsList.size(), getDialerType());
            dialerStatsService.startStats(getDialerPk(), detailsList.size(), getDialerType());
            if (record.getDialerQueueSettings().isBestTimeToCall()) {
                Collections.sort(detailsList, new LoanDetailsComparator());
            }
            notReadyLoans.addAll(detailsList);

            //populate the loan maps
            for (DialerQueueAccountDetails details : detailsList) {
                Long loanPk = details.getAccountPk();
                loanDetailsMap.put(loanPk, details);

                List<CustomerPhoneData> borrowerPhoneDatas = details.getCustomerPhoneData();
                StringBuilder sb = new StringBuilder();
                sb.append("LoanID: ").append(loanPk).append(" ");
                for (int i = 0; i < borrowerPhoneDatas.size(); i++) {
                    CustomerPhoneData get = borrowerPhoneDatas.get(i);
                    List<BasicPhoneData> basicPhoneDatas = get.getBasicPhoneData();
                    sb.append(i).append("_Phones: ");
                    for (BasicPhoneData get1 : basicPhoneDatas) {
                        sb.append(get1.getPhoneNumber());
                        sb.append(", ");
                    }
                }
                LOG.info("Adding {} QueuePK: {}", sb.toString(), getQueuePk());

                DialerLoan loan = new DialerLoan();
                loan.setLoanPk(loanPk);
                loan.setState(DialerLoan.State.NOT_READY);
                getLoans().put(loanPk, loan);
            }

            //make sure that triggers aren't paused first to make sure misfires
            //don't happen
            LOG.info("Resume Triggers for {} QueuePK: {} ", loanMap.size(), getQueuePk());
            resumeTriggers();
            //schedule the basic triggers
            LOG.info("Schedule Triggers for {} QueuePK: {}", loanMap.size(), getQueuePk());
            scheduleTriggers();
            LOG.info("Final List for {} QueuePK: {}", loanMap.size(), getQueuePk());
        } catch (RuntimeException ex) {
            LOG.error("Exception", ex);
            throw ex;
        } finally {
            dialerLock.unlock();
        }
    }

    @Override
    public void pause() throws DialerException {
        dialerLock.lock();
        try {
            dialerStatsService.pauseStats(getDialerPk());
            pauseTriggers();
        } finally {
            dialerLock.unlock();
        }
    }

    @Override
    public void resume() throws DialerException {
        dialerLock.lock();
        try {
            dialerStatsService.resumeStats(getDialerPk());
            resumeTriggers();
        } finally {
            dialerLock.unlock();
        }
        handleReadyLoans();
    }

    @Override
    public void stop() throws DialerException {
        dialerLock.lock();
        try {
            dialerStatsService.stopStats(getDialerPk());
            //pause all trigger dialer triggers so that if the unschedule fails any
            //new triggers will still be paused
            pauseTriggers();
            unscheduleTriggers();
            clearData();
        } finally {
            dialerLock.unlock();
        }
    }

    @Override
    public State getState() {
        return dialerStatsService.getDialerState(getDialerPk());
    }

    @Override
    public final boolean isRunning() {
        return getState() == State.RUNNING;
    }

    @Override
    public boolean handleAgentReady(int ext) throws DialerException {
        return handleAgentReady0(ext, true);
    }

    protected final boolean handleAgentReady0(int ext, boolean checkRunning) throws DialerException {
        if (!checkRunning || isRunning()) {
            dialerLock.lock();
            try {
                LOG.info("handleAgentReadyInternal for {}", ext);
                AgentDialerState state = agentService.getAgentDialerState(ext, CallDirection.OUTBOUND, true);
                if (state != null) {
                    LOG.info("Checking Agent {}: [state: {}, active: {}", ext, state.getState(), state.isDialerActive());
                    if (state.isDialerActive() && state.getState() == AgentState.IDLE && !state.hasCalls()) {
                        return handleAgentReadyInternal(ext);
                    }
                }
            } finally {
                dialerLock.unlock();
            }
        }
        return false;
    }

    protected boolean handleAgentReadyInternal(int ext) throws DialerException {
        return makeNextCall(ext);
    }

    @Override
    public boolean isLoanComplete(Long loanPk) {
        DialerLoan loan = loanMap.get(loanPk);
        if (loan == null) {
            return false;
        }
        return loan.getState() == DialerLoan.State.COMPLETE;
    }

    @Override
    public DialerStats getDialerStats() {
        return dialerStatsService.getStats(getDialerPk());
    }

    private static class LoanDetailsComparator implements Comparator<DialerQueueAccountDetails> {

        @Override
        public int compare(DialerQueueAccountDetails o1, DialerQueueAccountDetails o2) {
            LocalTime time1 = o1.getBestTimeToCall();
            LocalTime time2 = o2.getBestTimeToCall();

            if (time1 == null && time2 == null) {
                return 0;
            }
            if (time1 == null) {
                return -1;
            }
            if (time2 == null) {
                return 1;
            }
            return time1.compareTo(time2);
        }

    }

}
