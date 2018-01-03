/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.aggregation.impl.SetAdapter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.sti.constants.PreviewDialerType;
import com.objectbrains.sti.db.entity.base.dialer.OutboundDialerQueueSettings;
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.tms.db.entity.cdr.CallDetailRecord;
import com.objectbrains.tms.enumerated.AgentState;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.CallState;
import com.objectbrains.tms.enumerated.PhoneStatus;
import com.objectbrains.tms.freeswitch.pojo.DialerInfoPojo;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;
import com.objectbrains.tms.freeswitch.premaid.outbound.PowerDialer;
import com.objectbrains.tms.hazelcast.AbstractEntryProcessor;
import com.objectbrains.tms.hazelcast.AgentCallState;
import com.objectbrains.tms.hazelcast.Configs;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.pojo.BorrowerInfo;
import static com.objectbrains.tms.service.AgentCallService.getFirstCallInState;
import com.objectbrains.tms.service.dialer.DialerService;
import com.objectbrains.tms.websocket.Websocket;
import com.objectbrains.tms.websocket.message.outbound.PhoneToType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service(AgentCallService.BEAN_NAME)
public class AgentCallService {

    public static final String BEAN_NAME = "agentCallService";

    private static final Logger LOG = LoggerFactory.getLogger(AgentCallService.class);

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private AgentStatsService agentStatsService;

    @Autowired
    private DialerService dialerService;

    @Autowired
    private DispositionCodeService dispositionService;

    @Autowired
    @Lazy
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private DialerQueueRecordService queueRecordService;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    @Lazy
    private Websocket websocket;

    private IExecutorService executorService;

    private IMap<Integer, SetAdapter<AgentCall>> callMap;

    private Map<ScheduledKey, Future<?>> scheduledFutures = new ConcurrentHashMap<>(16);

    @PostConstruct
    private void init() {
        callMap = hazelcastService.getMap(Configs.AGENT_CALL_MAP);
        executorService = hazelcastService.getExecutorService(Configs.AGENT_CALL_EXECUTOR_SERVICE);
    }

    public Set<AgentCall> clearAgentCalls(int ext) {
        Set<AgentCall> removedCalls = callMap.remove(ext);
        handleRemovedCalls(ext, removedCalls);
        return removedCalls;
    }

    public Set<AgentCall> getAgentCalls(int ext) {
        Set<AgentCall> ret = callMap.get(ext);
        return ret != null ? ret : Collections.<AgentCall>emptySet();
    }

    public AgentCall getActiveCall(int ext) {
        return (AgentCall) callMap.executeOnKey(ext, new GetActiveCallEntryProcessor());
    }

    public AgentCall getAgentCall(int ext, String callUUID) {
        if (callUUID == null) {
            return null;
        }
        return (AgentCall) callMap.executeOnKey(ext, new GetAgentCallEntryProcessor(callUUID));
    }

    public Map<Integer, AgentCall> getActiveCalls(Collection<Agent> agents) {
        return getActiveCalls(Utils.getExtensions(agents));
    }

    @SuppressWarnings("unchecked")
    public Map<Integer, AgentCall> getActiveCalls(Set<Integer> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        return (Map) callMap.executeOnKeys(extensions, new GetActiveCallEntryProcessor());
    }

//    public boolean cannotReceiveCall(int extension) {
//        return (Boolean) callMap.executeOnKey(extension, new CannotReceiveCallEntryProcessor());
//    }
//
//    @SuppressWarnings("unchecked")
//    public Map<Integer, Boolean> cannotReceiveCalls(Set<Integer> extensions) {
//        if (extensions == null || extensions.isEmpty()) {
//            return Collections.EMPTY_MAP;
//        }
//        return (Map) callMap.executeOnKeys(extensions, new CannotReceiveCallEntryProcessor());
//    }
    public AgentCallState getAgentCallState(int ext, CallDirection callDirection, boolean autoDialed) {
        return (AgentCallState) callMap.executeOnKey(ext, new GetAgentCallStateEntryProcessor(callDirection, autoDialed));
    }

