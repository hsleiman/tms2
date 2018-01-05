/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer.predict;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.LifecycleMapperAdapter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.DialerType;
import com.objectbrains.tms.hazelcast.AgentDialerState;
import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.hazelcast.keys.AgentQueueKey;
import com.objectbrains.tms.service.TMSAgentService;
import com.objectbrains.tms.service.InboundCallService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author connorpetty
 */
@SpringAware
public class ParticipatingAgentMapper2 extends LifecycleMapperAdapter<AgentQueueKey, AgentWeightedPriority, AgentQueueWeightedPriority, Boolean>
        implements DataSerializable {

//    private static final InboundDialer INBOUND_DIALER = new InboundDialer();
    private static final Logger LOG = LoggerFactory.getLogger(ParticipatingAgentMapper2.class);

    private final Map<Long, List<ExtensionWeightedPriority>> queueWeightedPriorityMap = new HashMap<>();
    private final Map<Integer, List<QueuePriority>> agentWeightedPriorityMap = new HashMap<>();
    private final Map<Integer, AgentDialerState> agentStateCache = new HashMap<>();
    private final Map<Long, DialerInfo> dialerInfoCache = new HashMap<>();

    private boolean checkAgentReady;
    private CallDirection callDirection;
    private boolean autoDialed;

    @Autowired
    private TMSAgentService dialerService;

//    @Autowired
//    private CallService callService;
    private ParticipatingAgentMapper2() {
    }

    public ParticipatingAgentMapper2(boolean checkAgentReady, CallDirection callDirection, boolean autoDialed) {
        this.checkAgentReady = checkAgentReady;
        this.callDirection = callDirection;
        this.autoDialed = autoDialed;
    }

    @Override
    public void initialize(Context<AgentQueueWeightedPriority, Boolean> context) {
        LOG.trace("Starting");
    }

    //we need to take each agent queue association and determine if the agent
    //is able to take part in any dialer
    //then determine the queues that this agent is part of
    //for each agent queue mapping, determine if the agent is participating in
    //that particular queue
    @Override
    public void map(AgentQueueKey key, AgentWeightedPriority value, Context<AgentQueueWeightedPriority, Boolean> context) {
        int extension = key.getExtension();
        long queuePk = key.getQueuePk();
        LOG.trace("Processing: ext: {}, queuePk: {}", extension, queuePk);
        AgentDialerState state = getAgentDialerState(extension);
        if ((value.getDialerType() != DialerType.INBOUND && !state.isDialerActive())
                || (checkAgentReady && !InboundCallService.shouldRecieveCall(false, state.getState(), state, value, null))) {
//                || (checkAgentReady && (state.getState() != AgentState.IDLE || state.hasCalls()))) {
            return;
        }

        List<ExtensionWeightedPriority> extensionWeightedPriorityList = queueWeightedPriorityMap.get(queuePk);
        if (extensionWeightedPriorityList == null) {
            extensionWeightedPriorityList = new ArrayList<>();
            queueWeightedPriorityMap.put(queuePk, extensionWeightedPriorityList);
        }
        extensionWeightedPriorityList.add(new ExtensionWeightedPriority(extension, value));

        List<QueuePriority> queuePriorityList = agentWeightedPriorityMap.get(extension);
        if (queuePriorityList == null) {
            queuePriorityList = new ArrayList<>();
            agentWeightedPriorityMap.put(extension, queuePriorityList);
        }
        queuePriorityList.add(new QueuePriority(queuePk, value.getPriority()));
    }

    @Override
    public void finalized(Context<AgentQueueWeightedPriority, Boolean> context) {
        LOG.trace("Ending");
        for (Map.Entry<Long, List<ExtensionWeightedPriority>> entrySet : queueWeightedPriorityMap.entrySet()) {
            long queuePk = entrySet.getKey();
            DialerType type = getDialerInfo(queuePk).dialerType;
            for (ExtensionWeightedPriority ewp : entrySet.getValue()) {
                for (QueuePriority queuePriority : agentWeightedPriorityMap.get(ewp.extension)) {
                    boolean participating;
                    participating = isParticipating(type, queuePriority, ewp.weightedPriority.getPriority());
                    context.emit(new AgentQueueWeightedPriority(ewp.extension, queuePk, ewp.weightedPriority), participating);
                }
            }
        }
        LOG.trace("Ended");
    }

    private AgentDialerState getAgentDialerState(Integer extension) {
        AgentDialerState state = agentStateCache.get(extension);
        if (state == null) {
            state = dialerService.getAgentDialerState(extension, callDirection, autoDialed);
            if (state == null) {
                state = new AgentDialerState();
            }
            agentStateCache.put(extension, state);
        }
        return state;
    }

    private DialerInfo getDialerInfo(Long queuePk) {
        DialerInfo dialerInfo = dialerInfoCache.get(queuePk);
        if (dialerInfo == null) {
            ExtensionWeightedPriority ewp = queueWeightedPriorityMap.get(queuePk).get(0);
            dialerInfo = new DialerInfo(ewp.weightedPriority.getDialerType(),
                    ewp.weightedPriority.getIsRunning() != null && ewp.weightedPriority.getIsRunning(),
                    ewp.weightedPriority.getHasWaitingCalls() != null && ewp.weightedPriority.getHasWaitingCalls());

//            Dialer dialer = dialerService.getDialer(queuePk);
//            if (dialer == null) {
//                dialerInfo = new DialerInfo(DialerType.INBOUND, false, false);
//            } else {
//                boolean isRunning = dialer.isRunning();
//                boolean hasWaitingCalls = callService.hasWaitingCall(queuePk);
//                dialerInfo = new DialerInfo(dialer.getDialerType(), isRunning, hasWaitingCalls);
//            }
            dialerInfoCache.put(queuePk, dialerInfo);
        }
        return dialerInfo;
    }

    private boolean isParticipating(DialerType dialerType, QueuePriority queuePriority, int targetPriority) {
        DialerInfo dialer = getDialerInfo(queuePriority.queuePk);
        switch (dialerType) {
            case PROGRESSIVE:
            case PREDICTIVE:
                switch (dialer.dialerType) {
                    case POWER:
                        if (queuePriority.priority <= targetPriority) {
                            return !dialer.hasWaitingCalls && !dialer.isRunning;
                        }
                    //fall through
                    default:
                        return true;
                    case PROGRESSIVE:
                    case PREDICTIVE:
                    //fall through
                }
            //fall through
            case POWER:
                if (queuePriority.priority < targetPriority) {
                    return !dialer.hasWaitingCalls && !dialer.isRunning;
                }
            //fall through
            default:
                return true;
        }
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        int booleans = 0;
        booleans |= checkAgentReady ? 1 : 0;
        booleans |= autoDialed ? 2 : 0;
        out.writeByte(booleans);
        CallDirection.write(out, callDirection);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        byte booleans = in.readByte();
        checkAgentReady = (booleans & 1) > 0;
        autoDialed = (booleans & 2) > 0;
        callDirection = CallDirection.read(in);
    }

    private static class ExtensionWeightedPriority {

        private int extension;
        private AgentWeightedPriority weightedPriority;

        public ExtensionWeightedPriority(int extension, AgentWeightedPriority weightedPriority) {
            this.extension = extension;
            this.weightedPriority = new AgentWeightedPriority(weightedPriority);
        }

    }

    private static class QueuePriority {

        private long queuePk;
        private int priority;

        public QueuePriority(long queuePk, int priority) {
            this.queuePk = queuePk;
            this.priority = priority;
        }

    }

    private static class DialerInfo {

        private DialerType dialerType;
        private boolean isRunning;
        private boolean hasWaitingCalls;

        public DialerInfo(DialerType dialerType, boolean isRunning, boolean hasWaitingCalls) {
            this.dialerType = dialerType;
            this.isRunning = isRunning;
            this.hasWaitingCalls = hasWaitingCalls;
        }

    }

//    private static class InboundDialer extends AbstractDialer {
//
//        @Override
//        public DialerType getDialerType() {
//            return DialerType.INBOUND;
//        }
//
//        @Override
//        protected String makeCall(Integer ext, Long phoneNumber, DialerQueueLoanDetails details) {
//            return null;
//        }
//
//    }
}