    @SuppressWarnings("unchecked")
    public Map<Integer, AgentCallState> getAgentCallStates(Set<Integer> extensions, CallDirection callDirection, boolean autoDialed) {
        if (extensions == null || extensions.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        return (Map) callMap.executeOnKeys(extensions, new GetAgentCallStateEntryProcessor(callDirection, autoDialed));
    }

    public void setBadLanguage(int ext, String badLanguage) {
        callMap.executeOnKey(ext, new BadLanguageEntryProcessor(badLanguage));
    }

    public void setQueueForCall(int ext, String callUUID, long queuePk) {
        callMap.executeOnKey(ext, new AssignQueueEntryProcessor(callUUID, queuePk));
    }

    public void setAgentFreeswitchUUID(int ext, String callUUID, String agentFreeswitchUUID) {
        callMap.executeOnKey(ext, new SetFreeswitchUUIDEntryProcessor(callUUID, agentFreeswitchUUID));
    }

    /**
     *
     * @return true if call was created for agent. false if callUUID is null or
     * call could not be created due to agent already having a ringing call.
     */
    public boolean callStarted(final int ext, final String callUUID, PreviewDialerType previewType, boolean ignoreWrap,
            BorrowerInfo borrowerInfo, CallDirection callDirection, Long queuePk, String agentFreeswitchUUID, boolean autoDialed,
            int ringingTimeout) {
        return callStarted(ext, callUUID, previewType, ignoreWrap, borrowerInfo, callDirection, queuePk, agentFreeswitchUUID, autoDialed, false, ringingTimeout);
    }

    /**
     *
     * @return true if call was created for agent. false if callUUID is null or
     * call could not be created due to agent already having a ringing call.
     */
    public boolean callStarted(final int ext, final String callUUID, PreviewDialerType previewType, boolean ignoreWrap,
            BorrowerInfo borrowerInfo, CallDirection callDirection, Long queuePk, String agentFreeswitchUUID, boolean autoDialed,
            boolean ignoreTransferring, int ringingTimeout) {

        ringingTimeout = ringingTimeout + configuration.getRingingTimeoutDelta();

        LOG.info("{} - {} - ignor wrap {}, ringingTimeout {}", ext, callUUID, ignoreWrap, ringingTimeout);
        LOG.info("{} - {} - ignor wrap {}, ringingTimeout {}", ext, callUUID, ignoreWrap, ringingTimeout);
        LOG.info("{} - {}", ext, callUUID);
        LOG.info("{} - {}", ext, callUUID);
        if (callUUID == null) {
            LOG.warn("{} - No call UUID specified, ignoring", ext);
            return false;
        }

        AgentCall call = new AgentCall(callUUID, previewType, ignoreWrap, borrowerInfo, callDirection, queuePk, agentFreeswitchUUID, autoDialed);
        CallEntryProcessor.Result result = runCallEntryProcessor(ext, new AddCallEntryProcessor(call, ignoreTransferring));
        if (result.isModified() && call.getCallState() == CallState.RINGING) {
            scheduleEndIfNotReached(ext, callUUID, ringingTimeout);
        }
        return result.isModified();
    }

    private void scheduleEndIfNotReached(int ext, String callUUID, int ringingTimeout) {
        executorService.executeOnKeyOwner(new AddScheduledRunnable(ext, callUUID, ringingTimeout), ext);
    }

    private void unscheduleEndIfNotReached(int ext, String callUUID) {
        executorService.executeOnKeyOwner(new RemoveScheduledRunnable(ext, callUUID), ext);
    }

    public void callEnded(int ext, String callUUID) {
        runCallEntryProcessor(ext, new CallEndedEntryProcessor(callUUID));
    }
//    public void updateCallState(int ext, String callUUID, PhoneStatus phoneStatus) {
//        updateCallState(ext, callUUID, phoneStatus, null, null);
//    }
//
//    public void updateCallState(int ext, String callUUID, PhoneStatus phoneStatus,
//            PhoneToType phone) {
//        updateCallState(ext, callUUID, phoneStatus, phone, null);
//    }
//
//    public void updateCallState(int ext, String callUUID, PhoneStatus phoneStatus,
//            Long callDispositionId) {
//        updateCallState(ext, callUUID, phoneStatus, null, callDispositionId);
//    }

    public void callTransferring(int ext, String callUUID) {
        LOG.info("{} marking call {} as transferring", ext, callUUID);
        runCallEntryProcessor(ext, new CallTransferingEntryProcessor(callUUID));
    }

    public AgentCall updateCallState(int ext, String callUUID, PhoneStatus phoneStatus,
            PhoneToType phone, Long callDispositionId) {
        LOG.info("{} - {} - {}", ext, callUUID, phoneStatus);
        LOG.info("{} - {} - {}", ext, callUUID, phoneStatus);
        LOG.info("{} - {} - {}", ext, callUUID, phoneStatus);
        if (callUUID == null) {
            LOG.warn("{} - No call UUID specified, ignoring", ext);
            return null;
        }
        CallEntryProcessor.Result result = null;
        switch (phoneStatus) {
            case RINGING:
                //this doesn't change agent state so we don't use runCallEntryProcessor
                result = (CallEntryProcessor.Result) callMap.executeOnKey(ext, new CallAgentReachedEntryProcessor(callUUID));
                break;
            case ANSWER:
                result = runCallEntryProcessor(ext, new CallAnsweredEntryProcessor(callUUID));
                break;
            case ON_CALL:
            case OFFHOLD:
                result = runCallEntryProcessor(ext, new CallOffHoldEntryProcessor(callUUID));
                break;
            case ONHOLD:
                result = runCallEntryProcessor(ext, new CallOnHoldEntryProcessor(callUUID));
                break;
            case TRANSFER:
                result = runCallEntryProcessor(ext, new CallTransferingEntryProcessor(callUUID));
                break;
            case AGENT_HANGUP:
                result = runCallEntryProcessor(ext, new CallAgentHungupEntryProcessor(callUUID));
                break;
            case HANGUP:
                result = runCallEntryProcessor(ext, new CallCallerHungupEntryProcessor(callUUID));
                break;
            case HANGUP_TIMEOUT:
                //this isn't supposed to change the agent state so we don't use runCallEntryProcessor
                result = (CallEntryProcessor.Result) callMap.executeOnKey(ext, new CallCallerHungupEntryProcessor(callUUID));
                handleRemovedCalls(ext, result.getDoneCalls());
                clearAgentCalls(ext);
                agentStatsService.setState(ext, AgentState.FORCE, Duration.ZERO);
                break;
            case WRAP_START:
                break;
            case WRAP:
                result = runCallEntryProcessor(ext, new CallWrappedEntryProcessor(callUUID, callDispositionId));
                break;
            case PREVIEW_ACCEPT:
                result = runCallEntryProcessor(ext, new CallAcceptedEntryProcessor(callUUID,
                        phone.getPhoneNumber().toString(), phone.getFirstName(), phone.getLastName()));
                AgentCall call = getAgentCall(result.getActiveCalls(), callUUID);
                int ringingTimeout = configuration.getCallWaitTimeoutBeforeConnect(call.getCallDirection());
                DialerInfoPojo pojo = new DialerInfoPojo();
                pojo.setAgentExt(ext);
                pojo.setCallUUID(callUUID);
                pojo.setLoanId(call.getBorrowerInfo().getLoanId());
                pojo.setPreviewDialerType(call.getPreviewType());
                pojo.setSettings((OutboundDialerQueueSettings) queueRecordService.getQueueSettings(call.getQueuePk()));
                pojo.addPhoneToTypeSingle(phone);
                pojo.setBorrowerFirstName(phone.getFirstName());
                pojo.setBorrowerLastName(phone.getLastName());

                DialplanBuilder builder = new PowerDialer(pojo);
                dialerService.callInProgress(pojo.getCallUUID(), phone.getPhoneNumber());
                builder.setTMS_UUID(pojo.getCallUUID());
                builder.execute();
                scheduleEndIfNotReached(ext, callUUID, ringingTimeout);
                break;
            case PREVIEW_REJECT:
                result = runCallEntryProcessor(ext, new CallRejectedEntryProcessor(callUUID));
                break;
        }
        AgentCall call = null;
        if (result.isModified()) {
            call = getAgentCall(result.getActiveCalls(), callUUID);
            if (call == null) {
                call = getAgentCall(result.getDoneCalls(), callUUID);
            }
        }
        return call;
    }

    private void handleRemovedCalls(int ext, Collection<AgentCall> agentCalls) {
        if (agentCalls == null) {
            return;
        }

        for (AgentCall agentCall : agentCalls) {
            if (!agentCall.isAutoDialed()) {
                continue;
            }
            unscheduleEndIfNotReached(ext, agentCall.getCallUUID());
            //find dialer calls that were removed prematurely
            if (agentCall.getCallState() != CallState.DONE) {
                Long dispId = agentCall.getDispositionId();
                CallDispositionCode code;
                if (dispId != null) {
                    code = dispositionService.getDispositionCodeFromId(dispId);
                } else {
                    code = dispositionService.noWrapCode();
                }
                dialerService.callEnded(agentCall.getCallUUID(), code);
            } else if (agentCall.isRejected()) {
                dialerService.callEnded(agentCall.getCallUUID(), dispositionService.recordRestrictedCode());
            } else if (agentCall.isWrapped()) {
                CallDetailRecord mcdr = callDetailRecordService.getCDR(agentCall.getCallUUID());
                if (mcdr == null) {
                    continue;
                }
                LOG.info("Call Master Detail Record: {} -> {}", mcdr.getCall_uuid(), mcdr.getComplete());
                if (mcdr.getComplete() != null && mcdr.getComplete()) {
                    Long dispId = agentCall.getDispositionId();
                    CallDispositionCode code;
                    if (dispId != null) {
                        code = dispositionService.getDispositionCodeFromId(dispId);
                    } else {
                        code = dispositionService.callerUnknownCode();
                    }
                    dialerService.callEnded(agentCall.getCallUUID(), code);
                }
            }
        }
    }

    public void callTransfered(int ext, String callUUID) {
        LOG.info("{} - {}", ext, callUUID);
        LOG.info("{} - {}", ext, callUUID);
        LOG.info("{} - {}", ext, callUUID);
        if (callUUID == null) {
            LOG.warn("{} No call UUID specified, ignoring", ext);
            return;
        }
        runCallEntryProcessor(ext, new CallTranferedEntryProcessor(callUUID));
    }

    public AgentCall getTransferingCall(int ext) {
        return (AgentCall) callMap.executeOnKey(ext, new GetTranferingCallEntryProcessor());
    }

    @SuppressWarnings("unchecked")
    public Map.Entry<Integer, AgentCall> getTransferingCall(String callUUID) {
        Map<Integer, AgentCall> results = getAgentCalls(callUUID);
        for (Map.Entry<Integer, AgentCall> entry : results.entrySet()) {
            if (entry.getValue().getCallState() == CallState.TRANSFERRING) {
                return entry;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Map<Integer, AgentCall> getAgentCalls(String callUUID) {
        return (Map) callMap.executeOnEntries(new GetAgentCallEntryProcessor(callUUID));
    }

    private CallEntryProcessor.Result runCallEntryProcessor(int ext, CallEntryProcessor entryProcessor) {
        CallEntryProcessor.Result result = (CallEntryProcessor.Result) callMap.executeOnKey(ext, entryProcessor);
        exit:
        {
            if (!result.isModified()) {
                break exit;
            }
            handleRemovedCalls(ext, result.getDoneCalls());

            Set<AgentCall> calls = result.getActiveCalls();
            if (calls == null) {
                LOG.info("{} calls is Null ", ext);
            } else {
                LOG.info("{} calls has {}", ext, calls.size());
            }
//            AgentStats stats = agentStatsService.getAgentStats(ext);
            AgentState currentState = agentStatsService.getAgentState(ext);
            if (currentState != null && !currentState.isReadyState()) {
                break exit;
            }
            AgentState agentState = nextAgentState(currentState, calls);
            LOG.info("{} agentState is {}", ext, agentState);
            switch (agentState) {
                case IDLE:
                    agentStatsService.setAgentToIdle(ext);
                    break;
                case WRAP:
                    Set<Long> queuePks = new HashSet<>();
                    for (AgentCall call : calls) {
                        Long queuePk = call.getQueuePk();
                        if (queuePk != null) {
                            queuePks.add(queuePk);
                        }
                    }
                    agentStatsService.setAgentToWrap(ext, queuePks);
                    break;
                default:
                    agentStatsService.setState(ext, agentState, Duration.ZERO);
                    break;
            }
        }
        return result;
    }

    static AgentState nextAgentState(AgentState previousState, Set<AgentCall> calls) {
        AgentState defaultState = AgentState.IDLE;
//        if (previousState != null && !previousState.isReadyState()) {
//            defaultState = previousState;
//        } else {
//            defaultState = AgentState.IDLE;
//        }
//        if (previousState == null) {
//            return AgentState.OFFLINE;
//        }
//        if (previousState.isReadyState()) {
//            return AgentState.IDLE;
//        }
//        return previousState;
        if (calls == null) {
            return defaultState;
        }
        //don't include internal calls, they aren't relevant to agent states
        Collection<AgentCall> nonInternalCalls = new ArrayList<>(calls.size());
        for (AgentCall call : calls) {
            if (call.getCallDirection() != CallDirection.INTERNAL) {
                nonInternalCalls.add(call);
            }
        }
        if (nonInternalCalls.isEmpty()) {
            return defaultState;
        }
        AgentCall activeCall = getFirstCallInState(nonInternalCalls, CallState.values());
        LOG.info("ActiveCall is {} - {} - {}", activeCall.getCallState(), activeCall.getCallUUID(), activeCall.getCallDirection());
        return activeCall.getCallState().getImpliedAgentState();
    }

    static AgentCall getAgentCall(Iterable<AgentCall> calls, String callUUID) {
        if (calls != null) {
            for (AgentCall call : calls) {
                if (call.getCallUUID().equals(callUUID)) {
                    return call;
                }
            }
        }
        return null;
    }

    static AgentCall getPrimaryCall(Iterable<AgentCall> calls) {
        return getFirstCallInState(calls, CallState.TRANSFERRING, CallState.ACTIVE,
                CallState.RINGING, CallState.HOLD, CallState.PREVIEW);
    }

    static AgentCall getFirstCallInState(Iterable<AgentCall> calls, CallState... states) {
        if (calls != null) {
            for (CallState state : states) {
                LOG.debug("Searching for first call in state {}", state);
                for (AgentCall call : calls) {
                    LOG.debug("Checking call {} in state {}", call.getCallUUID(), call.getCallState());
                    if (call.getCallState() == state) {
                        return call;
                    }
                }
            }
        }
        return null;
    }

    static int countActiveCalls(Iterable<AgentCall> calls) {
        int activeCallCount = 0;
        if (calls != null) {
            for (AgentCall call : calls) {
                if (call.getCallDirection() != CallDirection.INTERNAL) {
                    switch (call.getCallState()) {
                        case ACTIVE:
                        case HOLD:
                        case RINGING:
                        case TRANSFERRING:
                            activeCallCount++;
                    }
                }
            }
        }
        return activeCallCount;
    }

    boolean cannotReceiveCall(int ext, Iterable<AgentCall> calls, CallDirection direction, boolean autoDialed, boolean ignoreTransferring) {
        AgentCall call = getFirstCallInState(calls, CallState.RINGING, CallState.TRANSFERRING);
        boolean fcis = call != null && (call.getCallState() == CallState.RINGING || !ignoreTransferring);
        LOG.debug("{} - First Call In State {} is {}", ext, call != null ? call.getCallState() : null, fcis);
        if (fcis) {
            return true;
        }

        int callCount = countActiveCalls(calls);
        int maxCallCount = autoDialed ? 1 : 2;
        boolean cdacc = (direction != CallDirection.INTERNAL && callCount >= maxCallCount);
        LOG.debug("{} - Direction {} and count active calls {}/{} is {}", ext, direction, callCount, maxCallCount, cdacc);
        if (cdacc) {
            return true;
        }
        boolean noSession = !websocket.hasSessions(ext);
        LOG.debug("{} - Websocket Session is {}", ext, noSession);
        if (noSession) {
            return true;
        }
        return false;

//        return getFirstCallInState(calls, CallState.RINGING) != null
//                || (direction != CallDirection.INTERNAL && countActiveCalls(calls) >= 2)
//                || !websocket.hasSessions(ext);
    }

    private static class ScheduledKey implements DataSerializable {

        private int ext;
        private String callUUID;

        private ScheduledKey() {
        }

        public ScheduledKey(int ext, String callUUID) {
            this.ext = ext;
            this.callUUID = callUUID;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + this.ext;
            hash = 97 * hash + Objects.hashCode(this.callUUID);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ScheduledKey other = (ScheduledKey) obj;
            if (this.ext != other.ext) {
                return false;
            }
            if (!Objects.equals(this.callUUID, other.callUUID)) {
                return false;
            }
            return true;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeInt(ext);
            out.writeUTF(callUUID);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            ext = in.readInt();
            callUUID = in.readUTF();
        }

    }

    @SpringAware
    private static class AddScheduledRunnable implements Runnable, DataSerializable {

        @Autowired
        @Qualifier(AgentCallService.BEAN_NAME)
        private AgentCallService callService;

        @Autowired
        private TaskScheduler scheduler;

        private ScheduledKey key;
        private int timeoutSec;

        private AddScheduledRunnable() {
        }

        public AddScheduledRunnable(int ext, String callUUID, int timeoutSec) {
            this.key = new ScheduledKey(ext, callUUID);
            this.timeoutSec = timeoutSec;
        }

        @Override
        public void run() {
            LocalDateTime time = LocalDateTime.now().plusSeconds(timeoutSec);
            Future<?> future = scheduler.schedule(new Runnable() {

                @Override
                public void run() {
                    callService.scheduledFutures.remove(key);
                    callService.runCallEntryProcessor(key.ext, new EndIfNotReachedEntryProcessor(key.callUUID));
                }

            }, time.toDate());
            callService.scheduledFutures.put(key, future);
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeObject(key);
            out.writeInt(timeoutSec);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            key = in.readObject();
            timeoutSec = in.readInt();
        }

    }

    @SpringAware
    private static class RemoveScheduledRunnable implements Runnable, DataSerializable {

        @Autowired
        @Qualifier(AgentCallService.BEAN_NAME)
        private AgentCallService callService;

        private ScheduledKey key;

        private RemoveScheduledRunnable() {
        }

        public RemoveScheduledRunnable(int ext, String callUUID) {
            this.key = new ScheduledKey(ext, callUUID);
        }

        @Override
        public void run() {
            Future<?> future = callService.scheduledFutures.remove(key);
            if (future != null) {
                future.cancel(false);
            }
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeObject(key);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            key = in.readObject();
        }

    }

}

abstract class AgentCallEntryProcessor extends AbstractEntryProcessor<Integer, SetAdapter<AgentCall>> {

    protected AgentCallEntryProcessor() {
        super(false);
    }

    protected AgentCallEntryProcessor(boolean applyOnBackup) {
        super(applyOnBackup);
    }

}

abstract class CallEntryProcessor extends AgentCallEntryProcessor {

    protected CallEntryProcessor() {
        super(true);
    }

    @Override
    protected final Result process(Map.Entry<Integer, SetAdapter<AgentCall>> entry, boolean isPrimary) {
        Result result = processInternal(entry);
        if (!isPrimary) {
            entry.setValue(result.activeCalls);
        }
        return result;
    }

    public abstract Result processInternal(Map.Entry<Integer, SetAdapter<AgentCall>> entry);

    public static class Result implements DataSerializable {

        private boolean modified;
        private SetAdapter<AgentCall> activeCalls = null;
        private SetAdapter<AgentCall> doneCalls = null;

        private Result() {
        }

        protected Result(boolean modified, SetAdapter<AgentCall> calls) {
            this.modified = modified;
            if (modified && calls != null) {
                for (AgentCall call : calls) {
                    if (call.getCallState() == CallState.DONE) {
                        if (doneCalls == null) {
                            doneCalls = new SetAdapter<>();
                        }
                        doneCalls.add(call);
                    } else {
                        if (activeCalls == null) {
                            activeCalls = new SetAdapter<>();
                        }
                        activeCalls.add(call);
                    }
                }
            }
        }

        public boolean isModified() {
            return modified;
        }

        public Set<AgentCall> getActiveCalls() {
            return activeCalls;
        }

        public Set<AgentCall> getDoneCalls() {
            return doneCalls;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            boolean activeCallsNotNull = activeCalls != null;
            boolean doneCallsNotNull = doneCalls != null;

            byte booleans = 0;
            booleans |= modified ? 1 : 0;
            booleans |= activeCallsNotNull ? 2 : 0;
            booleans |= doneCallsNotNull ? 4 : 0;
            out.writeByte(booleans);

            if (activeCallsNotNull) {
                activeCalls.writeData(out);
            }
            if (doneCallsNotNull) {
                doneCalls.writeData(out);
            }
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            byte booleans = in.readByte();
            modified = (booleans & 1) > 0;
            boolean activeCallsNotNull = (booleans & 2) > 0;
            boolean doneCallsNotNull = (booleans & 4) > 0;
            if (activeCallsNotNull) {
                activeCalls = new SetAdapter<>();
                activeCalls.readData(in);
            }
            if (doneCallsNotNull) {
                doneCalls = new SetAdapter<>();
                doneCalls.readData(in);
            }
        }
    }

}

class EndIfNotReachedEntryProcessor extends UpdateCallEntryProcessor {

    private EndIfNotReachedEntryProcessor() {
    }

    public EndIfNotReachedEntryProcessor(String callUUID) {
        super(callUUID);
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        if (!call.isAgentReached()) {
            return call.terminate();
        }
        return false;
    }

}

class AssignQueueEntryProcessor extends UpdateCallEntryProcessor {

    private long queuePk;

    private AssignQueueEntryProcessor() {
    }

    public AssignQueueEntryProcessor(String callUUID, long queuePk) {
        super(callUUID);
        this.queuePk = queuePk;
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        call.setQueuePk(queuePk);
        return true;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeLong(queuePk);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        queuePk = in.readLong();
    }

}

class SetFreeswitchUUIDEntryProcessor extends UpdateCallEntryProcessor {

    private String freeswitchUUID;

    private SetFreeswitchUUIDEntryProcessor() {
    }

    public SetFreeswitchUUIDEntryProcessor(String callUUID, String freeswitchUUID) {
        super(callUUID);
        this.freeswitchUUID = freeswitchUUID;
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        call.setAgentFreeswitchUUID(freeswitchUUID);
        return true;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeUTF(freeswitchUUID);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        freeswitchUUID = in.readUTF();
    }

}

class BadLanguageEntryProcessor extends AgentCallEntryProcessor {

    private String badLanguage;

    private BadLanguageEntryProcessor() {
        super(true);
    }

    public BadLanguageEntryProcessor(String badLanguage) {
        this();
        this.badLanguage = badLanguage;
    }

    @Override
    public Void process(Map.Entry<Integer, SetAdapter<AgentCall>> entry, boolean isPrimary) {
        SetAdapter<AgentCall> calls = entry.getValue();
        AgentCall activeCall = AgentCallService.getFirstCallInState(calls,
                CallState.TRANSFERRING, CallState.ACTIVE, CallState.RINGING);
        if (activeCall != null) {
            activeCall.setBadLanguage(badLanguage);
            entry.setValue(calls);
        }
        return null;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeUTF(badLanguage);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        badLanguage = in.readUTF();
    }

}

@SpringAware
class AddCallEntryProcessor extends CallEntryProcessor {

    @Autowired
    @Qualifier(AgentCallService.BEAN_NAME)
    private AgentCallService agentCallService;

    private AgentCall call;
    private Boolean cannotReceive = null;
    private boolean ignoreTransferring;

    private AddCallEntryProcessor() {
    }

    public AddCallEntryProcessor(AgentCall call, boolean ignoreTransferring) {
        this.call = call;
        this.ignoreTransferring = ignoreTransferring;
    }

    @Override
    public Result processInternal(Map.Entry<Integer, SetAdapter<AgentCall>> entry) {
        SetAdapter<AgentCall> calls = entry.getValue();
        if (cannotReceive == null) {
            cannotReceive = agentCallService.cannotReceiveCall(entry.getKey(), calls, call.getCallDirection(), call.isAutoDialed(), ignoreTransferring);
        }
        if (cannotReceive) {
            return new Result(false, null);
        }
        if (calls == null) {
            calls = new SetAdapter<>();
        }
        boolean modified = calls.add(call);
        if (modified) {
            entry.setValue(calls);
        }
        return new Result(modified, calls);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        call.writeData(out);
        out.writeObject(cannotReceive);
        out.writeBoolean(ignoreTransferring);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        call = new AgentCall();
        call.readData(in);
        cannotReceive = in.readObject();
        ignoreTransferring = in.readBoolean();
    }

}

abstract class UpdateCallEntryProcessor extends CallEntryProcessor {

    private String callUUID;

    protected UpdateCallEntryProcessor() {
    }

    public UpdateCallEntryProcessor(String callUUID) {
        this.callUUID = callUUID;
    }

    protected abstract boolean updateTargetCall(AgentCall call);

    protected boolean updateOtherCall(AgentCall call) {
        //Empty implementation
        return false;
    }

    @Override
    public final Result processInternal(Map.Entry<Integer, SetAdapter<AgentCall>> entry) {
        boolean modified = false;
        boolean foundCall = false;
        SetAdapter<AgentCall> calls = entry.getValue();
        if (calls != null) {
            for (AgentCall call : calls) {
                if (call.getCallUUID().equals(callUUID)) {
                    modified |= updateTargetCall(call);
                    foundCall = true;
                } else {
                    modified |= updateOtherCall(call);
                }
            }
            modified &= foundCall;
            if (modified) {
                entry.setValue(calls);
            }
        }
        return new Result(modified, calls);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeUTF(callUUID);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        callUUID = in.readUTF();
    }

}

class CallAgentReachedEntryProcessor extends UpdateCallEntryProcessor {

    private CallAgentReachedEntryProcessor() {
    }

    public CallAgentReachedEntryProcessor(String callUUID) {
        super(callUUID);
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        return call.agentReached();
    }

}

class CallAcceptedEntryProcessor extends UpdateCallEntryProcessor {

    private String phoneNumber;
    private String firstName;
    private String lastName;

    private CallAcceptedEntryProcessor() {
    }

    public CallAcceptedEntryProcessor(String callUUID, String phoneNumber, String firstName, String lastName) {
        super(callUUID);
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        BorrowerInfo info = call.getBorrowerInfo();
        info.setBorrowerPhoneNumber(phoneNumber);
        info.setBorrowerFirstName(firstName);
        info.setBorrowerLastName(lastName);
        return call.accepted();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeUTF(phoneNumber);
        out.writeUTF(firstName);
        out.writeUTF(lastName);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        phoneNumber = in.readUTF();
        firstName = in.readUTF();
        lastName = in.readUTF();
    }

}

class CallRejectedEntryProcessor extends UpdateCallEntryProcessor {

    private CallRejectedEntryProcessor() {
    }

    public CallRejectedEntryProcessor(String callUUID) {
        super(callUUID);
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        return call.rejected();
    }

}

class CallTranferedEntryProcessor extends UpdateCallEntryProcessor {

    private CallTranferedEntryProcessor() {
    }

    public CallTranferedEntryProcessor(String callUUID) {
        super(callUUID);
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        return call.transferred();
    }

}

class CallTransferingEntryProcessor extends UpdateCallEntryProcessor {

    private CallTransferingEntryProcessor() {
    }

    public CallTransferingEntryProcessor(String callUUID) {
        super(callUUID);
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        return call.transferring();
    }

}

class CallAnsweredEntryProcessor extends UpdateCallEntryProcessor {

    private CallAnsweredEntryProcessor() {
    }

    public CallAnsweredEntryProcessor(String callUUID) {
        super(callUUID);
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        return call.answered();
    }

    @Override
    protected boolean updateOtherCall(AgentCall call) {
        return call.onHold();
    }

}

class CallOffHoldEntryProcessor extends UpdateCallEntryProcessor {

    private CallOffHoldEntryProcessor() {
    }

    public CallOffHoldEntryProcessor(String callUUID) {
        super(callUUID);
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        return call.offHold();
    }

    @Override
    protected boolean updateOtherCall(AgentCall call) {
        return call.onHold();
    }

}

class CallOnHoldEntryProcessor extends UpdateCallEntryProcessor {

    private CallOnHoldEntryProcessor() {
    }

    public CallOnHoldEntryProcessor(String callUUID) {
        super(callUUID);
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        return call.onHold();
    }

}

class CallAgentHungupEntryProcessor extends UpdateCallEntryProcessor {

    private CallAgentHungupEntryProcessor() {
    }

    public CallAgentHungupEntryProcessor(String callUUID) {
        super(callUUID);
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        return call.agentHungup();
    }

}

class CallCallerHungupEntryProcessor extends UpdateCallEntryProcessor {

    private CallCallerHungupEntryProcessor() {
    }

    public CallCallerHungupEntryProcessor(String callUUID) {
        super(callUUID);
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        return call.callerHungup();
    }

}

class CallWrappedEntryProcessor extends UpdateCallEntryProcessor {

    private Long dispositionId;

    private CallWrappedEntryProcessor() {
    }

    public CallWrappedEntryProcessor(String callUUID, Long dispositionId) {
        super(callUUID);
        this.dispositionId = dispositionId;
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        return call.wrapped(dispositionId);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeObject(dispositionId);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        dispositionId = in.readObject();
    }

}

class CallEndedEntryProcessor extends UpdateCallEntryProcessor {

    private CallEndedEntryProcessor() {
    }

    public CallEndedEntryProcessor(String callUUID) {
        super(callUUID);
    }

    @Override
    protected boolean updateTargetCall(AgentCall call) {
        return call.terminate();
    }

}

class GetActiveCallEntryProcessor extends AgentCallEntryProcessor {

    @Override
    public AgentCall process(Map.Entry<Integer, SetAdapter<AgentCall>> entry, boolean isPrimary) {
        return AgentCallService.getFirstCallInState(entry.getValue(),
                CallState.TRANSFERRING, CallState.ACTIVE,
                CallState.RINGING, CallState.HOLD, CallState.PREVIEW);
    }

}

class GetTranferingCallEntryProcessor extends AgentCallEntryProcessor {

    @Override
    public AgentCall process(Map.Entry<Integer, SetAdapter<AgentCall>> entry, boolean isPrimary) {
        return AgentCallService.getFirstCallInState(entry.getValue(),
                CallState.TRANSFERRING);
    }

}

//class CannotReceiveCallEntryProcessor extends AgentCallEntryProcessor {
//
//    @Override
//    public Boolean process(Map.Entry<Integer, SetAdapter<AgentCall>> entry) {
//        return AgentCallService.cannotReceiveCall(entry.getValue());
//    }
//
//}
@SpringAware
class GetAgentCallStateEntryProcessor extends AgentCallEntryProcessor {

    @Autowired
    @Qualifier(AgentCallService.BEAN_NAME)
    private AgentCallService agentCallService;

    private CallDirection callDirection;
    private boolean autoDialed;

    private GetAgentCallStateEntryProcessor() {
    }

    public GetAgentCallStateEntryProcessor(CallDirection callDirection, boolean autoDialed) {
        this.callDirection = callDirection;
        this.autoDialed = autoDialed;
    }

    @Override
    public AgentCallState process(Map.Entry<Integer, SetAdapter<AgentCall>> entry, boolean isPrimary) {
        AgentCallState state = new AgentCallState();
        SetAdapter<AgentCall> calls = entry.getValue();
        state.setHasCalls(calls != null && !calls.isEmpty());
        state.setCannotReceive(agentCallService.cannotReceiveCall(entry.getKey(), calls, callDirection, autoDialed, false));
        return state;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        CallDirection.write(out, callDirection);
        out.writeBoolean(autoDialed);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        callDirection = CallDirection.read(in);
        autoDialed = in.readBoolean();
    }

}

class GetAgentCallEntryProcessor extends AgentCallEntryProcessor {

    private String callUUID;

    private GetAgentCallEntryProcessor() {
    }

    public GetAgentCallEntryProcessor(String callUUID) {
        this.callUUID = callUUID;
    }

    @Override
    public AgentCall process(Map.Entry<Integer, SetAdapter<AgentCall>> entry, boolean isPrimary) {
        return AgentCallService.getAgentCall(entry.getValue(), callUUID);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeUTF(callUUID);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        callUUID = in.readUTF();
    }

}
